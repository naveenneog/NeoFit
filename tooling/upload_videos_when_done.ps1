# Waits for the Sora-2 landscape regeneration to finish, then uploads the
# resulting clips to the GitHub `exercise-videos` release (clobbering the
# old portrait assets in place so the app's stream URLs stay valid).
$ErrorActionPreference = 'Continue'
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
$log = Join-Path $root 'tooling\video_gen.log'
$out = Join-Path $root 'tooling\upload_videos.log'
function W($m) { "$(Get-Date -Format 'HH:mm:ss') $m" | Tee-Object -FilePath $out -Append }

W "watcher started; waiting for ALL DONE in $log"
$deadline = (Get-Date).AddHours(3)
while ((Get-Date) -lt $deadline) {
    if (Test-Path $log) {
        $tail = Get-Content $log -Tail 3 -ErrorAction SilentlyContinue
        if ($tail -match 'ALL DONE') { W "generator reported: $($tail | Where-Object { $_ -match 'ALL DONE' })"; break }
    }
    Start-Sleep -Seconds 30
}

$files = Get-ChildItem (Join-Path $root 'tooling\videos\*.mp4') -ErrorAction SilentlyContinue
if (-not $files) { W 'no mp4 files found; aborting upload'; exit 1 }
W ("uploading {0} clips to release exercise-videos" -f $files.Count)
$paths = $files | ForEach-Object { $_.FullName }
& gh release upload exercise-videos $paths --clobber --repo naveenneog/NeoFit 2>&1 | ForEach-Object { W $_ }
W 'upload complete'
