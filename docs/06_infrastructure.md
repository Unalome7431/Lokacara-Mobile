# 06 Infrastructure Analysis

## Build Infrastructure

The project uses Gradle Wrapper and Android Gradle Plugin.

Commands:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

Evidence:
- File: `gradlew`, `gradlew.bat`
- File: `app/build.gradle.kts`

## Docker

No Dockerfile or Docker Compose file was found.

Evidence:
- Repository scan for `Dockerfile`, `docker-compose.yml`, and `docker-compose.yaml`

## CI/CD

No CI/CD configuration was found.

Evidence:
- No `.github/workflows` directory was found.
- No workflow YAML file was found in the repository.

## Deployment

Verified:
- Release build type exists.
- Minification is disabled for release.
- `proguard-rules.pro` exists.

Evidence:
- File: `app/build.gradle.kts`
- Configuration: `buildTypes.release.isMinifyEnabled = false`
- File: `app/proguard-rules.pro`

## Environment Variables and Local Properties

Configuration keys:
- `MAPS_API_KEY`
- `GOOGLE_WEB_CLIENT_ID`

Resolution order:
1. Gradle property
2. Environment variable
3. `local.properties`
4. Empty string fallback

Evidence:
- File: `app/build.gradle.kts`
- Explanation: both values are read through Gradle providers and local properties fallback.

## Secrets and Hardcoded URLs

External config:
- Google Maps API key
- Google Web Client ID

Hardcoded URLs:
- `https://lokacara.my.id/` in `NetworkModule`
- `https://lokacara.my.id/` in `TokenRefreshHelper`
- `https://lokacara.my.id` in `ImageUrlProvider`
- Event share URL in `EventDetailScreen`

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/NetworkModule.kt`
- File: `app/src/main/java/com/app/lokacara/data/remote/TokenRefreshHelper.kt`
- File: `app/src/main/java/com/app/lokacara/data/remote/ImageUrlProvider.kt`
- File: `app/src/main/java/com/app/lokacara/ui/screens/EventDetailScreen.kt`

## Android Manifest

Permissions:
- `INTERNET`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`

Application settings:
- `allowBackup=false`
- `usesCleartextTraffic=false`
- Google Maps API key is injected through `${MAPS_API_KEY}`.

Evidence:
- File: `app/src/main/AndroidManifest.xml`

## Monitoring and Logging

Monitoring:
- No Sentry, Crashlytics, Firebase Analytics, or similar production monitoring SDK was found.

Logging:
- `AnalyticsTracker` logs events and screen views with `Log.d`.
- `MainViewModel` logs DataStore read errors with `Log.e`.
- OkHttp body logging is enabled only when `Log.isLoggable("OkHttp", Log.VERBOSE)` returns true.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/AnalyticsTracker.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/MainViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/data/remote/NetworkModule.kt`

## External Infrastructure

External services:
- Backend API at `https://lokacara.my.id/`
- Google Maps
- Google Places
- Google Location Services
- Google Sign-In

Evidence:
- File: `gradle/libs.versions.toml`
- File: `app/src/main/java/com/app/lokacara/data/remote/NetworkModule.kt`

## Unverified Findings

- App signing, Play Store deployment, staging environment, and backend deployment cannot be verified from this repository.
