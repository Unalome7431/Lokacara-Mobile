# 00 Repository Inventory

This document summarizes the repository based on the actual implementation, not on assumptions.

## Summary

This repository contains one native Android mobile application named Lokacara Mobile.

Evidence:
- File: `settings.gradle.kts`
- Configuration: `rootProject.name = "Lokacara-Mobile"` and `include(":app")`
- Explanation: only one Gradle module is included: `:app`.

## Repository Structure

Top-level structure:

```text
.
|-- app/
|-- docs/
|-- gradle/
|-- openspec/
|-- AGENTS.md
|-- README.md
|-- build.gradle.kts
|-- gradle.properties
|-- gradlew
|-- gradlew.bat
|-- local.properties
`-- settings.gradle.kts
```

Main app structure:

```text
app/
|-- build.gradle.kts
|-- proguard-rules.pro
`-- src/
    |-- androidTest/java/com/app/lokacara/
    |-- main/
    |   |-- AndroidManifest.xml
    |   |-- java/com/app/lokacara/
    |   |   |-- data/
    |   |   |-- data/remote/
    |   |   |-- data/remote/dto/
    |   |   |-- di/
    |   |   |-- model/
    |   |   |-- repository/
    |   |   |-- ui/components/
    |   |   |-- ui/navigation/
    |   |   |-- ui/screens/
    |   |   |-- ui/theme/
    |   |   `-- viewmodel/
    |   `-- res/
    `-- test/java/com/app/lokacara/
```

Evidence:
- File list from `rg --files`
- Path: `app/src/main/java/com/app/lokacara`
- Explanation: all main app source code is under package `com.app.lokacara`.

## Programming Languages

- Kotlin for Android source code.
- Kotlin DSL for Gradle build files.
- XML for Android manifest and resources.

Evidence:
- File: `app/src/main/java/com/app/lokacara/MainActivity.kt`
- Class: `MainActivity`
- Explanation: the Android entry activity is implemented in Kotlin.
- File: `app/build.gradle.kts`
- Explanation: the module build script uses Kotlin DSL.
- File: `app/src/main/AndroidManifest.xml`
- Explanation: the Android manifest uses XML.

## Frameworks and Libraries

Main frameworks and libraries:
- Android application module
- Jetpack Compose
- Material 3 Compose
- Navigation Compose
- Hilt/Dagger
- Retrofit
- Moshi
- OkHttp
- AndroidX DataStore Preferences
- Coil
- Google Maps Compose, Play Services Maps, Location, Places, and Google Sign-In
- ZXing

Evidence:
- File: `app/build.gradle.kts`
- Configuration: Android application, Kotlin Compose, Hilt, and KSP plugins are applied.
- File: `gradle/libs.versions.toml`
- Explanation: version catalog declares Compose, Navigation, DataStore, Retrofit, OkHttp, Moshi, Coil, Google services, and ZXing dependencies.

## Package Manager and Build Tools

Package/dependency manager:
- Gradle through the Gradle Wrapper.
- Version catalog at `gradle/libs.versions.toml`.

Build tools:
- Android Gradle Plugin `9.1.1`
- Kotlin `2.2.10`
- KSP
- Java 17 compatibility
- compile SDK `36.1`
- min SDK `24`
- target SDK `36`

Evidence:
- File: `gradlew`, `gradlew.bat`
- Explanation: Gradle Wrapper is present.
- File: `app/build.gradle.kts`
- Explanation: SDK versions, Java compatibility, plugins, and dependencies are declared there.

## Database and Local Storage

No local relational database, Room schema, SQLite helper, migration, or seed file was found.

Verified local storage:
- DataStore `user_session` through `UserSessionManager`
- DataStore `settings` through `SettingsManager`
- DataStore `onboarding` through `OnboardingManager`
- DataStore `bookmarks` through `BookmarkManager`
- DataStore `event_draft` through `DraftManager`
- Internal file storage through `FileStorageManager`
- In-memory cache through `HomeCache`

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/UserSessionManager.kt`
- Class: `UserSessionManager`
- Explanation: stores login/session fields and encrypted access token in DataStore.
- File: `app/src/main/java/com/app/lokacara/data/DraftManager.kt`
- Class: `DraftManager`
- Explanation: stores create-event draft fields in DataStore.
- File: `app/src/main/java/com/app/lokacara/data/HomeCache.kt`
- Class: `HomeCache`
- Explanation: keeps feed and category data in memory.

## Infrastructure Found

Found:
- Android Gradle build configuration.
- External backend domain: `https://lokacara.my.id/`.
- Google Maps API key and Google Web Client ID are injected from Gradle property, environment variable, or `local.properties`.

Not found:
- Dockerfile or Docker Compose.
- CI/CD workflow.
- Backend application source code.
- Web frontend application source code.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/NetworkModule.kt`
- Object: `NetworkModule`
- Explanation: Retrofit base URL is `https://lokacara.my.id/`.
- File: `app/build.gradle.kts`
- Explanation: `MAPS_API_KEY` and `GOOGLE_WEB_CLIENT_ID` are loaded from external configuration.
- File: `app/src/main/AndroidManifest.xml`
- Explanation: Google Maps API key is passed through manifest placeholder `${MAPS_API_KEY}`.

## Main Modules

Gradle modules:
- `:app`

Main source packages:
- `com.app.lokacara`
- `com.app.lokacara.data`
- `com.app.lokacara.data.remote`
- `com.app.lokacara.data.remote.dto`
- `com.app.lokacara.di`
- `com.app.lokacara.model`
- `com.app.lokacara.repository`
- `com.app.lokacara.ui.components`
- `com.app.lokacara.ui.navigation`
- `com.app.lokacara.ui.screens`
- `com.app.lokacara.ui.theme`
- `com.app.lokacara.viewmodel`

Evidence:
- File: `settings.gradle.kts`
- Configuration: `include(":app")`
- File: `app/src/main/java/com/app/lokacara/ui/navigation/NavGraph.kt`
- Functions: `NavGraph()`, `MainContainer()`
- Explanation: navigation is implemented in the mobile app module.

## Main Services

Main client-side services/helpers:
- `ApiService`: Retrofit API contract.
- `AuthInterceptor`: adds JSON and bearer-token headers; refreshes token on 401.
- `TokenRefreshHelper`: manually calls `api/auth/refresh`.
- `ImageUrlProvider`: builds poster, avatar, and certificate URLs.
- Repositories: `AuthRepository`, `HomeRepository`, `ExploreRepository`, `EventDetailRepository`, `DashboardRepository`, `TicketsRepository`, `ProfileRepository`, `NotificationRepository`, `BookmarkRepository`.
- Local managers: `UserSessionManager`, `SettingsManager`, `OnboardingManager`, `BookmarkManager`, `DraftManager`, `FileStorageManager`.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/ApiService.kt`
- Interface: `ApiService`
- Explanation: defines all consumed backend endpoints.
- File: `app/src/main/java/com/app/lokacara/data/remote/AuthInterceptor.kt`
- Class: `AuthInterceptor`
- Function: `intercept()`
- Explanation: attaches auth headers and retries after token refresh.

## Applications

Found:
- Native Android mobile app: module `:app`, package `com.app.lokacara`, launcher activity `MainActivity`.

Not found:
- Backend app.
- Web frontend app.
- Worker/queue app.

Evidence:
- File: `app/src/main/AndroidManifest.xml`
- Activity: `.MainActivity`
- Explanation: activity has `MAIN` and `LAUNCHER` intent filter.

## Unverified Findings

- Backend implementation details, database schema, migrations, queue workers, and scheduled jobs cannot be verified from this repository.
- Backend entities such as users, events, categories, registrations, certificates, notifications, and bookmarks are inferred only from DTOs and API contracts.
