# Backend API — Bookmark & Notification Settings

Dua endpoint tambahan untuk Android app agar bookmark dan preferensi notifikasi bisa sync ke server.

---

## 1. Bookmark Toggle — `POST /api/bookmarks/{eventId}` & `DELETE /api/bookmarks/{eventId}`

### Deskripsi
Android app saat ini menyimpan bookmark hanya di DataStore lokal. Dua endpoint ini memungkinkan sync ke server, sehingga bookmark yang disimpan di Android juga muncul di web (dan sebaliknya).

### Endpoint Tambah Bookmark

```
POST /api/bookmarks/{eventId}
Authorization: Bearer {token}
Content-Type: application/json
```

| Parameter | Type | Location | Required | Description |
|-----------|------|----------|----------|-------------|
| `eventId` | integer | Path | ✅ | ID event yang akan di-bookmark |

**Response (201):**
```json
{
    "message": "Event bookmarked successfully"
}
```

**Response (400):**
```json
{
    "message": "Event already bookmarked"
}
```

### Endpoint Hapus Bookmark

```
DELETE /api/bookmarks/{eventId}
Authorization: Bearer {token}
```

| Parameter | Type | Location | Required | Description |
|-----------|------|----------|----------|-------------|
| `eventId` | integer | Path | ✅ | ID event yang akan di-unbookmark |

**Response (200):**
```json
{
    "message": "Bookmark removed successfully"
}
```

**Response (400):**
```json
{
    "message": "Bookmark not found"
}
```

### Catatan
- Endpoint ini **tidak mempengaruhi web flow yang sudah ada**. Web tetap pake form POST/redirect-nya sendiri.
- `GET /api/bookmarks` yang sudah ada akan otomatis mencakup bookmark dari Android.
- Server harus memvalidasi bahwa `eventId` adalah event yang valid (ada di database).

### Kode Laravel

**Route — `routes/api.php`:**
```php
use App\Http\Controllers\BookmarkController;

Route::middleware('auth:sanctum')->group(function () {
    Route::post('bookmarks/{event}', [BookmarkController::class, 'store']);
    Route::delete('bookmarks/{event}', [BookmarkController::class, 'destroy']);
});
```

**Controller — `app/Http/Controllers/BookmarkController.php`:**
```php
<?php

namespace App\Http\Controllers;

use App\Models\Event;
use App\Models\Bookmark;
use Illuminate\Http\Request;

class BookmarkController extends Controller
{
    public function store(Request $request, Event $event)
    {
        $user = $request->user();

        if ($user->bookmarks()->where('event_id', $event->id)->exists()) {
            return response()->json(['message' => 'Event already bookmarked'], 400);
        }

        $user->bookmarks()->create(['event_id' => $event->id]);

        return response()->json(['message' => 'Event bookmarked successfully'], 201);
    }

    public function destroy(Request $request, Event $event)
    {
        $user = $request->user();

        $deleted = $user->bookmarks()->where('event_id', $event->id)->delete();

        if (!$deleted) {
            return response()->json(['message' => 'Bookmark not found'], 400);
        }

        return response()->json(['message' => 'Bookmark removed successfully']);
    }
}
```

**Model — `app/Models/Bookmark.php`:**
```php
class Bookmark extends Model
{
    protected $fillable = ['user_id', 'event_id'];

    public function user()
    {
        return $this->belongsTo(User::class);
    }

    public function event()
    {
        return $this->belongsTo(Event::class);
    }
}
```

**Migration — tambah `bookmarks` table:**
```php
Schema::create('bookmarks', function (Blueprint $table) {
    $table->id();
    $table->foreignId('user_id')->constrained()->cascadeOnDelete();
    $table->foreignId('event_id')->constrained()->cascadeOnDelete();
    $table->unique(['user_id', 'event_id']);
    $table->timestamps();
});
```

---

## 2. Update User Settings — `PATCH /api/user/settings`

### Deskripsi
Android app saat ini menyimpan preferensi notifikasi hanya secara lokal. Endpoint ini memungkinkan preferensi user disimpan di server.

### Endpoint

```
PATCH /api/user/settings
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
    "notifications_enabled": false
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `notifications_enabled` | boolean | ✅ | `true` = notifikasi aktif, `false` = nonaktif |

**Response (200):**
```json
{
    "message": "Settings updated successfully",
    "data": {
        "notifications_enabled": false
    }
}
```

### Catatan
- Field `notifications_enabled` bisa ditambah ke tabel `users` atau dibuat tabel `user_settings` terpisah.
- Pilihan: tambah kolom `notifications_enabled` di tabel `users` — lebih sederhana.
- Android app akan memanggil endpoint ini setiap kali user mengubah toggle di Settings.

### Kode Laravel

**Route — `routes/api.php`:**
```php
Route::middleware('auth:sanctum')->patch('user/settings', [UserController::class, 'updateSettings']);
```

**Controller — `app/Http/Controllers/UserController.php`:**
```php
public function updateSettings(Request $request)
{
    $validated = $request->validate([
        'notifications_enabled' => 'required|boolean',
    ]);

    $user = $request->user();
    $user->notifications_enabled = $validated['notifications_enabled'];
    $user->save();

    return response()->json([
        'message' => 'Settings updated successfully',
        'data' => [
            'notifications_enabled' => (bool) $user->notifications_enabled,
        ],
    ]);
}
```

**Migration — tambah kolom ke tabel `users`:**
```php
Schema::table('users', function (Blueprint $table) {
    $table->boolean('notifications_enabled')->default(true)->after('remember_token');
});
```
