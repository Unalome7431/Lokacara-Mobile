# 07 Business Rules

## Authentication and Session

### Login requires valid email and password

Trigger:
- `AuthViewModel.login()`

Conditions:
- Email cannot be blank.
- Email must match the app email regex.
- Password cannot be blank.

Outcome:
- Invalid input sets an error and blocks API call.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/AuthViewModel.kt`
- Function: `login()`

### Auth response must include token and valid user

Trigger:
- `AuthViewModel.saveAuthenticatedSession()`

Conditions:
- Token is non-blank.
- User is non-null.
- User id is greater than 0.

Outcome:
- Valid response is saved with `UserSessionManager.saveAuth()`.
- Invalid response shows "Respons login tidak valid".

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/AuthViewModel.kt`
- Function: `saveAuthenticatedSession()`

### Registration requires accepted terms

Trigger:
- `AuthViewModel.register()`

Conditions:
- `isChecked` must be true.

Outcome:
- Registration is blocked if terms are not accepted.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/AuthViewModel.kt`
- Function: `register()`

### Password rules

Rules:
- Register password minimum length: 6.
- Change password requires old password.
- New password minimum length: 6.
- Confirmation must match.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/AuthViewModel.kt`
- Function: `register()`
- File: `app/src/main/java/com/app/lokacara/viewmodel/ChangePasswordViewModel.kt`
- Function: `changePassword()`

## Networking

### Requests include JSON and bearer headers

Trigger:
- Any OkHttp request through Retrofit.

Outcome:
- Adds `Accept: application/json`.
- Adds `Authorization: Bearer <token>` if token exists.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/AuthInterceptor.kt`
- Function: `intercept()`

### 401 triggers refresh and retry

Trigger:
- API response code 401 and local token exists.

Outcome:
- Refresh token is requested.
- Original request is retried with the new token.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/AuthInterceptor.kt`
- File: `app/src/main/java/com/app/lokacara/data/remote/TokenRefreshHelper.kt`

## Home and Discovery

### Home uses stale-while-revalidate

Trigger:
- `HomeViewModel.loadData()`

Outcome:
- Cached events can be shown first.
- Feed is refreshed from backend afterward.
- Cache is stale after 30 seconds.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/HomeViewModel.kt`
- Function: `loadData()`
- File: `app/src/main/java/com/app/lokacara/data/HomeCache.kt`
- Property: `isStale`

### Popular events are sorted by view count

Trigger:
- Feed loaded successfully.

Outcome:
- Top 10 events by `viewCount` become popular events.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/HomeViewModel.kt`
- Function: `loadData()`

### Nearby events use Haversine distance

Trigger:
- Event list or current location changes.

Outcome:
- If location exists, events are sorted by distance and limited to 5.
- If location does not exist, first 5 events are used.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/HomeViewModel.kt`
- Property: `nearbyEvents`
- Function: `haversine()`

## Explore

Rules:
- Filters include name, location, category text, category chip, date filter, and price filter.
- Sort options are newest, most popular, and cheapest.
- Search history keeps the latest 10 unique non-empty queries.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/ExploreViewModel.kt`
- Property: `filteredEvents`
- Function: `onSearchSubmit()`

## Bookmarks

Rules:
- Bookmark state is local-first through DataStore.
- Backend sync is attempted after local update.
- Some flows roll back on backend failure; Home flow ignores backend failure.
- Saved events load server bookmarks first, then fetch missing local event IDs individually.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/BookmarkManager.kt`
- Function: `toggleBookmark()`
- File: `app/src/main/java/com/app/lokacara/viewmodel/HomeViewModel.kt`
- Function: `toggleBookmark()`
- File: `app/src/main/java/com/app/lokacara/viewmodel/ExploreViewModel.kt`
- Function: `toggleBookmark()`
- File: `app/src/main/java/com/app/lokacara/viewmodel/BookmarkViewModel.kt`
- Function: `loadBookmarkedEvents()`

## Create Event

Rules:
- Title is required and max 255 characters.
- Description is required and max 5000 characters.
- Category is required.
- Start and end time are required.
- End time must be after start time.
- Capacity must be between 1 and 100000.
- Online events require platform and link.
- Offline events require coordinates.
- Latitude and longitude must be provided together.
- Poster max size before compression is 10 MB.
- Posters larger than 300 KB are resized to max dimension 1600 and compressed as JPEG quality 80.
- Draft is saved on exit only if it contains meaningful input.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/CreateEventViewModel.kt`
- Functions: `publish()`, `saveDraftAndExit()`, `hasMeaningfulDraft()`

## Event Detail and Attendance

Rules:
- Host status is computed by comparing event host user id with current session user id.
- If event detail reports the user as registered, QR ticket is loaded automatically.
- QR scan requires non-empty token and non-zero event id.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/EventDetailViewModel.kt`
- Functions: `loadEvent()`, `loadQrTicket()`
- File: `app/src/main/java/com/app/lokacara/viewmodel/QrScanViewModel.kt`
- Function: `scan()`

## Tickets and Certificates

Rules:
- Joined events are split into upcoming/history using `start_datetime.take(10)` compared to today's `yyyy-MM-dd`.
- Certificate download writes the response body to a local file and marks the event as downloaded.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/TicketsViewModel.kt`
- Functions: `loadDashboard()`, `downloadCertificate()`

## Profile and Settings

Rules:
- Profile name cannot be blank.
- Profile email must be valid.
- Avatar upload max size is 5 MB.
- Notification setting is optimistic and rolls back on API failure.
- Account deletion requires password.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/ProfileViewModel.kt`
- Function: `updateProfileField()`
- File: `app/src/main/java/com/app/lokacara/repository/ProfileRepository.kt`
- Function: `uploadAvatar()`
- File: `app/src/main/java/com/app/lokacara/viewmodel/SettingsViewModel.kt`
- Functions: `setNotificationsEnabled()`, `deleteAccount()`

## Mapping Rules

Rules:
- Null or zero price displays as `Gratis`.
- Missing category displays as `Lainnya`.
- Missing organizer displays as `Penyelenggara`.
- Relative countdown labels include "Hari ini", "Besok", and day/week labels.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/Mappers.kt`
- Functions: `EventDto.toEvent()`, `countdownLabel()`

## Unverified Findings

- Server-side business rules are not available. Client-side validation should not be treated as the only source of truth.
