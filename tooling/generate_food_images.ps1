param(
  [int]$Size = 1024,
  [int]$Target = 512
)
# Generates a gpt-image-2 photo for every dish in FoodKnowledgeBase and bundles a
# downscaled JPEG into app/src/main/assets/food/<id>.jpg. Skips existing files.
$ErrorActionPreference = 'Continue'
Add-Type -AssemblyName System.Drawing

$root = "C:\Users\navg\DailyApps\NeoFit"
$seed = "$root\app\src\main\java\com\neofit\data\seed\FoodKnowledgeBase.kt"
$outDir = "$root\app\src\main\assets\food"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$log = "$root\tooling\food_gen.log"
$endpoint = "https://ai-contosohub530569751908.cognitiveservices.azure.com"
$uri = "$endpoint/openai/deployments/gpt-image-2/images/generations?api-version=2025-04-01-preview"

function Log($m) { $line = "$(Get-Date -Format HH:mm:ss) $m"; Add-Content $log $line; Write-Output $line }

$regionMap = @{
  SOUTH="South Indian"; NORTH="North Indian"; NORTH_EAST="North-East Indian";
  WEST="West Indian"; CENTRAL="Central Indian"; EAST="East Indian (Bengali)";
  PAN_INDIA=""; MIXED="" }

# Parse: item("id", "Name", REGION,
$rx = [regex]'item\(\s*"([^"]+)"\s*,\s*"([^"]+)"\s*,\s*([A-Z_]+)\s*,'
$items = @()
foreach ($m in $rx.Matches((Get-Content $seed -Raw))) {
  $items += [pscustomobject]@{ id=$m.Groups[1].Value; name=$m.Groups[2].Value; region=$m.Groups[3].Value }
}
Log "Parsed $($items.Count) dishes."

$token = $null; $tokenAt = Get-Date
function Refresh-Token { $script:token = az account get-access-token --resource https://cognitiveservices.azure.com --query accessToken -o tsv; $script:tokenAt = Get-Date; Log "token refreshed" }
Refresh-Token

$done = 0; $skip = 0; $fail = 0
foreach ($it in $items) {
  $path = Join-Path $outDir "$($it.id).jpg"
  if (Test-Path $path) { $skip++; continue }
  if (((Get-Date) - $tokenAt).TotalMinutes -gt 40) { Refresh-Token }
  $style = $regionMap[$it.region]
  $stylePart = if ($style) { " in $style style" } else { "" }
  $prompt = "Generate a realistic high-quality food photo of $($it.name), an Indian dish served in authentic Indian style$stylePart, top-down or slight angled view, natural plating on traditional tableware, soft natural lighting, appetising but realistic, no text, no branding, no people."
  $body = @{ model="gpt-image-2"; prompt=$prompt; n=1; size="${Size}x${Size}" } | ConvertTo-Json

  $attempt = 0; $ok = $false
  while (-not $ok -and $attempt -lt 10) {
    $attempt++
    $headers = @{ "Authorization"="Bearer $token"; "Content-Type"="application/json" }
    try {
      $resp = Invoke-RestMethod -Uri $uri -Method Post -Headers $headers -Body $body -TimeoutSec 180
      $b64 = $resp.data[0].b64_json
      if (-not $b64) { Log "no b64 $($it.id)"; break }
      $bytes = [Convert]::FromBase64String($b64)
      $ms = New-Object System.IO.MemoryStream(,$bytes)
      $img = [System.Drawing.Image]::FromStream($ms)
      $bmp = New-Object System.Drawing.Bitmap $Target, $Target
      $g = [System.Drawing.Graphics]::FromImage($bmp)
      $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
      $g.DrawImage($img, 0, 0, $Target, $Target)
      $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Jpeg)
      $g.Dispose(); $bmp.Dispose(); $img.Dispose(); $ms.Dispose()
      $done++; $ok = $true
      Log "OK $($it.id) ($done generated, $skip skipped)"
      Start-Sleep -Seconds 8   # base pacing between successes
    } catch {
      $emsg = "$($_.Exception.Message)"
      if ($emsg -match "401|expired") { Refresh-Token; continue }
      if ($emsg -match "429|Too Many") {
        $wait = [Math]::Min(20 + $attempt * 10, 75)
        Log "429 $($it.id) attempt $attempt -> wait ${wait}s"
        Start-Sleep -Seconds $wait
      } else {
        Log "ERR $($it.id): $emsg"; Start-Sleep -Seconds 5
      }
    }
  }
  if (-not $ok) { $fail++; Log "GIVEUP $($it.id)" }
}
Log "DONE generated=$done skipped=$skip failed=$fail total=$($items.Count)"
