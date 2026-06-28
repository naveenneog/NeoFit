# Neo Fit — Architecture & Restart Context

> Single source of truth for **design, architecture, AI-model access, and the build/release
> workflow**, so work can be resumed without losing context. Feature list lives in
> [`README.md`](../README.md); the running change log lives in [`CHANGELOG.md`](../CHANGELOG.md);
> the on-device coach design lives in [`AI_COACH.md`](AI_COACH.md).

## 1. Snapshot (current state)

| | |
|---|---|
| Version | **v1.3.1** (`versionCode 5`) |
| Repo | `C:\Users\navg\DailyApps\NeoFit` · package `com.neofit` · single Gradle module `:app` |
| Min / target | Android 8.0 (API 26) + |
| Distribution | GitHub release (`NeoFit-v1.3.1.apk`, debug-signed, installable) + GitHub Pages (`docs/`) |
| License | PolyForm Noncommercial 1.0.0 |
| Web | https://naveenneog.github.io/NeoFit/ |

## 2. Product

A science-informed Indian health & fitness app: log diverse Indian foods (home / street / thali /
regional, **107 dishes** veg & non-veg), track calories in/out, follow goal-based workouts with
demo videos, and watch weight/wellness trends. **Offline-first, privacy-first, transparent about
estimation.** UI localised into **22 Indian languages**.

## 3. Tech stack

Kotlin · Jetpack Compose (Material 3) · MVVM · Hilt (DI) · Room (persistence) ·
Coroutines/Flow · OkHttp + kotlinx.serialization (REST) · ML Kit Image Labeling (on-device food
recognition) · Media3/ExoPlayer (workout videos).

## 4. Architecture (clean, layered)

```
com.neofit
├── core          cross-cutting: common, designsystem (theme/colours), i18n (22-lang strings), util
├── data          local (Room: entity, dao, converter), remote, repository, seed (knowledge bases)
├── domain         model (entities/enums), repository (interfaces), usecase (business rules)
├── di            Hilt modules
├── engine         calorie / wellness / goal computation
├── integration    ai (image gen, ML Kit food recognition), health (Health Connect)
└── feature        Compose screens + ViewModels: onboarding, dashboard, foodlog, exercise,
                    insights, progress, profile, navigation, common
```

UI (feature) → UseCase (domain) → Repository (domain interface, data impl) → Room/seed/remote.
ViewModels expose `StateFlow`; screens are stateless Composables. Targets (calorie/protein/etc.)
always flow through `ComputeGoalUseCase` so Dashboard, Insights and Profile agree (root cause of an
earlier mismatch: `LogWeightUseCase` updated `currentWeightKg` but not a stored target snapshot).

## 5. AI model integration — Azure AI Foundry  ⭐ (restart-critical)

**Resource (user's own Azure AI Foundry):**
- Foundry project: `https://ai-contosohub530569751908.services.ai.azure.com/api/projects/ai-contosohub5305697519-project`
- Cognitive Services endpoint: `https://ai-contosohub530569751908.cognitiveservices.azure.com`
- **Auth (tooling):** `az account get-access-token --resource https://cognitiveservices.azure.com --query accessToken -o tsv` → `Authorization: Bearer <token>` (token cached ~40 min, refresh on 401/403).
- **Auth (in-app):** `api-key: <AZURE_OPENAI_API_KEY>` header. Keys come from `local.properties`
  (gitignored) → `BuildConfig` (see §7). **Never commit keys.** Endpoint/deployment names are not secret.
- Roadmap: add `DefaultAzureCredential` / Managed Identity as the default auth (preferred), with
  pluggable api-key config for other users.

### 5.1 gpt-image-2 — food photos
- REST: `POST {endpoint}/openai/deployments/gpt-image-2/images/generations?api-version=2025-04-01-preview`
- Body: `{ "model":"gpt-image-2", "prompt":<prompt>, "n":1, "size":"1024x1024" }` → resp `data[0].b64_json` (base64 PNG).
- **In-app:** `integration/ai/ImageGeneration.kt` → `AzureImageGenerationService` (api-key). If not
  configured, `isConfigured()`=false and the provider chain falls back to bundled asset → web → local
  placeholder, so the app stays usable.
- **Offline batch:** `tooling/generate_food_images.ps1` (Bearer) — generates one photo per dish in
  `FoodKnowledgeBase.kt`, downsizes to JPEG, bundles into `app/src/main/assets/food/<id>.jpg`.
- Prompt builder: `PromptBuilder.foodPhoto(dishName, region)`.

### 5.2 sora-2 — exercise demo videos  (async job API)
- Create: `POST {endpoint}/openai/v1/videos?api-version=preview` body `{ "model":"sora-2", "prompt":<p>, "seconds":"8", "size":"1280x720" }` → `{ "id": ... }`
- Poll: `GET {endpoint}/openai/v1/videos/{id}?api-version=preview` → `status` ∈ `completed|failed`
- Download: `GET {endpoint}/openai/v1/videos/{id}/content?api-version=preview` → mp4 bytes
- **Offline batch:** `tooling/generate_exercise_videos.py` (concurrency 4, 429 backoff, token
  refresh). Output mp4s are published as the GitHub release **`exercise-videos`**; the app streams
  `VIDEO_BASE/<id>.mp4` on demand (`ExerciseLibrary.kt` `VIDEO_BASE`, one clip per exercise id).
- Player: `feature/exercise/ExerciseVideo.kt`. ExoPlayer is keyed on `remember(videoUrl)`; the
  `AndroidView` needs **both** `factory` **and** `update = { it.player = player }` or the PlayerView
  keeps the first (released) player and clips look frozen / don't advance.

### 5.3 gpt-4o-mini — string translation
- Chat-completions on the same endpoint; offline via `tooling/translate_missing.py`,
  `translate_strings.py` (+ `diff_missing.py`, `fix_apostrophes.py`). Weak at Ol Chiki (Santali).

### 5.4 ML Kit — on-device food recognition
- `integration/ai/MlKitFoodRecognitionService.kt` — runs fully on-device (no cloud), maps labels to
  `FoodKnowledgeBase` entries.

## 6. Data & domain

- **Room** db `neofit.db` (key cols: `caloriesKcal`, `timestampEpochMillis`, `portionLabel`).
  Inspect: `adb shell run-as com.neofit sqlite3 -header /data/data/com.neofit/databases/neofit.db "<sql>"`.
- **`data/seed/FoodKnowledgeBase.kt`** — 107 dishes with per-serving kcal/macros, region (SOUTH /
  NORTH / EAST / WEST / …), diet (veg / non-veg / **both**, for mixed-diet users), portion sizes
  (HALF_KATORI, KATORI=150 g, PLATE, PIECE, GLASS, BOWL), and authentic native names
  (Tamil/Telugu/Kannada/Malayalam/Hindi/…). Native names must be authentic — never invented.
- **`data/seed/ExerciseLibrary.kt`** — goal-based, mostly no-equipment plans; each item has steps,
  TTS voice cue, MET (calorie), image prompt, and `videoUrl`.
- **`engine/`** — calorie estimation, wellness score, `ComputeGoalUseCase` (Mifflin-style targets).

## 7. Build / test / release workflow

Environment (nothing on PATH — set per shell):
- `JAVA_HOME = C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot`
- Android SDK = `%LOCALAPPDATA%\Android\Sdk`; adb = `…\Sdk\platform-tools\adb.exe`
- Secrets via `local.properties` → `BuildConfig` (`build.gradle.kts` reads `local.properties` then
  env): `AZURE_OPENAI_ENDPOINT` (default the cognitiveservices URL), `AZURE_OPENAI_API_KEY`,
  `AZURE_IMAGE_DEPLOYMENT_NAME` (default `gpt-image-2`), `AZURE_API_VERSION` (default
  `2025-04-01-preview`), optional `AZURE_TRANSLATOR_*`.

Commands:
```
.\gradlew.bat :app:assembleDebug          # ~17-22 s
.\gradlew.bat :app:testDebugUnitTest      # ~12 s
.\gradlew.bat :app:assembleRelease        # ~1-3 min -> app/build/outputs/apk/release/app-release.apk (debug-signed, 65.5 MB)
```
Release: bump `versionCode`/`versionName` in `app/build.gradle.kts` → `assembleRelease` → stage as
`NeoFit-vX.Y.Z.apk` → `gh release create vX.Y.Z … <apk>` (or `gh release upload vX.Y.Z <apk>
--clobber` to replace) → update `docs/index.html` download links (`releases/latest/download/
NeoFit-vX.Y.Z.apk`, filename must match) → push (Pages rebuild ~1 min). Verify link HTTP 200.
Adaptive launcher icon: `res/drawable/ic_launcher_*` (foreground heart + orange split background);
verify by rendering the vector + group transform (heart centre must map to 54,54 in the 108 viewport).

## 8. Emulator coordination (shared AVD)

One AVD `actioncut_test` is **shared** across DailyApps builds (com.neofit, com.kidkat.kidkat,
com.pier36.pier_36, PrimeBeats, ActionCut). Use **`tooling/emulator_guard.ps1`**
(`status|foreground|acquire|heartbeat|release|wait`): foreground via `topResumedActivity`, plus a
cooperative TTL lock `/data/local/tmp/dailyapps_emu.lock`. **Rule: gate every emulator action on
`acquire` returning exit 0** (0=free/acquired, 2=busy/refused, 3=no device); never steal focus.
Always `release` when testing is done. Verify video playback (not the static fallback) by comparing
two screenshots ~1.5 s apart for pose change.

## 9. Tooling index (`tooling/`)

| Script | Purpose |
|---|---|
| `generate_food_images.ps1` | gpt-image-2 → bundled food JPEGs |
| `generate_exercise_videos.py` | sora-2 → exercise mp4s (→ `exercise-videos` release) |
| `translate_missing.py` / `translate_strings.py` / `diff_missing.py` / `fix_apostrophes.py` | gpt-4o-mini i18n |
| `emulator_guard.ps1` | shared-emulator lock/coordination |

## 10. Change history

See [`CHANGELOG.md`](../CHANGELOG.md). Recent: **v1.3.1** per-exercise workout-video fix, +37 South
Indian dishes, centred launcher icon, emulator-guard tooling · **v1.3.0** UX-friction pass, charts,
full 22-language localisation.

## 11. Known open / deferred

- Deeper i18n: enum `.label` values (diet/goal/region/sex/activity) and `ComputeGoalUseCase`
  rationale strings are still English.
- Open `qa_findings` (mostly medium/low) tracked in the session DB.
- Auth roadmap: DefaultAzureCredential/Managed Identity as default (§5).
