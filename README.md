# Lokacara Mobile

Lokacara Mobile is an Android app for discovering events, registering for events, saving bookmarks, managing tickets, scanning attendance QR codes, and managing user profiles.

## Tech Stack

- Kotlin
- Jetpack Compose
- Hilt
- Retrofit, OkHttp, and Moshi
- Android DataStore
- Google Maps, Places, Location, and Sign-In

## How to Run

1. Open this repository in Android Studio.

2. Make sure `local.properties` contains the Android SDK path. Android Studio usually creates this file automatically.

3. Add these values through `local.properties`, Gradle properties, or environment variables:

```properties
MAPS_API_KEY=your_google_maps_key
GOOGLE_WEB_CLIENT_ID=your_google_web_client_id
```

4. Sync Gradle in Android Studio.

5. Run the app from Android Studio using the `app` configuration, or build it from the terminal:

```powershell
.\gradlew.bat assembleDebug
```

6. Run tests and checks:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat lintDebug
```

`connectedDebugAndroidTest` requires a running Android device or emulator.
