# Changelog & Decision Log — Neo Fit

A running record of what was built and key decisions/approaches (including pitfalls fixed), so the
work is traceable and we don't repeat dead-ends.

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
