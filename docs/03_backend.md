# 03 Backend Analysis

This repository does not contain backend server implementation. It contains an Android client that consumes the external backend API at `https://lokacara.my.id/`.

## Backend Source Status

Not found in this repository:
- Backend controllers
- Backend services
- Backend middleware
- ORM models
- Database migrations
- Seeders
- Queue workers
- Scheduled jobs

Evidence:
- Repository source tree
- Explanation: executable source code is only the Android app under `app/src/main/java/com/app/lokacara`.

## Mobile Backend Client

### `NetworkModule`

Purpose:
- Provides Moshi, OkHttp, Retrofit, and `ApiService`.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/NetworkModule.kt`
- Object: `NetworkModule`
- Functions: `provideMoshi()`, `provideOkHttp()`, `provideRetrofit()`, `provideApiService()`

### `ApiService`

Purpose:
- Defines the backend HTTP contract consumed by the mobile app.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/ApiService.kt`
- Interface: `ApiService`
- Explanation: contains auth, discovery, profile, dashboard, bookmark, notification, organizer, attendance, and certificate endpoints.

### `AuthInterceptor`

Purpose:
- Client-side request middleware.

Behavior:
- Adds `Accept: application/json`.
- Adds bearer token if available.
- Refreshes token on 401 and retries the original request.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/AuthInterceptor.kt`
- Class: `AuthInterceptor`
- Function: `intercept()`

## Authentication

Mobile authentication uses bearer tokens.

Supported flows:
- Email/password login.
- Google login with ID token.
- Registration.
- Token refresh.
- Logout.

Evidence:
- File: `app/src/main/java/com/app/lokacara/repository/AuthRepository.kt`
- Class: `AuthRepository`
- Functions: `login()`, `register()`, `loginWithGoogle()`
- File: `app/src/main/java/com/app/lokacara/viewmodel/AuthViewModel.kt`
- Function: `saveAuthenticatedSession()`

## Authorization

Server-side authorization cannot be verified.

Client-side indications:
- `UserDto` contains a `role`.
- Organizer endpoints use path prefix `api/organizer`.
- `EventDetailViewModel` computes host status by comparing `event.user.id` with current session `userId`.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/dto/AuthDtos.kt`
- Class: `UserDto`
- File: `app/src/main/java/com/app/lokacara/viewmodel/EventDetailViewModel.kt`
- Function: `loadEvent()`

## Backend Modules Inferred from API Contract

### Auth

Endpoints:
- `POST api/auth/register`
- `POST api/auth/login`
- `POST api/auth/google`
- `POST api/auth/logout`
- `POST api/auth/refresh`
- `POST api/auth/password/change`
- `POST api/auth/password/email`
- `POST api/auth/password/reset`

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/ApiService.kt`

### Discovery

Endpoints:
- `GET api/events/feed`
- `GET api/events/search`
- `GET api/events/{event}`
- `GET api/categories`
- `GET api/locations`

Evidence:
- File: `app/src/main/java/com/app/lokacara/repository/HomeRepository.kt`
- File: `app/src/main/java/com/app/lokacara/repository/ExploreRepository.kt`
- File: `app/src/main/java/com/app/lokacara/repository/EventDetailRepository.kt`

### Profile and User

Endpoints:
- `GET api/user`
- `DELETE api/user`
- `GET api/profile`
- `PATCH api/profile`
- `POST api/profile/avatar`
- `PATCH api/user/settings`

Evidence:
- File: `app/src/main/java/com/app/lokacara/repository/ProfileRepository.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/SettingsViewModel.kt`

### Participant and Dashboard

Endpoints:
- `GET api/dashboard`
- `POST api/events/{event}/join`
- `DELETE api/events/{event}/join`
- `GET api/events/{event}/attendance/qr`
- `POST api/events/{event}/report`
- `GET api/events/{event}/certificate`

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/TicketsViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/EventDetailViewModel.kt`

### Notifications

Endpoint:
- `GET api/notifications`

Evidence:
- File: `app/src/main/java/com/app/lokacara/repository/NotificationRepository.kt`

### Bookmarks

Endpoints:
- `GET api/bookmarks`
- `POST api/bookmarks/{event}`
- `DELETE api/bookmarks/{event}`

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/BookmarkSyncHelper.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/BookmarkViewModel.kt`

### Organizer

Endpoints:
- `GET api/organizer/events`
- `POST api/organizer/events`
- `POST api/organizer/events/{event}`
- `DELETE api/organizer/events/{event}`
- `GET api/organizer/events/{event}/attendees`
- `POST api/organizer/events/{event}/attendance/scan`
- `PATCH api/organizer/events/{event}/attendance/{registration}/toggle`
- `POST api/organizer/events/{event}/reminders`
- `POST api/organizer/events/{event}/certificates/template`
- `POST api/organizer/events/{event}/certificates/distribute`

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/CreateEventViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/AttendeesViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/QrScanViewModel.kt`

## Queue, Events, and Scheduled Jobs

No backend queue, event listener, or scheduled job implementation exists in this repository.

Endpoints that may trigger backend async work:
- `POST api/organizer/events/{event}/reminders`
- `POST api/organizer/events/{event}/certificates/distribute`

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/ApiService.kt`
- Functions: `sendReminders()`, `distributeCertificates()`

## Unverified Findings

- Server-side controller logic, validation, policies, middleware, and database usage cannot be verified.
- Reminder and certificate distribution may use backend queues, but no queue implementation is present in this repository.
