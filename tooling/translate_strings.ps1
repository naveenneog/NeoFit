# Translates res/values/strings.xml into native Indian languages via Azure GPT,
# writing res/values-<code>/strings.xml. Missing keys safely fall back to English.
$ErrorActionPreference = 'Stop'
$root = "C:\Users\navg\DailyApps\NeoFit"
$src = Get-Content "$root\app\src\main\res\values\strings.xml" -Raw
$endpoint = "https://ai-contosohub530569751908.cognitiveservices.azure.com"
$token = az account get-access-token --resource https://cognitiveservices.azure.com --query accessToken -o tsv
$headers = @{ "Authorization"="Bearer $token"; "Content-Type"="application/json" }
$uri = "$endpoint/openai/deployments/gpt-4o-mini/chat/completions?api-version=2024-10-21"

$langs = @(
  @{ code="hi"; name="Hindi";    script="Devanagari" },
  @{ code="kn"; name="Kannada";  script="Kannada"    },
  @{ code="ta"; name="Tamil";    script="Tamil"      },
  @{ code="te"; name="Telugu";   script="Telugu"     },
  @{ code="mr"; name="Marathi";  script="Devanagari" },
  @{ code="bn"; name="Bengali";  script="Bengali"    }
)

foreach ($l in $langs) {
  $sys = "You are a professional mobile-app localizer. Translate an Android strings.xml into natural, native $($l.name) ($($l.script) script). Rules: translate ONLY the human-readable text inside each string element; keep every name attribute exactly the same; keep app_name as Neo Fit; keep emojis and units (kg, kcal, g, cm, BMI) sensible; preserve XML escaping (use &apos; for apostrophes and &amp; for ampersand). Render any Hinglish Latin words in the native script. Output ONLY the full strings.xml starting with the xml declaration and nothing else."
  $body = @{
    messages = @(
      @{ role="system"; content=$sys },
      @{ role="user"; content=$src }
    )
    temperature = 0.3
    max_tokens = 4000
  } | ConvertTo-Json -Depth 6

  Write-Output "Translating -> $($l.name) ($($l.code)) ..."
  $resp = Invoke-RestMethod -Uri $uri -Method Post -Headers $headers -Body $body -TimeoutSec 120
  $xml = $resp.choices[0].message.content
  # strip markdown fences if any
  $xml = $xml -replace '```xml','' -replace '```',''
  $xml = $xml.Trim()
  if ($xml -notmatch '^<\?xml|^<resources') { Write-Output "  ! unexpected output for $($l.code), skipping"; continue }
  if ($xml -notmatch '<resources') { Write-Output "  ! no <resources> for $($l.code), skipping"; continue }
  $dir = "$root\app\src\main\res\values-$($l.code)"
  New-Item -ItemType Directory -Force -Path $dir | Out-Null
  Set-Content -Path "$dir\strings.xml" -Value $xml -Encoding UTF8
  $count = ([regex]::Matches($xml,'<string ')).Count
  Write-Output "  wrote values-$($l.code)/strings.xml ($count strings)"
}
Write-Output "Translation done."
