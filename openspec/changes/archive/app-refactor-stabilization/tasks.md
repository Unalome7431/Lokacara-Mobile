## 1. Audit Phase: Hardcoded Data & Dead Code Discovery

- [x] 1.1 Scan all screen composables for hardcoded string literals (labels, titles, descriptions, placeholders, messages)
- [x] 1.2 Scan all ViewModels and repositories for mock data paths, fallback defaults, and hardcoded return values
- [x] 1.3 Scan all image/imageUrl references for hardcoded/local drawable URLs that should be API-sourced
- [x] 1.4 Identify all hardcoded IDs, status labels, category names, and enum-like display values
- [x] 1.5 Identify dead code: unused functions, classes, files, imports, and resources across the entire codebase
- [x] 1.6 Identify deprecated utilities, obsolete services, and redundant logic
- [x] 1.7 Catalog all TODOs, FIXMEs, debug comments, and temporary implementations

## 2. Data Source Cleanup: Replace Hardcoded Content

- [x] 2.1 Replace hardcoded display text on HomeScreen with API-driven event data
- [x] 2.2 Replace hardcoded display text on ExploreScreen with API-driven search/filter results
- [x] 2.3 Replace hardcoded display text on EventDetailScreen with API response fields
- [x] 2.4 Replace hardcoded display text on ProfileScreen with API /api/user or /api/profile data
- [x] 2.5 Replace hardcoded display text on TicketsScreen with API ticket/registration data
- [x] 2.6 Replace hardcoded display text on MyEventsScreen, SavedEventsScreen, BookmarkScreen with API/organizer or dashboard data
- [x] 2.7 Replace hardcoded display text on SettingsScreen, NotificationScreen, and HelpCenterScreen with API/config data
- [x] 2.8 Replace hardcoded display text on CreateEventScreen with dynamic form defaults
- [x] 2.9 Hapus semua fallback drawable dari Coil loading — hapus pattern `?: R.drawable.candi` (HomeComponents, DetailComponents, TicketComponents, ProfileComponents), `R.drawable.qr_dummy` (TicketComponents, TicketsScreen), `R.drawable.sertifcontoh` (ProfileComponents), `R.drawable.profileicon` (ProfileScreen, EditProfileScreen), dan 4 demo drawable di AboutScreen (`candi`,`seminar`,`seminar_2`,`seminar_3`). Coil langsung render `imageUrl` tanpa elvis fallback — backend diwajibkan selalu mengirim URL (gambar real atau default URL server)
- [x] 2.10 Pindahkan tab labels ke string.xml — TicketsScreen (`"Mendatang","Riwayat"`), NotificationScreen (`"Aktivitas","Informasi"`), dan kategori chips di ExploreComponents (`"Semua","Musik","Teknologi","Anime","Hobi","Olahraga","Bisnis","Seni","Webinar"`). Ini temporary — nanti akan diganti API endpoint `GET /api/categories` dan `GET /api/config/tabs` kalau backend sudah siap
- [x] 2.11 Pindahkan section title strings di SettingsScreen (`"Preferensi","Keamanan","Lainnya"`) dan label menu items (`"Notifikasi","Ubah Kata Sandi","Pusat Bantuan","Kebijakan Privasi"`) ke string.xml. Ini juga temporary — nanti akan diganti API endpoint `GET /api/config/tabs` kalau backend sudah siap
- [x] 2.12 Implement avatar upload via API di ProfileRepository (ganti `// TODO` di ProfileViewModel.kt:144 dengan multipart upload ke endpoint `api/profile/avatar`)

### ✅ BUILD CHECKPOINT 1
- [x] B1 Build project — pastikan tidak ada compile error setelah semua perubahan Phase 2. Jika error, fix dulu sebelum lanjut

## 3. Data Consistency Refactor

- [x] 3.1 Audit Mappers.kt — ensure every API DTO field has a corresponding domain model mapping; add missing mappings
- [x] 3.2 Verify every repository returns ApiResult type (Success/Error) without mock fallback paths
- [x] 3.3 Standardize data flow for Event data: verify consistent DTO → Mapper → Repository → ViewModel → UI pipeline
- [x] 3.4 Standardize data flow for User/Profile data: verify consistent pipeline across all profile-related screens
- [x] 3.5 Standardize data flow for Ticket/Registration data: verify consistent pipeline
- [x] 3.6 Standardize data flow for Notification data: verify consistent pipeline
- [x] 3.7 Eliminate duplicate data sources: merge overlapping state in BookmarkManager, UserSessionManager, and local caches
- [x] 3.8 Ensure no ViewModel manually maps API fields — all mapping goes through Mappers.kt
- [x] B2 Build project — pastikan tidak ada compile error setelah perubahan Phase 3. Jika error, fix dulu sebelum lanjut

## 4. Architecture Cleanup

- [x] 4.1 Remove all dead functions, classes, and files identified in task 1.5
- [x] 4.2 Remove deprecated utilities and obsolete services (e.g., legacy managers replaced by API integration)
- [x] 4.3 Remove redundant logic: replace custom utilities with stdlib/AndroidX equivalents where applicable
- [x] 4.4 Fix naming convention violations across the codebase (Kotlin conventions)
- [x] 4.5 Extract business logic from screen composables into appropriate ViewModels
- [x] 4.6 Extract data access logic from ViewModels into repositories/data managers
- [x] 4.7 Clean up unused imports and optimize imports across all files
- [x] B3 Build project — pastikan tidak ada compile error setelah perubahan Phase 4. Jika error, fix dulu sebelum lanjut

## 5. UX & Reliability Enhancements

- [x] 5.1 Implement UiState sealed class (Loading/Success/Error) in all ViewModels that fetch data
- [x] 5.2 Add loading states (spinner/skeleton) to HomeScreen, ExploreScreen, EventDetailScreen
- [x] 5.3 Add loading states to ProfileScreen, TicketsScreen, MyEventsScreen, SavedEventsScreen
- [x] 5.4 Add loading states to NotificationScreen, CertificatesScreen, BookmarkScreen
- [x] 5.5 Add empty states with call-to-action to all list screens (Explore, MyEvents, SavedEvents, Tickets, Notifications)
- [x] 5.6 Add error states with retry buttons to all data-fetching screens
- [x] 5.7 Implement pull-to-refresh on list screens where appropriate
- [x] 5.8 Ensure graceful handling: slow networks show loading within 500ms, no ANR, UI stays interactive
- [x] B4 Build project — pastikan tidak ada compile error setelah perubahan Phase 5. Jika error, fix dulu sebelum lanjut

## 6. Performance Optimization

- [x] 6.1 Audit composables for unnecessary recompositions — add remember/derivedStateOf/LaunchedEffect where missing
- [x] 6.2 Implement API request deduplication: prevent duplicate in-flight requests for the same resource
- [x] 6.3 Add client-side caching in repositories for data that doesn't change frequently (categories, settings)
- [x] 6.4 Optimize DataStore reads: cache repeated preference reads in ViewModel fields
- [x] 6.5 Ensure all API calls have proper timeout handling and cleanup on dispose

### ✅ BUILD CHECKPOINT 5
- [x] B5 Build project — pastikan tidak ada compile error setelah perubahan Phase 6. Jika error, fix dulu sebelum lanjut

## 7. Bug Audit & Fixes

- [x] 7.1 Fix navigation issues: verify all routes, arguments, and back stack behavior
- [x] 7.2 Fix state synchronization: ensure UI updates immediately after mutations (join/leave event, bookmark toggle, profile update)
- [x] 7.3 Fix API integration issues: verify every screen handles actual API response shapes correctly
- [x] 7.4 Fix form validation: align frontend validation rules with backend expectations (Register, CreateEvent, EditProfile)
- [x] 7.5 Fix UI inconsistencies: ensure consistent use of theme colors, typography, and spacing
- [x] 7.6 Fix any crash or ANR scenarios discovered during manual testing of every screen

### ✅ BUILD CHECKPOINT 6
- [x] B6 Build project — pastikan tidak ada compile error setelah perubahan Phase 7. Jika error, fix dulu sebelum lanjut

## 8. Production Readiness

- [x] 8.1 Remove all Log.d/Log.v calls or wrap in BuildConfig.DEBUG checks
- [x] 8.2 Remove all mock data paths, debug code paths, and temporary implementations
- [x] 8.3 Remove all TODOs, FIXMEs, and debug comments that have been addressed (move unresolved ones to backlog)
- [x] 8.4 Remove unused resources from res/ (drawables, strings, colors, etc.)
- [x] 8.5 Verify release build succeeds with ProGuard/R8 minification enabled
- [x] 8.6 Verify runtime behavior in release mode: no ClassNotFoundException or missing method errors
- [x] 8.7 Final manual smoke test of all screens in release build
- [x] B7 Build project dalam mode release — pastikan tidak ada compile error setelah semua perubahan. Jika error, fix dulu sebelum archive
