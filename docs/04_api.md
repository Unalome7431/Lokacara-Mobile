# 04 API Documentation

Base URL: `https://lokacara.my.id/`

Common behavior:
- Every request gets `Accept: application/json`.
- If an access token exists, every request gets `Authorization: Bearer <token>`.
- On HTTP 401, the client attempts token refresh and retries the original request.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/NetworkModule.kt`
- File: `app/src/main/java/com/app/lokacara/data/remote/AuthInterceptor.kt`

## Auth

| Method | URL | Request | Response | Client behavior |
|---|---|---|---|---|
| `POST` | `api/auth/register` | `RegisterRequest(name, email, password, password_confirmation)` | `AuthResponse` | Validates name, email, password length, password confirmation, and terms checkbox. |
| `POST` | `api/auth/login` | `LoginRequest(email, password)` | `AuthResponse` | Saves session only if response has token, user, and valid user id. |
| `POST` | `api/auth/google` | `GoogleLoginRequest(token)` | `AuthResponse` | Same session-save behavior as normal login. |
| `POST` | `api/auth/logout` | none | `MessageResponse` | Local logout still happens if server logout fails. |
| `POST` | `api/auth/refresh` | `{}` in `TokenRefreshHelper` | `RefreshTokenResponse(token)` | Stores the new token and retries the failed request. |
| `POST` | `api/auth/password/change` | map with `old_password`, `new_password`, `new_password_confirmation` | `MessageResponse` | Requires old password, new password min 6, confirmation match. |
| `POST` | `api/auth/password/email` | map with `email` | `MessageResponse` | Requires valid email. |
| `POST` | `api/auth/password/reset` | `Map<String, String>` | `MessageResponse` | Defined in `ApiService`; usage not verified. |

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/ApiService.kt`
- File: `app/src/main/java/com/app/lokacara/repository/AuthRepository.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/AuthViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/ChangePasswordViewModel.kt`

## Discovery

| Method | URL | Parameters | Response | Client behavior |
|---|---|---|---|---|
| `GET` | `api/events/feed` | none | `EventListResponse` | Home feed uses cache and refreshes from API. |
| `GET` | `api/events/search` | `keyword`, `category_id`, `page` | `PaginatedEventsResponse` | Used by Explore search and Home load-more. |
| `GET` | `api/events/{event}` | path `event` | `EventDetailResponse` | Used to load detail, registration status, and host state. |
| `GET` | `api/categories` | none | `CategoryListResponse` | Used by Home, Explore, and Create Event. |
| `GET` | `api/locations` | none | `LocationListResponse` | Used by Explore for location suggestions. |

Evidence:
- File: `app/src/main/java/com/app/lokacara/repository/HomeRepository.kt`
- File: `app/src/main/java/com/app/lokacara/repository/ExploreRepository.kt`
- File: `app/src/main/java/com/app/lokacara/repository/EventDetailRepository.kt`

## Profile and User

| Method | URL | Request | Response | Client behavior |
|---|---|---|---|---|
| `GET` | `api/user` | none | `UserDto` | Defined in `ApiService`. |
| `DELETE` | `api/user` | map with `password` | `MessageResponse` | Password must be non-blank. |
| `GET` | `api/profile` | none | `ProfileResponse` | Loads profile; falls back to local session if unavailable. |
| `PATCH` | `api/profile` | `Map<String, String>` | `ProfileResponse` | Name cannot be blank; email must be valid. |
| `POST` | `api/profile/avatar` | multipart `avatar` | `ProfileResponse` | Avatar max size is 5 MB. |
| `PATCH` | `api/user/settings` | map with `notifications_enabled` | `MessageResponse` | Local setting is rolled back if API sync fails. |

Evidence:
- File: `app/src/main/java/com/app/lokacara/repository/ProfileRepository.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/ProfileViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/SettingsViewModel.kt`

## Participant and Dashboard

| Method | URL | Request | Response | Client behavior |
|---|---|---|---|---|
| `GET` | `api/dashboard` | none | `DashboardResponse` | Tickets split joined events into upcoming/history. |
| `POST` | `api/events/{event}/join` | none | `MessageResponse` | Sets registered state and loads QR ticket. |
| `DELETE` | `api/events/{event}/join` | none | `MessageResponse` | Clears registered state. |
| `GET` | `api/events/{event}/attendance/qr` | none | `QrTicketResponse` | Stores `registration.qr_token`. |
| `POST` | `api/events/{event}/report` | `Map<String, String>` | `MessageResponse` | Defined in `ApiService`; detailed usage not verified. |
| `GET` | `api/events/{event}/certificate` | none | `ResponseBody` | Streams certificate file to local storage. |

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/TicketsViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/EventDetailViewModel.kt`

## Notifications

| Method | URL | Request | Response |
|---|---|---|---|
| `GET` | `api/notifications` | none | `NotificationListResponse(data, unread_count)` |

Evidence:
- File: `app/src/main/java/com/app/lokacara/repository/NotificationRepository.kt`

## Bookmarks

| Method | URL | Request | Response | Client behavior |
|---|---|---|---|---|
| `GET` | `api/bookmarks` | none | `BookmarkListResponse` | Saved-events screen loads server bookmarks first. |
| `POST` | `api/bookmarks/{event}` | none | `MessageResponse` | Adds bookmark after local toggle. |
| `DELETE` | `api/bookmarks/{event}` | none | `MessageResponse` | Removes bookmark after local toggle. |

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/BookmarkViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/data/BookmarkSyncHelper.kt`

## Config

| Method | URL | Request | Response | Client behavior |
|---|---|---|---|---|
| `GET` | `api/config/tabs` | none | `ConfigTabsResponse` | Endpoint exists; usage not verified. |

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/dto/ConfigDtos.kt`

## Organizer

| Method | URL | Request | Response | Client behavior |
|---|---|---|---|---|
| `GET` | `api/organizer/events` | query `page` | `PaginatedEventsResponse` | Loads organizer events. |
| `POST` | `api/organizer/events` | multipart event fields | `CreateEventResponse` | Validates form and compresses poster if needed. |
| `POST` | `api/organizer/events/{event}` | multipart event fields | `CreateEventResponse` | Endpoint exists; usage not verified. |
| `DELETE` | `api/organizer/events/{event}` | none | `MessageResponse` | Endpoint exists. |
| `GET` | `api/organizer/events/{event}/attendees` | query `page` | `AttendeesResponse` | Loads attendee list. |
| `POST` | `api/organizer/events/{event}/attendance/scan` | `ScanRequest(qr_token)` | `ScanResponse` | QR token must be non-empty. |
| `PATCH` | `api/organizer/events/{event}/attendance/{registration}/toggle` | none | `ScanResponse` | Updates attendee state locally. |
| `POST` | `api/organizer/events/{event}/reminders` | none | `MessageResponse` | Triggers reminder send. |
| `POST` | `api/organizer/events/{event}/certificates/template` | multipart `template` | `MessageResponse` | Endpoint exists; usage not verified. |
| `POST` | `api/organizer/events/{event}/certificates/distribute` | `Map<String, Any>` | `MessageResponse` | Endpoint exists; usage not verified. |

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/CreateEventViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/AttendeesViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/QrScanViewModel.kt`

## Error Handling

`safeApiCall()`:
- Reads `message` from HTTP error response bodies when possible.
- Converts IO errors to a connection error message.
- Converts unknown exceptions to generic error messages.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/ApiResult.kt`
- Function: `safeApiCall()`

## Unverified Findings

- Server-side validation, auth requirements, status codes, and full response examples cannot be verified from this mobile repository.
