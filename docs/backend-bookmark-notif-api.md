# Backend API — Bookmark & Notification Settings

Dua endpoint sudah diimplementasikan di server Laravel. Android app bisa panggil endpoint ini untuk sync bookmark dan preferensi notifikasi ke server.

---

## 1. Bookmark Toggle — `POST /api/bookmarks/{eventId}` & `DELETE /api/bookmarks/{eventId}`

✅ **Status: Implemented** — `feat/bookmark-notif-api`

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
- Route model binding Laravel dipakai (`Event $event`), jadi server otomatis memvalidasi bahwa event ada di database.

### Kode Laravel (aktual)

**Route — `routes/api.php`:**
```php
use App\Http\Controllers\Api\BookmarkController;

Route::middleware('auth:sanctum')->group(function () {
    Route::post('bookmarks/{event}', [BookmarkController::class, 'store']);
    Route::delete('bookmarks/{event}', [BookmarkController::class, 'destroy']);
});
```

**Controller — `app/Http/Controllers/Api/BookmarkController.php`:**
```php
<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Bookmark;
use App\Models\Event;
use Illuminate\Http\Request;
use OpenApi\Attributes as OA;

class BookmarkController extends Controller
{
    // ... index() sudah ada sebelumnya ...

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
<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Attributes\Fillable;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

#[Fillable(['user_id', 'event_id'])]
class Bookmark extends Model
{
    use HasFactory;

    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    public function event(): BelongsTo
    {
        return $this->belongsTo(Event::class);
    }
}
```

**Migration — `database/migrations/2026_06_09_154602_create_bookmarks_table.php`:**
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

✅ **Status: Implemented** — `feat/bookmark-notif-api`

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
- Kolom `notifications_enabled` ditambahkan langsung ke tabel `users` (tidak pakai tabel terpisah).
- Android app akan memanggil endpoint ini setiap kali user mengubah toggle di Settings.
- Endpoint ini ada di grup middleware `auth:sanctum`.

### Kode Laravel (aktual)

**Route — `routes/api.php`:**
```php
Route::middleware('auth:sanctum')->patch('user/settings', [UserController::class, 'updateSettings']);
```

**Controller — `app/Http/Controllers/Api/UserController.php`:**
```php
<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use OpenApi\Attributes as OA;

class UserController extends Controller
{
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
}
```

**Migration — `database/migrations/2026_06_12_000000_add_notifications_enabled_to_users_table.php`:**
```php
Schema::table('users', function (Blueprint $table) {
    $table->boolean('notifications_enabled')->default(true)->after('location');
});
```
