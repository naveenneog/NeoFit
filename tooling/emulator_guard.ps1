<#
  emulator_guard.ps1 — coordinate ONE shared Android emulator across multiple app builds.

  When several apps under DailyApps share a single emulator, a test run must NOT
  steal focus from another build that is mid-test. This guard reports occupancy
  and provides a cooperative, auto-expiring lock so each build claims the device
  before install/launch/test and releases it afterwards.

  Signals used (most reliable first):
    * topResumedActivity  — the true foreground activity/package
    * a shared lock file  — /data/local/tmp/dailyapps_emu.lock (owner;host;ts)

  Usage:
    pwsh tooling/emulator_guard.ps1 status                       # FREE (exit 0) or BUSY (exit 2)
    pwsh tooling/emulator_guard.ps1 foreground                   # print foreground package
    pwsh tooling/emulator_guard.ps1 acquire   -App com.neofit    # claim (0) or refuse (2)
    pwsh tooling/emulator_guard.ps1 heartbeat -App com.neofit    # refresh claim during long tests
    pwsh tooling/emulator_guard.ps1 release   -App com.neofit    # release claim
    pwsh tooling/emulator_guard.ps1 wait      -App com.neofit -TimeoutSeconds 300  # block until free, then acquire

  Convention: EVERY DailyApps build calls this with the SAME lock file (default
  below), so they coordinate. Exit codes: 0 free/ok, 2 busy/refused, 3 no device.
#>
[CmdletBinding()]
param(
  [Parameter(Position = 0)]
  [ValidateSet('status', 'foreground', 'acquire', 'release', 'heartbeat', 'wait')]
  [string]$Action = 'status',
  [string]$App = 'com.neofit',
  [int]$TtlSeconds = 900,
  [int]$TimeoutSeconds = 300,
  [string]$Serial
)

$ErrorActionPreference = 'Stop'
$sdk = if ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT }
       elseif ($env:ANDROID_HOME) { $env:ANDROID_HOME }
       else { "$env:LOCALAPPDATA\Android\Sdk" }
$adb = Join-Path $sdk 'platform-tools\adb.exe'
$lockPath = '/data/local/tmp/dailyapps_emu.lock'
$target = if ($Serial) { @('-s', $Serial) } else { @() }

function Adb { & $adb @target @args }

function Get-Foreground {
  $dump = Adb shell dumpsys activity activities 2>$null
  $m = $dump | Select-String 'topResumedActivity=ActivityRecord\{\S+ \S+ ([^/} ]+)/' | Select-Object -First 1
  if ($m) { return $m.Matches[0].Groups[1].Value }
  $w = Adb shell dumpsys window 2>$null |
    Select-String 'mCurrentFocus=Window\{\S+ \S+ ([^/} ]+)/' | Select-Object -First 1
  if ($w) { return $w.Matches[0].Groups[1].Value }
  return ''
}

function Test-HomeOrFree([string]$pkg) {
  if ([string]::IsNullOrWhiteSpace($pkg)) { return $true }
  if ($pkg -like '*launcher*') { return $true }
  if ($pkg -in @('com.android.systemui', 'com.google.android.apps.nexuslauncher')) { return $true }
  return $false
}

function Read-Lock {
  $raw = (Adb shell cat $lockPath 2>$null) -join "`n"
  if ([string]::IsNullOrWhiteSpace($raw)) { return $null }
  $o = @{}
  foreach ($kv in ($raw -split ';')) {
    if ($kv -match '^\s*(\w+)=(.*)$') { $o[$Matches[1]] = $Matches[2].Trim() }
  }
  return $o
}

function Get-Epoch { [int][double]::Parse((Get-Date -UFormat %s)) }

function Test-LockLive($lock) {
  if (-not $lock) { return $false }
  $ts = 0; [void][int]::TryParse([string]$lock.ts, [ref]$ts)
  return ((Get-Epoch) - $ts) -lt $TtlSeconds
}

function Write-Lock {
  $content = "owner=$App;host=$env:COMPUTERNAME;ts=$(Get-Epoch)"
  Adb shell "echo '$content' > $lockPath" | Out-Null
}

function Get-BusyReason {
  $fg = Get-Foreground
  if (-not (Test-HomeOrFree $fg) -and $fg -ne $App) { return "another app in foreground ($fg)" }
  $lock = Read-Lock
  if ((Test-LockLive $lock) -and $lock.owner -ne $App) {
    return "claimed by $($lock.owner) (host $($lock.host), $((Get-Epoch) - [int]$lock.ts)s ago)"
  }
  return $null
}

# --- device present? ---
$devs = (Adb devices) -split "`n" | Where-Object { $_ -match '\bdevice$' }
if (-not $devs) { Write-Output 'NO_DEVICE: no emulator/device connected'; exit 3 }

switch ($Action) {
  'foreground' {
    Write-Output "foreground=$(Get-Foreground)"; exit 0
  }
  'status' {
    $reason = Get-BusyReason
    if ($reason) { Write-Output "BUSY: $reason"; exit 2 }
    Write-Output "FREE: foreground=$(Get-Foreground)"; exit 0
  }
  'acquire' {
    $reason = Get-BusyReason
    if ($reason) { Write-Output "REFUSED: $reason"; exit 2 }
    Write-Lock; Write-Output "ACQUIRED by $App"; exit 0
  }
  'heartbeat' {
    $lock = Read-Lock
    if ($lock -and $lock.owner -ne $App -and (Test-LockLive $lock)) {
      Write-Output "REFUSED: lock held by $($lock.owner)"; exit 2
    }
    Write-Lock; Write-Output "HEARTBEAT $App"; exit 0
  }
  'release' {
    $lock = Read-Lock
    if ($lock -and $lock.owner -ne $App) { Write-Output "SKIP: lock owned by $($lock.owner)"; exit 0 }
    Adb shell rm -f $lockPath | Out-Null
    Write-Output 'RELEASED'; exit 0
  }
  'wait' {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
      $reason = Get-BusyReason
      if (-not $reason) { Write-Lock; Write-Output "ACQUIRED by $App"; exit 0 }
      Write-Output "waiting... $reason"
      Start-Sleep -Seconds 5
    }
    Write-Output "TIMEOUT after $TimeoutSeconds s -- still busy"; exit 2
  }
}
