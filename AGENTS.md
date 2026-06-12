# Repository Guidelines

## Project Structure & Module Organization

Lokacara is a single-module Android project. The app code lives in `app/src/main/java/com/app/lokacara`, grouped by role: `ui/screens`, `ui/components`, `ui/navigation`, `ui/theme`, `viewmodel`, `repository`, `model`, `data`, `data/remote`, `data/remote/dto`, and `di`. Android resources are in `app/src/main/res`, including drawables, fonts, launcher icons, XML rules, and values. Unit tests belong in `app/src/test/java`; instrumented and Compose UI tests belong in `app/src/androidTest/java`. API and backend notes are in `docs/`; historical OpenSpec work is under `openspec/changes/archive`.

## Build, Test, and Development Commands

Use the Gradle wrapper from the repository root:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

`assembleDebug` builds a local debug APK. `testDebugUnitTest` runs JVM tests. `connectedDebugAndroidTest` runs device/emulator tests and requires an attached Android device or emulator. `lintDebug` checks Android lint issues. In Android Studio, open the root project and run the `app` configuration for interactive development.

## Coding Style & Naming Conventions

Use Kotlin with Java 17 compatibility and Jetpack Compose. Follow existing package boundaries: screens render UI, ViewModels own UI state, repositories handle data access, DTOs stay in `data/remote/dto`, and Hilt bindings belong in `di`. Use 4-space indentation, `PascalCase` for classes, composables, ViewModels, and data classes, and `camelCase` for functions and properties. Name screen files like `HomeScreen.kt`, components like `EventCard.kt`, repositories like `HomeRepository.kt`, and ViewModels like `HomeViewModel.kt`.

## Testing Guidelines

The project uses JUnit for local tests and AndroidX JUnit, Espresso, and Compose UI testing for instrumented tests. Add unit tests near the behavior being changed in `app/src/test/java/com/app/lokacara`; add UI or Android framework tests in `app/src/androidTest/java/com/app/lokacara`. Prefer descriptive test names that state the condition and expected result.

## Commit & Pull Request Guidelines

Recent history uses Conventional Commit prefixes such as `feat:`, `fix:`, and `docs:`. Keep commits focused and imperative, for example `fix: preserve bookmark sync on token refresh`. Pull requests should include a brief summary, test results, linked issue or task when available, and screenshots or recordings for UI changes. Mention API contract changes and update `docs/` when backend expectations change.

## Security & Configuration Tips

Do not commit local secrets or machine-specific SDK paths. Keep `local.properties` local. Treat map keys, auth endpoints, and backend URLs as configuration, and document required setup changes in `docs/` instead of embedding private values in source.
