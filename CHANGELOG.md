# Changelog & Decision Log — Neo Fit

A running record of what was built and key decisions/approaches (including pitfalls fixed), so the
work is traceable and we don't repeat dead-ends.

## [1.1.0] — AI media, languages & smarter food

### Delivered (all verified on the Android 14 emulator)
- **22 scheduled Indian languages** + English + Hinglish/Kanglish/Tanglish, with **working runtime
  language switching** (AppCompat per-app locales) — fixed the earlier broken switching. UI
  strings translated via Azure GPT (gpt-4o-mini) into all scripts (Devanagari, Kannada, Tamil,
  Telugu, Bengali, Gurmukhi, Odia, Malayalam, Gujarati, Perso-Arabic, Ol Chiki, …). Verified: full
  Kannada dashboard.
- **Real gpt-image-2 food photos** for the dish library, downscaled and **bundled in app assets** —
  shown across search/dashboard/log (idli, dosa, sambar, biryani, …).
- **Sora-2 exercise videos**: short looping demos generated per exercise, published as a GitHub
  release and **streamed in the workout runner** (Media3 ExoPlayer → TextureView, muted, looping,
  with image fallback). Verified playing on-device.
- **On-device food recognition** via Google ML Kit image labeling (offline, bundled), mapped onto
  the Indian knowledge base — replaces the mock; keeps confirm-before-log. Verified on a real photo.
- **Flexitarian + "veg days"**: new diet option and per-user vegetarian weekdays; suggestions go veg
  on those days (dashboard shows a veg-day badge).
- New dishes: **Ragi Mudde** and **Jowar Roti** (millet staples, with native names).

### Key decisions
- **Locale switching** moved from a manual `attachBaseContext` wrap to
  `AppCompatDelegate.setApplicationLocales` (+ `AppLocalesMetadataHolderService` autoStoreLocales);
  `MainActivity` is now an `AppCompatActivity`. Reliable across API levels.
- **`disableLocalAuth=true`** on the Azure resource → the app cannot use API keys; only AAD/Managed
  Identity works. So GPT/image/video are used at **build/tooling time** (translations, food photos,
  exercise videos) and bundled/streamed. A live in-app GPT assistant would need a backend proxy
  (Managed Identity) — noted as a TODO.
- **Food recognition**: Python 3.14 on the box can't host TensorFlow, so instead of training a
  custom net we use ML Kit's bundled model + knowledge-base mapping. The generated gpt-image-2
  photos are kept as a labelled reference set to train a custom Indian-food TFLite later.
- Exercise videos are **streamed from a GitHub release**, not bundled, to keep the APK installable.

### Tooling added
- `tooling/translate_strings.py` — GPT translation of strings.xml into all 22 languages.
- `tooling/generate_food_images.ps1` — gpt-image-2 dish photos (429 backoff) → assets.
- `tooling/generate_exercise_videos.py` — Sora-2 videos (concurrent, 429 backoff) → release.

### Pitfalls fixed
- AAPT "invalid escape" from unescaped apostrophes in generated translations → post-process escape.
- gpt-image-2 rate limits (429, capacity 2) → retry/backoff; Sora-2 transient "deployment not ready"
  404s right after deploy → re-runnable generator.
- `adb screencap` renders SurfaceView video as black → switched ExoPlayer output to TextureView
  (also disabled the unsupported 96 kHz AAC audio track).

### Known gaps / next
- Some non-resource UI strings remain English (a few button labels, exercise seed content).
- A handful of exercise videos failed on the fresh sora-2 deploy (re-runnable).
- APK is ~65 MB (bundled photos + ML Kit + Media3 native libs) — trim via ABI splits/minify.
- Live GPT assistant pending a Managed-Identity backend proxy.

## [1.0.0] — Initial MVP (one-shot build)

### Delivered
- Full Android app (Kotlin, Compose, MVVM + Clean Architecture) for Indian-food-aware calorie,
  activity and wellness tracking. 15 screens, 7 workout plans, ~60-dish knowledge base.
- Engines: BMR/TDEE (Mifflin–St Jeor), calorie estimation (portion × cooking × confidence), region
  classifier, transparent wellness score, recommendation nudges.
- Local-first Room storage; Hilt DI; Health Connect with estimate/simulate fallback; Azure
  `gpt-image-2` image generation behind a provider chain; mock food recogniser; multilingual with
  native dish names.
- Tests: 5 engine unit-test classes + 2 instrumentation tests (Room round-trip, Compose smoke).

### Key decisions
- **Single Gradle module, package-separated** (not multi-module) to maximise "compiles in one shot";
  package boundaries mirror clean-architecture layers so a later split is mechanical.
- **Engines kept framework-free** (no Hilt annotations) and provided via `EngineModule` — keeps them
  trivially unit-testable.
- **Calorie values surfaced as approximate** (`~`, confidence chips, basis text) rather than implying
  false precision — core product principle.
- **Azure auth via API key** in `BuildConfig` from `local.properties` (no secrets committed); AAD /
  Managed Identity proxying flagged as a production TODO.
- **Image generation is on-demand** (detail/runner), lists use gradient placeholders, to avoid
  unnecessary API spend.
- **Locale**: manual `attachBaseContext` wrapping via `LocaleManager` (reliable with
  `ComponentActivity`) + `LocalAppLanguage` for dynamic content; EN/HI string resources, native dish
  names for all languages.

### Build pitfalls fixed during development
- `inline` on a sealed-interface member (`DataResult.map`) is prohibited → made it a normal member.
- `Json.encodeToString` / `decodeFromString` resolved to the wrong overload → added explicit
  `import kotlinx.serialization.encodeToString` / `decodeFromString`.
- Removed an unresolved Health Connect opt-in compiler arg (marker not in the alpha artifact).
- `com.google.truth` was only on the unit-test classpath → added to `androidTestImplementation`.
- Reverted custom `testInstrumentationRunner` to the standard `AndroidJUnitRunner` (no Hilt in
  instrumentation tests).
- Dashboard header overlapped the status bar → use `contentPadding.calculateTopPadding()` (edge-to-edge inset).

### Verified on emulator (Android 14 / API 34, x86_64)
- Onboarding (multilingual, validation) → Home dashboard (targets, simulated steps fallback, macros,
  wellness, hydration, recommendations).
- Food search → estimate (portion/cooking recompute + confidence downgrade + `~` prefix) → save →
  persisted & grouped in the food log.
- Exercise plan → workout runner: live countdown timer, step image placeholder, instructions,
  calorie accrual, **TTS voice connected**.
- Unit tests + instrumentation tests green.

### Notes / environment
- Health Connect APK is absent on the test emulator → exercised the estimate/simulate fallback path.
- The emulator was shared with another user's app during final checks; did not interfere with it.
