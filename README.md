# Neo Fit 🥗

**A science-informed health & fitness app tailored to Indian dietary habits and lifestyle.**

Neo Fit helps Indian users track calorie intake & burn, log diverse Indian foods (home, street,
thali, regional), follow simple goal-based workouts, and watch their weight & wellness trend —
all **offline-first**, **privacy-first**, and **transparent about estimation**.

> ⚠️ Neo Fit supports general fitness and wellness. It does **not** replace professional medical or
> nutrition advice. Calorie values for many Indian dishes are approximations, and the app says so.

<p align="center">
  <a href="https://github.com/naveenneog/NeoFit/releases/latest/download/NeoFit-v1.1.0.apk"><img alt="Download APK" src="https://img.shields.io/badge/Download-APK%20v1.1.0-FF7A1A?style=for-the-badge&logo=android&logoColor=white"></a>
  <a href="https://naveenneog.github.io/NeoFit/"><img alt="Website" src="https://img.shields.io/badge/Website-GitHub%20Pages-1B998B?style=for-the-badge&logo=githubpages&logoColor=white"></a>
  <a href="LICENSE"><img alt="License" src="https://img.shields.io/badge/License-PolyForm%20Noncommercial%201.0.0-E8505B?style=for-the-badge"></a>
</p>

- 🌐 **Website:** https://naveenneog.github.io/NeoFit/
- ⬇️ **Latest APK:** [NeoFit-v1.1.0.apk](https://github.com/naveenneog/NeoFit/releases/latest/download/NeoFit-v1.1.0.apk) · Android 8.0 (API 26)+
- 📜 **License:** [PolyForm Noncommercial 1.0.0](LICENSE) — open-source, **free for non-commercial use**.

---

## Table of contents
- [Highlights](#highlights)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Module / package layout](#module--package-layout)
- [Screens](#screens)
- [Data models](#data-models)
- [How calorie estimation works](#how-calorie-estimation-works)
- [Regional classification](#regional-classification)
- [Wellness score](#wellness-score)
- [Food image system (web → Azure → placeholder)](#food-image-system)
- [Multilingual / native food names](#multilingual--native-food-names)
- [Setup](#setup)
- [Configuring Health Connect](#configuring-health-connect)
- [Configuring Azure image generation](#configuring-azure-image-generation)
- [Testing](#testing)
- [Assumptions](#assumptions)
- [Known limitations](#known-limitations)
- [Future improvements / production TODOs](#future-improvements--production-todos)

---

## Highlights
- **Indian food intelligence**: a seeded knowledge base spanning regions (South, North, North-East,
  West, Central, East, pan-Indian), home meals, street food, thalis/combos, beverages, snacks and
  sweets — each with native names and per-serving nutrition.
- **Honest calorie estimation**: portion multipliers + cooking-style adjustment + confidence
  scoring (`High` / `Medium` / `Rough estimate`) and a human-readable basis. Approximate values are
  prefixed with `~`. Users can correct anything.
- **Activity sync with graceful fallback**: reads steps/distance/active-calories from **Health
  Connect** when available; otherwise estimates/simulates so the dashboard stays useful.
- **Goal-based workouts**: 7 plans with step-by-step instructions, a live timer, **voice guidance
  (TTS)**, calorie accrual, and **on-demand generated pose images**.
- **Weight & transparent wellness score** (0–100) you can see the breakdown of.
- **Recommendations & nudges** (e.g. “You’re short on protein today”, “You’ve already walked 70%”).
- **Multilingual** with native dish names and Hinglish/Kanglish flavour.
- **Offline-first**: Room is the source of truth; network/AI sit behind interfaces with mocks.

## Tech stack
Kotlin · Jetpack Compose (Material 3) · MVVM + Clean Architecture · Room · Hilt · Coroutines + Flow ·
DataStore · Retrofit/OkHttp + kotlinx.serialization · Coil · Health Connect · Camera/Photo Picker ·
JUnit + Truth (unit) · AndroidX Test + Compose UI Test (instrumentation).

- AGP 8.7.3 · Kotlin 2.0.21 · KSP · Compose BOM 2024.12.01 · Hilt 2.52 · Room 2.6.1
- `compileSdk 35` · `minSdk 26` · `targetSdk 35`

## Architecture
Clean Architecture with a unidirectional data flow:

```
 UI (Compose screens) ──► ViewModel (StateFlow) ──► UseCase ──► Repository (interface)
                                                                   │
                                          ┌────────────────────────┼───────────────────────┐
                                          ▼                        ▼                        ▼
                                   Room (local, SoT)        Integrations (Health      Engines (pure
                                                            Connect, Azure AI,         Kotlin: estimation,
                                                            recogniser mock)           region, wellness, reco)
```

- **Domain** is pure Kotlin: models, repository interfaces, use cases. No Android/framework deps.
- **Engines** (`engine/`) are pure, deterministic and unit-tested: `CalorieMath`,
  `CalorieEstimationEngine`, `RegionClassifier`, `WellnessScoreEngine`, `RecommendationEngine`.
- **Data** implements repositories over Room DAOs and in-memory seed libraries, mapping
  entities ↔ domain models.
- **Integrations** wrap external/AI concerns behind interfaces:
  `HealthConnectManager`, `ImageGenerationService` (+ `AzureImageGenerationService`),
  `FoodImageProvider` (`WebImageProvider`, `AzureGeneratedImageProvider`), `FoodRecognitionService`
  (`MockFoodRecognitionService`).
- **DI** (Hilt) wires it all in `di/` (`DatabaseModule`, `NetworkModule`, `EngineModule`,
  `RepositoryModule`).

### Why a single Gradle module?
The brief lists feature *modules*. We implement strict **package** separation
(`core`, `data`, `domain`, `engine`, `integration`, `feature/*`, `di`) inside a **single Gradle
module**. This maximises "compiles in one shot with minimal manual work" while preserving
clean-architecture boundaries. Splitting into Gradle modules later is mechanical (the package
boundaries already match) — see TODOs.

## Module / package layout
```
app/src/main/java/com/neofit/
├── NeoFitApp.kt                 # @HiltAndroidApp
├── MainActivity.kt             # edge-to-edge, theme, locale, NavGraph host
├── core/
│   ├── designsystem/           # Material3 theme, colors, type, dimens
│   ├── i18n/                   # LocaleManager, LocalAppLanguage
│   ├── common/                 # UiState, DataResult
│   └── util/                   # DateUtil, Format
├── domain/
│   ├── model/                  # UserProfile, FoodItem, MealLog, ExercisePlan, …
│   ├── repository/             # repository interfaces
│   └── usecase/                # ComputeGoal, GetDashboard, LogMeal, EstimateMeal, …
├── engine/                     # pure estimation/region/wellness/recommendation engines
├── data/
│   ├── local/                  # Room DB, entities, DAOs, converters, mappers
│   ├── repository/             # repository implementations
│   └── seed/                   # FoodKnowledgeBase, ExerciseLibrary
├── integration/
│   ├── health/                 # HealthConnectManager
│   └── ai/                     # image generation, providers, recognition mock, prompt builder
├── di/                         # Hilt modules
└── feature/
    ├── onboarding/ dashboard/ foodlog/ exercise/ progress/ profile/ insights/
    ├── common/                 # shared Compose components
    └── navigation/             # routes + NavGraph
```

## Screens
Splash · Onboarding (7 steps) · Home dashboard · Food log · Add meal · Meal search ·
Camera/photo food log · Meal detail · Exercise plans · Exercise detail (runner) · Progress ·
Weight history · Profile/settings · Region & preferences · Insights.

## Data models
`UserProfile`, `Goal`, `FoodItem`, `MealLog`, `NutritionEstimate`, `FoodRegion`, `PortionSize`,
`ExercisePlan`, `ExerciseItem`, `WorkoutSession`, `StepSummary`, `WeightEntry`, `WellnessSummary`,
`FoodImageAsset`, `Recommendation`, `SyncStatus`, plus the aggregate `DashboardSummary`.

## How calorie estimation works
`CalorieEstimationEngine` turns a knowledge-base `FoodItem` + chosen `PortionSize` (+ optional
cooking-style override) into a `NutritionEstimate`:

1. **Base lookup** – per-serving calories/macros from `FoodKnowledgeBase`.
2. **Portion multiplier** – `relativePortion = chosenPortion.multiplier / baseServing.multiplier`.
3. **Cooking adjustment** – if the user overrides the cooking method, calories (and fat) scale by
   `override.factor / baseStyle.factor` (e.g. frying ≈ ×1.4).
4. **Confidence scoring** – starts from the food's `baseConfidence`, downgraded for street
   food/combos, unusual portions, or a cooking override. Surfaced as `High`/`Medium`/`Rough estimate`.
5. **Transparency** – a plain-language `basis` string and an `isApproximate` flag (UI shows `~`).
6. **Manual correction** – an entered calorie override is trusted and flagged.

`BMR`/`TDEE`/targets use the **Mifflin–St Jeor** equation (`CalorieMath`), with safe deficit/surplus
caps and a hard calorie floor.

## Regional classification
`RegionClassifier` infers a broad regional food profile from your logged dishes. It is **advisory,
never restrictive**, fully overridable, and **explains itself** ("Detected mostly South India dishes
(3 of 4 regional meals)…"). Pan-Indian/neutral dishes carry no regional signal.

## Wellness score
`WellnessScoreEngine` produces a transparent 0–100 score = **Consistency 25 + Activity 25 + Calorie
adherence 30 + Workout 20**. The Insights screen shows each component so the number is never a black box.

## Food image system
`ImageRepositoryImpl` resolves a dish/exercise image through a provider chain and caches the result:

1. **WebImageProvider** – returns a known image URL if the food ships one *(plug a real image-search
   API here — see TODO)*.
2. **AzureGeneratedImageProvider** – calls the configured Azure model (`gpt-image-2`) with a prompt
   from `PromptBuilder`.
3. **Placeholder** – a colourful gradient tile with the dish initials (always available).

Lists use placeholders (cheap); generation is triggered on explicit actions (meal detail, workout
runner) to avoid unnecessary API calls.

## Multilingual / native food names
- Language is chosen during onboarding and changeable in Profile.
- `FoodItem.localizedNames` holds **authentic native dish names** (never invented words), e.g.
  इडली / ಇಡ್ಲಿ / இட்லி. Romanized blends (Hinglish/Kanglish/Tanglish) reuse the already-native
  `nameEn` (e.g. "Rajma Chawal").
- Android UI strings are localised for **English** and **Hindi**; other languages fall back to
  English UI while still showing native dish names. Runtime translation of remaining UI is wired as
  an Azure AI Translator placeholder (config in `local.properties`).
- `LocaleManager` applies the resource locale at startup; `LocalAppLanguage` drives dynamic content.

## Setup
**Prerequisites:** Android Studio (Koala+), JDK 17, Android SDK 35.

```bash
# 1. Open the project in Android Studio (or use the wrapper)
# 2. Ensure local.properties has your SDK path (Android Studio writes this automatically):
#    sdk.dir=/path/to/Android/Sdk
# 3. Build & install on a device/emulator (API 26+):
./gradlew :app:installDebug
# or
./gradlew :app:assembleDebug
```

The app opens into **onboarding**, then the **home dashboard**. Seed data (Indian foods, workout
plans) is bundled — no backend required.

## Configuring Health Connect
- Permissions for steps, distance, active/total calories and exercise are declared in the manifest,
  plus the permission-rationale intent filters.
- On a device **with Health Connect** installed, the app reads today's steps/distance/active calories
  via `HealthConnectManager` and shows source `health connect`.
- On a device **without** Health Connect (e.g. many emulators), reads return null and the app
  **estimates/simulates** steps so the dashboard remains demonstrable (source `estimated`).
- To grant permissions on a real device: install “Health Connect”, then accept the permission
  request the app triggers on first activity sync. (A first-class permission-request button is a
  TODO; the plumbing/contract already exists in `HealthConnectManager`.)

## Configuring Azure image generation
Credentials are read from `local.properties` (or environment variables) into `BuildConfig` — **no
secrets are committed**. Defaults already point at a verified-working Foundry deployment; supply a
key to enable generation:

```properties
# local.properties
AZURE_OPENAI_ENDPOINT=https://<your-resource>.cognitiveservices.azure.com
AZURE_OPENAI_API_KEY=<your-key>           # leave blank to disable generation (falls back to placeholder)
AZURE_IMAGE_DEPLOYMENT_NAME=gpt-image-2
AZURE_API_VERSION=2025-04-01-preview
```

The call uses the **deployment-path** Images API
(`POST {endpoint}/openai/deployments/{deployment}/images/generations?api-version=...`) with the
`api-key` header and parses `b64_json`. With no key configured, the app is fully usable and shows
gradient placeholders.

> **Auth note:** the in-app client uses API-key auth (simplest for a mobile build). Production
> deployments should proxy through a backend using Azure AD / Managed Identity
> (DefaultAzureCredential) rather than shipping a key — see TODOs.

## Testing
```bash
./gradlew :app:testDebugUnitTest            # engine unit tests (JUnit + Truth)
./gradlew :app:connectedDebugAndroidTest    # Room round-trip + Compose smoke (needs a device/emulator)
```
- **Unit:** `CalorieMath`, `CalorieEstimationEngine`, `RegionClassifier`, `WellnessScoreEngine`,
  `RecommendationEngine`.
- **Instrumentation:** Room persistence (`NeoFitDatabaseTest`) and a Compose render smoke test
  (`ComposeSmokeTest`).

This build has been validated end-to-end on an Android 14 (API 34) emulator: onboarding → dashboard
→ search → estimate → log → exercise runner (timer + TTS), with unit + instrumentation suites green.

## Assumptions
- **Single user / single profile** (`id = 1`). Multi-profile/account sync is out of scope for the MVP.
- **Single Gradle module** with package separation (see rationale above).
- **Calorie values are approximate** for most Indian dishes by design; the app communicates this.
- **Food recognition is a mock** that returns confirmable candidates — we never auto-log a guess.
- **Water/hydration** is a simple per-day glass counter (placeholder card on the dashboard).
- **Steps may be simulated** when Health Connect is unavailable so the experience is demonstrable.
- English & Hindi UI strings are translated; other languages fall back to English UI with native
  dish names.

## Known limitations
- No real food-image recognition model (mock + manual confirmation).
- Web image provider only resolves URLs already present in the data (no live image search yet).
- Health Connect permission flow relies on the system prompt; no dedicated in-app rationale screen.
- Nutrition data is curated/approximate, not a certified database.
- No cloud backup/sync; data is local to the device.

## Future improvements / production TODOs
- [ ] Split packages into real Gradle feature modules (boundaries already align).
- [ ] Replace `MockFoodRecognitionService` with an on-device (ML Kit/TFLite) or cloud vision model.
- [ ] Add a licensed image-search provider for `WebImageProvider`.
- [ ] Back Azure image/translation calls with a server using **Azure AD / Managed Identity**.
- [ ] First-class Health Connect permission & onboarding screen; write-back of workouts.
- [ ] Expand the food knowledge base and add per-ingredient composition estimates.
- [ ] Full UI localisation for all supported languages (wire `AzureTranslator`).
- [ ] Room migrations (currently destructive fallback for the MVP).
- [ ] Cloud sync/backup, widgets, notifications/reminders, accessibility audit, ProGuard tuning.

---

_Product name “Neo Fit” is a placeholder. Built as a production-ready MVP foundation._
