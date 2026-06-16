# 08 Hidden Knowledge

## Hardcoded Assumptions

### Backend domain is duplicated

Finding:
- `https://lokacara.my.id` appears in multiple files.

Risk:
- Changing environment requires editing multiple places.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/NetworkModule.kt`
- File: `app/src/main/java/com/app/lokacara/data/remote/TokenRefreshHelper.kt`
- File: `app/src/main/java/com/app/lokacara/data/remote/ImageUrlProvider.kt`
- File: `app/src/main/java/com/app/lokacara/ui/screens/EventDetailScreen.kt`

### Token refresh duplicates network setup

Finding:
- `TokenRefreshHelper` creates a separate OkHttp client and manually builds the refresh request.

Risk:
- Timeout, logging, and base URL config can diverge from the main Retrofit client.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/TokenRefreshHelper.kt`
- Class: `TokenRefreshHelper`

### Onboarding has two persistence managers

Finding:
- `SettingsManager` and `OnboardingManager` both store onboarding state.
- Root routing uses `SettingsManager`.

Risk:
- Future code may write/read different sources of truth.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/MainViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/data/SettingsManager.kt`
- File: `app/src/main/java/com/app/lokacara/data/OnboardingManager.kt`

### Auth interceptor blocks with `runBlocking`

Finding:
- `AuthInterceptor.intercept()` reads token using `runBlocking`.

Risk:
- OkHttp threads can be blocked by DataStore reads.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/AuthInterceptor.kt`
- Function: `intercept()`

## Magic Values

| Value | Meaning | Evidence |
|---|---|---|
| `30_000L` | Home cache stale threshold | `HomeCache.isStale` |
| `10_000` | Location fallback delay | `HomeViewModel.autoDetectLocation()` |
| `6371.0` | Earth radius for Haversine | `HomeViewModel.haversine()` |
| `200` | Home max loaded events | `HomeViewModel.loadMore()` |
| `255` | Event title max length | `CreateEventViewModel.publish()` |
| `5000` | Event description max length | `CreateEventViewModel.publish()` |
| `100_000` | Event capacity max | `CreateEventViewModel.publish()` |
| `10_000_000` | Poster max size | `CreateEventViewModel.publish()` |
| `300_000` | Poster compression threshold | `CreateEventViewModel.publish()` |
| `1600f` | Poster max dimension | `CreateEventViewModel.publish()` |
| `80` | JPEG quality | `CreateEventViewModel.publish()` |
| `5_000_000` | Avatar max size | `ProfileRepository.uploadAvatar()` |
| `512` | QR bitmap size | `TicketComponents.createQrBitmap()` |
| `50` | Default create-event capacity | `CreateEventViewModel`, `DraftManager` |

## Technical Debt

### Repository layer is inconsistent

Finding:
- Some ViewModels use repositories; some call `ApiService` directly.

Risk:
- Error handling and testability are inconsistent.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/HomeViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/CreateEventViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/AttendeesViewModel.kt`

### Bookmark sync is duplicated

Finding:
- Bookmark logic exists in `HomeViewModel`, `ExploreViewModel`, `BookmarkViewModel`, and `BookmarkSyncHelper`.

Risk:
- Rollback behavior differs between screens.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/HomeViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/ExploreViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/BookmarkViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/data/BookmarkSyncHelper.kt`

### Empty repository placeholder

Finding:
- `BookmarkRepository` is empty except for a future-sync comment.

Evidence:
- File: `app/src/main/java/com/app/lokacara/repository/BookmarkRepository.kt`

## Workarounds

### Draft poster URI is not restored

Finding:
- Draft stores poster URI string, but restore intentionally skips it because `content://` URIs may expire.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/CreateEventViewModel.kt`
- Function: `loadDraft()`

### HTTP asset URLs are forced to HTTPS

Finding:
- `ImageUrlProvider` converts `http://` to `https://`.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/ImageUrlProvider.kt`

### Home load-more uses search endpoint

Finding:
- Initial Home feed uses `api/events/feed`, but pagination uses `api/events/search`.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/HomeViewModel.kt`
- Functions: `loadData()`, `loadMore()`

## Known Risks

- Date handling uses string slicing and `SimpleDateFormat`, which can cause timezone/boundary issues.
- Explore date filter boundaries may include too wide a range for "today".
- Some sync failures are silently ignored.
- Auth requirements are not explicit in endpoint definitions; bearer token is attached opportunistically.

Evidence:
- File: `app/src/main/java/com/app/lokacara/data/remote/Mappers.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/ExploreViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/HomeViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/SettingsViewModel.kt`

## Fragile Modules

High-risk modules:
- `CreateEventViewModel`: validation, draft, media processing, and multipart API are in one class.
- `ExploreViewModel`: filtering, pagination, analytics, bookmarks, and search history are combined.
- Auth refresh: synchronous interceptor plus manual refresh helper.

Evidence:
- File: `app/src/main/java/com/app/lokacara/viewmodel/CreateEventViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/viewmodel/ExploreViewModel.kt`
- File: `app/src/main/java/com/app/lokacara/data/remote/AuthInterceptor.kt`
- File: `app/src/main/java/com/app/lokacara/data/remote/TokenRefreshHelper.kt`

## Unverified Findings

- Existing backend notes in `docs/` mention Laravel/Sanctum/Socialite concepts, but backend source code is not present here.
