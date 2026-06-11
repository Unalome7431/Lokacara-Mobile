## Context

Lokacara-Mobile is a single-module Android app (Kotlin, Jetpack Compose, Hilt, Retrofit/Moshi) with ~80 Kotlin files across screens, ViewModels, repositories, data managers, and DI modules. The backend API (lokacara.my.id, Laravel-style REST) is fully integrated, but the app still contains hardcoded data, mock values, placeholder assets, and temporary logic left over from development. These artifacts exist at every layer: UI hardcodes text/labels/IDs, ViewModels use fallback defaults, repositories have mock return paths, and data managers contain unused methods.

## Goals / Non-Goals

**Goals:**
- Every screen displays data sourced from backend API responses or local databases — zero hardcoded display content
- Single consistent data flow: API DTO → Domain Model → Repository → ViewModel → UI for every data type
- Dead code, unused imports, deprecated utilities, and redundant logic removed from the entire codebase
- Performance baseline: eliminate recomposition leaks, redundant API calls, and excessive DataStore reads
- Consistent UX patterns: loading/error/empty/retry states across all screens
- All known bugs fixed, no placeholder or TODO implementations remain

**Non-Goals:**
- No new features or screens — this is purely a refactor and stabilization effort
- No backend API changes — the API contract is frozen
- No migration to multi-module architecture — the single module stays
- No dependency upgrades beyond patch versions within the current stack
- No UI redesign — visual appearance remains the same, only data sources change

## Decisions

### 1. Audit Methodology: Hybrid (Static Analysis + Manual Review)
- **Decision**: Use a combination of grep-based static analysis + screen-by-screen manual review
- **Rationale**: grep catches obvious patterns (hardcoded strings, mock data, TODOs) but can't detect logically hardcoded values (e.g., a default empty list that shadows real data). Manual review catches the rest.
- **Alternatives considered**: Pure automated audit (misses logical issues) vs pure manual (too slow)

### 2. Hardcoded Data Replacement: Follow Existing Repository Pattern
- **Decision**: Every hardcoded value is replaced by tracing the proper data source (API endpoint via repository or DataStore) and wiring it through the existing architecture. No new data flow patterns.
- **Rationale**: The API layer is already complete. The problem is that many screens bypass it. The fix is to use what's already built.
- **Risk**: Some screens may discover missing API fields — handled by using sensible defaults only as true fallbacks (not mock data).

### 3. Architecture Cleanup: Safe Deletion with Usage Verification
- **Decision**: Remove code only after verifying zero usage with IDE "Find Usages" equivalent (grep across codebase). Keep public API contracts stable.
- **Rationale**: Minimizes risk of breaking something inadvertently. The refactor is already broad enough without introducing accidental regressions.

### 4. State Management: Unified Sealed Class Pattern
- **Decision**: Standardize on a UiState<T> sealed class (Loading/Success/Error) across all ViewModels where it's not already used. Replace ad-hoc nullable-and-null-check patterns.
- **Rationale**: Consistent error handling makes the UX reliability improvements systematic rather than one-off. Many ViewModels already use variations of this pattern.

### 5. Performance: Targeted Optimization, Not Rewrite
- **Decision**: Profile first with compose compiler metrics and network logging, then fix specific bottlenecks. No premature optimization.
- **Rationale**: Premature optimization wastes time. The real perf issues are likely unnecessary recompositions (missing `remember`/`derivedStateOf`) and redundant API calls (missing caching/deduplication).

## Risks / Trade-offs

- **[Scope Creep]** The refactor touches most files — some "while we're here" fixes may expand scope.
  → Mitigation: Strictly enforce the Non-Goals. Log nice-to-haves separately.
- **[Regression Risk]** Replacing data sources can break screens in subtle ways (e.g., field mapping mismatch).
  → Mitigation: Manual testing of every screen after changes. No automated UI tests exist yet.
- **[Missed Hardcoded Values]** Some hardcoded values may be deeply buried (e.g., in composable lambdas, string resources with wrong values).
  → Mitigation: Two-pass approach: automated grep + manual screen-by-screen review.
- **[Performance Regressions]** Fixing data sources might add latency if the API is slow.
  → Mitigation: Add loading states systematically (UiState pattern) and consider client-side caching for repeated data.
- **[Team Coordination]** 5 team members may step on each other's changes.
  → Mitigation: Task assignment by screen/module ownership. Work sequentially where dependencies exist.

## Dependencies / Backlog (Endpoint Needs for Backend Team)

Endpoint berikut perlu ditambahkan atau dilengkapi di backend `lokacara.my.id` agar frontend berfungsi penuh tanpa workaround:

| # | Endpoint / Field | Kegunaan | Screens / Files Terkait |
|---|---|---|---|
| 1 | `POST /api/auth/refresh` | Refresh token otomatis saat expired 401 — user tidak perlu login ulang | `AuthInterceptor.kt` |
| 2 | `GET /api/notifications` | Data notifikasi real, bukan hardcoded mock | `NotificationRepository.kt` |
| 3 | Field `price` di `GET /api/events` response | Harga event — sekarang hardcoded "Gratis" | `Mappers.kt` |
| 4 | Field `phone` + `location` di `GET /api/profile` response | Data profil user tidak lengkap — phone & location selalu fallback kosong | `ProfileViewModel.kt`, `UserDto` |
| 5 | `POST /api/auth/password/change` | Endpoint khusus ganti password (bukan forgot-password flow) | `ChangePasswordViewModel.kt` |
| 6 | `GET /api/locations` | Daftar lokasi — sekarang `HomeRepository` dan `ExploreRepository` punya hardcoded list berbeda | `HomeRepository.kt`, `ExploreRepository.kt` |
| 7 | `GET /api/categories` | Daftar kategori dengan `id` — sekarang hardcoded tanpa ID, filter tidak bisa match | `ExploreRepository.kt`, `HomeRepository.kt` |
| 8 | `GET /api/config/tabs` | Key-label mapping untuk tab dinamis — `{"tickets": [{"key":"upcoming","label":"Mendatang"}], ...}` | `TicketsScreen`, `NotificationScreen`, `SettingsScreen` |
| 9 | Default image URLs | Pastikan `image_url`, `profile_image_url`, `file_url` selalu terisi (gambar real atau default server) | Semua screen |

**Status**: Untuk sekarang, labels dan fallback menggunakan `string.xml` dan hardcoded values sebagai solusi sementara. Saat endpoint sudah ready, migrasi bisa dilakukan sebagai change terpisah.
