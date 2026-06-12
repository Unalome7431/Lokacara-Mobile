# Backend Backlog — Lokacara API

Dokumen ini berisi **seluruh endpoint, field, dan konfigurasi** yang perlu ditambahkan di backend `lokacara.my.id` agar frontend Android berfungsi penuh tanpa workaround.

> **Update terakhir:** 2026-06-09
> **Sumber:** Audit menyeluruh 77 file Kotlin — 3 round audit
> **Total item:** 11 (8 endpoint baru + 3 field/konvensi)

---

## 📋 Daftar Prioritas

| Prioritas | # | Item | Impact |
|-----------|----|------|--------|
| 🔴 P0 | 1 | `POST /api/auth/refresh` | User dipaksa login ulang tiap token expired |
| 🔴 P0 | 3 | `GET /api/notifications` | Fitur notifikasi tidak berfungsi (data palsu) |
| 🔴 P0 | 10 | Default image URLs | 10+ gambar kosong kalau URL null |
| 🟠 P1 | 2 | `POST /api/auth/password/change` | Fitur ganti password tidak berfungsi |
| 🟠 P1 | 8 | `DELETE /api/user` | Tombol hapus akun hanya logout |
| 🟠 P1 | 9a | Field `price` di events | Semua event tampil "Gratis" |
| 🟠 P1 | 9b | Field `phone` + `location` di profile | Data profil user tidak lengkap |
| 🟡 P2 | 4 | `GET /api/categories` | Kategori hardcoded, create event error |
| 🟡 P2 | 5 | `GET /api/locations` | 2 list lokasi hardcoded berbeda |
| 🟡 P2 | 7 | `GET /api/bookmarks` | Bookmark event lama hilang |
| 🟢 P3 | 6 | `GET /api/config/tabs` | Labels statis, tidak bisa diubah tanpa update app |

---

## 🔴 P0 — CRITICAL: Fitur Inti Tidak Berfungsi

### 1. `POST /api/auth/refresh`

**Masalah:** Frontend tidak bisa me-refresh access token. Setiap kali token expired (HTTP 401), user harus logout dan login ulang. Pengalaman user sangat buruk — tiba-tiba semua API call gagal tanpa penjelasan.

**File terdampak:** `AuthInterceptor.kt:16`

**Kode saat ini:**
```kotlin
// AuthInterceptor.kt — setiap request HTTP
val token = runBlocking { sessionManager.getAccessToken() }  // blocking IO thread!
requestBuilder.addHeader("Authorization", "Bearer $token")
// Tidak ada logic untuk 401 → refresh → retry
```

**Dampak:** User yang sedang browsing tiba-tiba melihat error, harus login ulang, kehilangan state aplikasi.

**Endpoint yang dibutuhkan:**
```
POST /api/auth/refresh
Authorization: Bearer {expired_token}
```
**Request:** `{}` (token ada di header)

**Response (200):**
```json
{
    "token": "new-access-token-here"
}
```
**Response (401):** Token invalid/expired, user harus login ulang.

**Catatan:** Backend Laravel bisa pakai Sanctum — Sanctum sudah punya mekanisme refresh, tinggal di-expose endpoint.

---

### 3. `GET /api/notifications`

**Masalah:** Fitur notifikasi adalah **stub** — `NotificationRepository.kt` me-return 5 data hardcoded yang tidak pernah berubah. User tidak pernah melihat notifikasi real.

**File terdampak:** `NotificationRepository.kt:8-17`, `NotificationViewModel.kt:46`

**Kode saat ini:**
```kotlin
// NotificationRepository.kt — data palsu
fun getNotifications(): List<NotificationItem> {
    return listOf(
        NotificationItem(id = 1, senderName = "Velengio", message = "...", type = SOCIAL, ...),
        NotificationItem(id = 2, senderName = "Daffa", message = "...", type = SYSTEM, ...),
        // ... 3 lagi
    )
}
```
5 notifikasi hardcoded, nama pengirim fiktif, tidak ada integrasi API.

**Endpoint yang dibutuhkan:**
```
GET /api/notifications
Authorization: Bearer {token}
```

**Response (200):**
```json
{
    "data": [
        {
            "id": 1,
            "sender_name": "Lokacara",
            "message": "Event \"Seminar AI\" akan dimulai besok",
            "type": "system",
            "is_read": false,
            "created_at": "2026-06-09T10:00:00+07:00"
        },
        {
            "id": 2,
            "sender_name": "Budi Santoso",
            "message": "Budi mendaftar di event Anda",
            "type": "social",
            "is_read": false,
            "created_at": "2026-06-08T15:30:00+07:00"
        }
    ],
    "unread_count": 2
}
```

**Field dalam `type`:**
- `"system"` — Notifikasi dari sistem (event reminder, event approved, dll)
- `"social"` — Notifikasi dari user lain (join event, mention, dll)

---

### 10. Semua field gambar harus selalu return URL non-null

**Masalah:** Backend saat ini mengembalikan `null` untuk `poster`, `avatar_url`, dan `file_url` ketika tidak ada gambar. Frontend telah **menghapus semua fallback drawable** (`R.drawable.candi`, `R.drawable.profileicon`, dll) karena seharusnya backend yang menyediakan default.

**Akibat:** Kalau backend return `null`, AsyncImage di frontend tidak menampilkan apa-apa — blank space.

**Endpoint terdampak:**

| Endpoint | Field | DTO frontend |
|----------|-------|-------------|
| `GET /api/events/*` | `poster` / `poster_url` | `EventDto.poster: String?` |
| `GET /api/events/feed` | `poster` / `poster_url` | `EventDto.poster: String?` |
| `GET /api/events/search` | `poster` / `poster_url` | `EventDto.poster: String?` |
| `GET /api/profile` | `avatar_url` | `UserDto.avatar_url: String?` |
| `GET /api/user` | `avatar_url` | `UserDto.avatar_url: String?` |
| `GET /api/dashboard` | `file_url` (certificates) | `CertificateDto.file_url: String?` |

**Yang dibutuhkan:** Backend Laravel harus memastikan **setiap field gambar selalu return URL**:
- Ada gambar → return URL ke file asli
- Tidak ada gambar → return URL ke default (`/default_cover.jpg`, `/default_avatar.png`, `/default_certificate.png`)

**Implementasi backend (Laravel):**
```php
// Model Event.php
public function getPosterUrlAttribute(): string
{
    if ($this->poster) {
        return Storage::url($this->poster);
    }
    return asset('images/default_cover.jpg');
}

// Model User.php
public function getAvatarUrlAttribute(): string
{
    if ($this->avatar_url) {
        return $this->avatar_url; // atau Storage::url()
    }
    return asset('images/default_avatar.png');
}
```

**File default yang perlu ada di server:**
- `public/images/default_cover.jpg`
- `public/images/default_avatar.png`
- `public/images/default_certificate.png`

---

## 🟠 P1 — HIGH: Fitur Rusak / Data Tidak Lengkap

### 2. `POST /api/auth/password/change`

**Masalah:** Fitur ganti password sama sekali tidak berfungsi. `ChangePasswordViewModel` menampilkan pesan "Fitur ganti password sedang dalam pengembangan". Validasi frontend sudah siap — hanya butuh endpoint.

**File terdampak:** `ChangePasswordViewModel.kt:37-78`

**Kode saat ini:**
```kotlin
fun changePassword() {
    // Validasi password (old/new/confirm) — SUDAH JALAN ✅
    viewModelScope.launch {
        // TODO: Ganti dengan API call ke POST /api/auth/password/change
        delay(300)
        _errorMessage.value = "Fitur ganti password sedang dalam pengembangan"
    }
}
```

**Endpoint yang dibutuhkan:**
```
POST /api/auth/password/change
Authorization: Bearer {token}
```
**Request:**
```json
{
    "old_password": "password_lama",
    "new_password": "password_baru",
    "new_password_confirmation": "password_baru"
}
```

**Response (200):**
```json
{
    "message": "Password berhasil diubah"
}
```
**Response (422 — validasi gagal):**
```json
{
    "message": "The given data was invalid.",
    "errors": {
        "old_password": ["Kata sandi lama tidak sesuai"],
        "new_password": ["Kata sandi baru minimal 8 karakter"]
    }
}
```

**Catatan:** Endpoint ini **harus authenticated** (pakai Bearer token, bukan via reset token seperti forgot-password flow).

---

### 8. `DELETE /api/user`

**Masalah:** Tombol "Hapus Akun" di SettingsScreen **hanya melakukan logout**, tidak menghapus akun dari database. UX sangat misleading — user mengira akunnya sudah dihapus padahal tidak.

**File terdampak:** `SettingsScreen.kt:67-76`

**Kode saat ini:**
```kotlin
// SettingsScreen.kt — dialog "Hapus Akun"
onClick = {
    showDeleteDialog = false
    scope.launch {
        viewModel.logout()  // ← hanya logout, bukan delete!
        rootNavController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
    }
}
```

**Endpoint yang dibutuhkan:**
```
DELETE /api/user
Authorization: Bearer {token}
```

**Response (200):**
```json
{
    "message": "Akun berhasil dihapus"
}
```

**Catatan:** Setelah akun dihapus, semua data terkait (events, registrations, certificates, bookmarks) juga harus dihapus (cascade delete di database).

---

### 9a. Field `price` di responses `GET /api/events/*`

**Masalah:** Backend tidak mengembalikan field `price` di response event manapun. Akibatnya, semua event di frontend menampilkan "Gratis" — termasuk event berbayar.

**File terdampak:** `Mappers.kt:21`, `Event.kt:12`, `EventDto`

**Kode saat ini:**
```kotlin
// Mappers.kt — harga selalu hardcoded
fun EventDto.toEvent(imageUrlProvider: ImageUrlProvider): Event {
    return Event(
        ...
        price = "Gratis",  // ← hardcoded untuk SEMUA event
        ...
    )
}
```

**Field yang perlu ditambah di response:**
```json
{
    "id": 1,
    "title": "Seminar AI",
    "price": 25000,
    ...
}
```

Atau kalau gratis:
```json
{
    "price": 0,
    "is_free": true,
    ...
}
```

**Endpoint terdampak:**
- `GET /api/events/{id}`
- `GET /api/events/feed`
- `GET /api/events/search`
- `GET /api/organizer/events`

**Yang perlu diubah di frontend setelah backend siap:**
1. Tambah field `price` ke `EventDto`
2. Update `Mappers.kt:21` — map dari `EventDto.price`
3. Update `EventCard.kt` — tampilkan harga (gratis atau nominal)

---

### 9b. Field `phone` + `location` di `GET /api/profile`

**Masalah:** Backend tidak mengembalikan phone dan location di response `/api/profile`. Akibatnya ProfileViewModel selalu fallback ke empty string — user tidak bisa melihat/mengedit data kontaknya.

**File terdampak:** `UserDto.kt`, `ProfileViewModel.kt:66-67`

**Kode saat ini:**
```kotlin
// ProfileViewModel — phone & location tidak pernah dari API
_userProfile.value = UserProfile(
    name = user.name,
    email = user.email,
    phone = "",      // ← selalu kosong, tidak ada di UserDto
    location = "",   // ← selalu kosong, tidak ada di UserDto
    profileImageUrl = user.avatar_url
)
```

**Field yang perlu ditambah di `UserDto` (response dari `/api/profile`):**
```json
{
    "id": 1,
    "name": "Budi Santoso",
    "email": "budi@email.com",
    "role": "user",
    "phone": "08123456789",
    "location": "Jakarta",
    "avatar_url": "https://lokacara.my.id/avatars/abc.jpg",
    "created_at": "2026-01-01T00:00:00+07:00"
}
```

**Yang perlu diubah di frontend setelah backend siap:**
1. Tambah `phone: String? = null` dan `location: String? = null` ke `UserDto`
2. Update `ProfileViewModel.loadUserProfile()` — baca `user.phone` dan `user.location`

**Catatan:** Pastikan field ini juga bisa di-update via `PATCH /api/profile`.

---

## 🟡 P2 — MEDIUM: Workaround Berfungsi Tapi Tidak Ideal

### 4. `GET /api/categories`

**Masalah:** Tidak ada endpoint untuk mendapatkan daftar kategori event. Akibatnya:
1. **Explore Screen** — chip filter kategori hardcoded tanpa ID (filter tidak match dengan API `category_id`)
2. **Create Event** — user mengirim nama kategori (string) bukan ID kategori (int), sehingga API mungkin gagal

**File terdampak:** `ExploreRepository.kt:24`, `HomeRepository.kt:20`, `CreateEventViewModel.kt:78-79`

**Kode saat ini — 2 list hardcoded berbeda:**
```kotlin
// ExploreRepository.kt
fun getCategories(): List<String> = listOf("Workshop", "Wanita", "Webinar", "Anime", "Musik", "Teknologi")

// HomeRepository.kt
fun getCategories(): List<String> = listOf("Semua", "Musik", "Teknologi", "Anime", "Hobi")
```

**Endpoint yang dibutuhkan:**
```
GET /api/categories
```
**Response (200):**
```json
{
    "data": [
        { "id": 1, "name": "Musik", "icon": null },
        { "id": 2, "name": "Teknologi", "icon": null },
        { "id": 3, "name": "Olahraga", "icon": null },
        { "id": 4, "name": "Bisnis", "icon": null },
        { "id": 5, "name": "Seni", "icon": null },
        { "id": 6, "name": "Webinar", "icon": null },
        { "id": 7, "name": "Anime", "icon": null },
        { "id": 8, "name": "Hobi", "icon": null }
    ]
}
```

---

### 5. `GET /api/locations`

**Masalah:** Tidak ada endpoint untuk daftar lokasi. Dua repository berbeda punya list hardcoded yang tidak sinkron — `ExploreRepository` pakai "Surakarta" sedangkan `HomeRepository` pakai "Solo" (nama yang sama tapi beda string).

**File terdampak:** `ExploreRepository.kt:23`, `HomeRepository.kt:19`

**Endpoint yang dibutuhkan:**
```
GET /api/locations
```
**Response (200):**
```json
{
    "data": [
        { "id": 1, "name": "Surakarta" },
        { "id": 2, "name": "Yogyakarta" },
        { "id": 3, "name": "Semarang" },
        { "id": 4, "name": "Jakarta" },
        { "id": 5, "name": "Surabaya" }
    ]
}
```

---

### 7. `GET /api/bookmarks`

**Masalah:** BookmarkViewModel mengambil **seluruh feed events** lalu memfilter secara lokal berdasarkan bookmark IDs. Jika user mem-bookmark event lama yang sudah tidak ada di feed, event tersebut **hilang dari daftar bookmark**.

**File terdampak:** `BookmarkViewModel.kt:54`

**Kode saat ini:**
```kotlin
// BookmarkViewModel — ambil semua feed, filter lokal
safeApiCall { apiService.getFeedEvents() }.let { result ->
    when (result) {
        is ApiResult.Success -> {
            _savedEvents.value = result.data.data
                .filter { it.id.toString() in bookmarkedIds }  // event lama tidak ada di sini
                .map { it.toEvent(imageUrlProvider) }
        }
    }
}
```

**Opsi A — endpoint terpisah:**
```
GET /api/bookmarks
Authorization: Bearer {token}
```
**Response (200):**
```json
{
    "data": [
        { "id": 1, "title": "Event Lama", "poster": "posters/old.jpg", ... },
        { "id": 5, "title": "Event Baru", "poster": "posters/new.jpg", ... }
    ]
}
```

**Opsi B — tambah query param di endpoint feed:**
```
GET /api/events/feed?bookmarked=true
```
Return hanya event yang di-bookmark user yang sedang login.

---

## 🟢 P3 — LOW: Nice to Have

### 6. `GET /api/config/tabs`

**Masalah:** Label tab di TicketsScreen ("Mendatang"/"Riwayat") dan NotificationScreen ("Aktivitas"/"Informasi") saat ini hardcoded di `strings.xml`. Kalau ingin mengubah atau menambah tab, harus update app.

**File terdampak:** `TicketsScreen.kt:41-44`, `NotificationScreen.kt:35`

**Kode saat ini:**
```kotlin
// TicketsScreen.kt — labels dari strings.xml
val tabs = listOf(
    context.getString(R.string.tab_tickets_upcoming),   // "Mendatang"
    context.getString(R.string.tab_tickets_history)     // "Riwayat"
)
```

**Endpoint yang dibutuhkan:**
```
GET /api/config/tabs
```
**Response (200):**
```json
{
    "tickets_tabs": [
        { "key": "upcoming", "label": "Mendatang" },
        { "key": "history", "label": "Riwayat" }
    ],
    "notification_tabs": [
        { "key": "activity", "label": "Aktivitas" },
        { "key": "info", "label": "Informasi" }
    ],
    "settings_sections": [
        { "key": "preferences", "label": "Preferensi" },
        { "key": "security", "label": "Keamanan" },
        { "key": "help", "label": "Bantuan & Informasi" },
        { "key": "others", "label": "Lainnya" }
    ]
}
```

---

## 📊 Ringkasan Keseluruhan

| # | Prioritas | Item | Tipe | Fitur Frontend |
|---|-----------|------|------|---------------|
| 1 | 🔴 P0 | `POST /api/auth/refresh` | Endpoint baru | Auto-refresh token |
| 3 | 🔴 P0 | `GET /api/notifications` | Endpoint baru | Notifikasi real |
| 10 | 🔴 P0 | Default image URLs | Konvensi | Semua gambar tampil |
| 2 | 🟠 P1 | `POST /api/auth/password/change` | Endpoint baru | Ganti password |
| 8 | 🟠 P1 | `DELETE /api/user` | Endpoint baru | Hapus akun |
| 9a | 🟠 P1 | Field `price` | Field tambahan | Harga event |
| 9b | 🟠 P1 | Field `phone` + `location` | Field tambahan | Profil lengkap |
| 4 | 🟡 P2 | `GET /api/categories` | Endpoint baru | Filter + create event |
| 5 | 🟡 P2 | `GET /api/locations` | Endpoint baru | Filter lokasi |
| 7 | 🟡 P2 | `GET /api/bookmarks` | Endpoint baru | Bookmark akurat |
| 6 | 🟢 P3 | `GET /api/config/tabs` | Endpoint baru | Tab dinamis |

| Kategori | Jumlah |
|----------|--------|
| Endpoint baru | 8 |
| Field tambahan | 3 (price, phone, location) |
| Konvensi | 1 (default image URLs) |
| **Total** | **11** |

---

_Catatan: Semua endpoint baru menggunakan autentikasi Bearer token (kecuali `/api/config/tabs` — opsional, tergantung implementasi)._

_Frontend akan mendeteksi 401 dan menampilkan error yang sesuai — jadi endpoint baru bisa di-deploy bertahap tanpa breaking change._
