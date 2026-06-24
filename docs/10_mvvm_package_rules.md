# 10 MVVM Package Rules And Dependency Direction

## Package Family Ownership

| Package | Owns | Must NOT Own |
|---------|------|-------------|
| `ui/screens/` | Screen route composables, stateless content composables, section/dialog/action composables | ViewModels, repositories, Retrofit calls, DTO mapping, file I/O |
| `ui/components/` | Reusable UI composables (cards, inputs, panels, shimmers) | ViewModels, repositories, Retrofit calls, DTO mapping |
| `ui/navigation/` | NavGraph, Screen routes, NavigationActions | Screen content, business logic |
| `ui/state/` | Presentation effect contracts (snackbar, navigation effects), UI state wrappers | Business logic, data access |
| `ui/theme/` | Color, Typography, Shape | Any logic |
| `viewmodel/` | Screen state Flow, user action methods, screen-level orchestration | Bitmap processing, file I/O, DTO mapping detail, repeated validation logic |
| `repository/` | API orchestration, data source coordination, ApiResult wrapping | UI state, Compose dependencies, bitmap operations, file compression |
| `data/remote/` | Retrofit services, interceptors, network module, DTOs, mappers, ApiResult wrapper | UI state, navigation, Compose |
| `data/remote/dto/` | API response/request data classes only | Logic, mappings |
| `data/media/` | Image validation, resize, compression, multipart preparation | UI rendering, ViewModel state |
| `data/result/` | API error to user-facing message mapping | UI rendering, Compose |
| `data/validation/` | Form validation rules, field validators | UI rendering, ViewModel state |
| `data/pagination/` | Pagination state primitives, page loading guards | UI rendering, Compose |
| `data/` | DataStore managers, cache managers, local helpers | UI state, Compose, navigation |
| `model/` | Immutable domain/UI model data classes | Logic, data access |
| `di/` | Hilt @Module, @Provides, @Binds bindings | Business logic |
| `notifications/` | FCM service, notification channels, payload models | UI rendering |

## Allowed Dependency Direction

```
ui/screens → viewmodel → repository → data/remote → backend
                    ↘ data/local managers
                    ↘ data/media, data/validation, data/result, data/pagination
ui/components → (no dependencies on viewmodel/repository/data.remote)
ui/state → (pure Kotlin, no Android framework dependencies preferred)
```

### Rules

1. **UI must not directly depend on data layer.** Screens/components communicate through ViewModels. No direct calls to `ApiService`, `*Repository`, `*Manager`, Retrofit, or DTO classes from `ui/` packages.

2. **ViewModels own state and actions.** ViewModels expose `StateFlow` and named action methods. Composables call action methods; they do not mutate ViewModel state directly.

3. **Repositories are the data boundary.** ViewModels call repositories for data. Repositories call `ApiService` and local managers. Repository methods return `ApiResult<T>` or suspend functions.

4. **Shared helpers are pure or mostly pure.** `data/validation`, `data/result`, `data/pagination` should be JVM-testable. `data/media` may need Android `Bitmap`/`ContentResolver` APIs but should keep policies testable.

5. **No new subpackages without reducing complexity.** Only create subpackages (e.g., `ui/screens/auth/`) when it groups 3+ related files and reduces directory clutter.

## Migration Order

1. Create shared infrastructure (`data/result`, `data/media`, `data/validation`, `data/pagination`, `ui/state`)
2. Migrate Auth and Profile features
3. Migrate Home and Explore features
4. Migrate Create Event feature
5. Migrate Event Detail, Tickets, and secondary screens
6. Cleanup and verification

## Deferred Refactor Areas (as of Jun 2026)

### Intentionally Deferred

1. **EventDetailScreen (1056 lines) and AttendeesScreen (619 lines):** These screens are large but used existing `DetailComponents.kt` separation. A full split into route/content/dialogs/actions composables is deferred to avoid destabilizing the most complex screen flow (join/leave/report/edit/cancel/QR/certificate).

2. **ExploreViewModel (437 lines) deep pagination replacement:** The `data/pagination` primitives are available (`PaginationController`, `PaginationLoadingState`) but integrating them into `ExploreViewModel` and `HomeViewModel` would require restructuring the coroutine-driven `searchEvents()`/`loadNextPage()`/`loadMore()` methods. This is low-risk to defer since the existing pagination works correctly.

3. **`ui/state` effect contracts for existing snackbars:** The `UiEffect` sealed interface and `ScreenState` are available for new screens. Converting all existing screens from the `SnackbarManager` object to one-shot effect channels would require Navigation-aware effect collectors and is deferred.

4. **`data/media` progressive compression:** The `ProfileRepository` avatar upload uses quality-step compression (82→52) that the generic `prepareMediaFromUri` doesn't replicate. The avatar path uses the shared media utility with `MediaConstraints` matching the original dimensions, but doesn't apply progressive quality reduction.

### Known Risks

- **Google Sign-In deprecation:** loginScreen.kt and registerScreen.kt use deprecated `GoogleSignIn`/`GoogleSignInOptions` APIs. This is a pre-existing issue unrelated to this refactor.
- **`menuAnchor()` deprecation:** ExploreComponents.kt uses deprecated `Modifier.menuAnchor()`. Pre-existing.
- **Geocoder deprecation:** HomeViewModel.kt uses deprecated `Geocoder.getFromLocation()`. Pre-existing.
- **Hilt KSP annotation-target warning:** ExploreViewModel has a warning about `@ApplicationContext` annotation target changes. Pre-existing.

