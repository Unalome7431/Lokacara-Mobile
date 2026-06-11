# POST /api/auth/google — Google Login API

## Deskripsi
Endpoint untuk login/register via Google di mobile app (Android/iOS).
Menerima Google ID token dari perangkat, verifikasi ke Google, lalu return Sanctum API token.

---

## Endpoint

```
POST /api/auth/google
Content-Type: application/json
```

---

## Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `token` | `string` | ✅ | Google ID token (JWT) yang didapat dari Google Sign-In SDK di perangkat |

```json
{
    "token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFiZDM2..."
}
```

---

## Response

### Success (200)

```json
{
    "message": "Login successful",
    "user": {
        "id": 1,
        "name": "Budi Santoso",
        "email": "budi@gmail.com",
        "role": "user",
        "phone": null,
        "location": null,
        "avatar_url": null,
        "provider": "google",
        "suspended_at": null,
        "email_verified_at": "2026-06-12T10:00:00.000000Z",
        "created_at": "2026-06-12T10:00:00.000000Z",
        "updated_at": "2026-06-12T10:00:00.000000Z"
    },
    "token": "1|abc123def456ghi789jkl012mno345pqr678..."
}
```

### Validation Error (422)

```json
{
    "message": "The given data was invalid.",
    "errors": {
        "token": ["The token field is required."]
    }
}
```

### Invalid Token (401)

```json
{
    "message": "Invalid Google token"
}
```

### Account Suspended (403)

```json
{
    "message": "Your account has been suspended. Please contact support."
}
```

---

## Alur di Android

```
1. User tap "Masuk dengan Google"
2. App launch Google Sign-In SDK → user pilih akun Google
3. Google return ID token ke app
4. App POST /api/auth/google { token: "id_token" }
5. Backend verifikasi token ke Google (Socialite::driver('google')->userFromToken())
6. Backend find-or-create user, generate Sanctum token
7. App simpan token + user data di DataStore
8. App navigasi ke HomeScreen
```

---

## Implementasi Laravel (Contoh)

### 1. Route — `routes/api.php`

```php
use App\Http\Controllers\Api\AuthController;

// Inside guest middleware group:
Route::middleware('guest')->group(function () {
    Route::post('/auth/register', [AuthController::class, 'register']);
    Route::post('/auth/login', [AuthController::class, 'login']);
    Route::post('/auth/google', [AuthController::class, 'googleLogin']);
    // ...
});
```

### 2. Controller — `app/Http/Controllers/Api/AuthController.php`

```php
use Laravel\Socialite\Facades\Socialite;
use App\Models\User;
use Illuminate\Support\Str;

public function googleLogin(Request $request)
{
    $request->validate([
        'token' => 'required|string',
    ]);

    try {
        $googleUser = Socialite::driver('google')->userFromToken($request->token);
    } catch (\Exception $e) {
        return response()->json([
            'message' => 'Invalid Google token'
        ], 401);
    }

    $user = User::updateOrCreate(
        ['email' => $googleUser->getEmail()],
        [
            'name'              => $googleUser->getName() ?? $googleUser->getEmail(),
            'password'          => bcrypt(Str::random(32)),
            'email_verified_at' => now(),
            'role'              => 'user',
            'provider'          => 'google',
            'provider_id'       => $googleUser->getId(),
            'avatar_url'        => $googleUser->getAvatar(),
        ]
    );

    if ($user->suspended_at) {
        return response()->json([
            'message' => 'Your account has been suspended. Please contact support.'
        ], 403);
    }

    $token = $user->createToken('auth_token')->plainTextToken;

    return response()->json([
        'message' => 'Login successful',
        'user'    => $user->fresh(),
        'token'   => $token,
    ], 200);
}
```

**Catatan:**
- `updateOrCreate` digunakan agar user yang sudah ada (dari register biasa) tidak dibuat duplikat — langsung login dengan akun yang sudah ada.
- Jika user sudah ada dengan `provider` berbeda (misalnya `email`), provider-nya **akan di-overwrite menjadi `google`**. Jika tidak ingin overwrite, gunakan `first()` + `create()` terpisah dengan pengecekan.
- `avatar_url` diambil dari Google profile (`$googleUser->getAvatar()`) — ini URL publik Google, jadi tidak perlu upload ulang.
- Token name menggunakan `auth_token` — konsisten dengan endpoint `login` dan `register`.

### 3. Pastikan field berikut ada di migration `users`

```php
// database/migrations/xxxx_create_users_table.php
$table->string('avatar_url')->nullable();
$table->string('provider_id')->nullable();
$table->string('provider')->nullable();
```

### 4. Pastikan Google credentials di `config/services.php`

```php
'google' => [
    'client_id'     => env('GOOGLE_CLIENT_ID'),
    'client_secret' => env('GOOGLE_CLIENT_SECRET'),
    'redirect'      => env('GOOGLE_REDIRECT_URI'),
],
```

> **Catatan:** `redirect` **tidak dipakai** di flow ini (server-side token verification). Nilainya boleh dummy/placeholder — hanya diperlukan untuk OAuth redirect flow di web browser.

---

## Catatan

### Google Cloud Console
- **Google Cloud Console** harus punya credential dengan tipe **Web application** (bukan Android) — karena verifikasi dilakukan di server.
- **Client ID & Client Secret** di `.env` adalah milik server, bukan milik app Android.
- Di Android, `requestIdToken()` pakai **Web Client ID** (server-side OAuth), bukan Android Client ID.

### API Usage
- Endpoint ini **tidak pakai Sanctum middleware** (tidak butuh `auth:sanctum`) karena user belum login.
- Response format SAMA dengan `POST /api/auth/login` dan `POST /api/auth/register` — frontend bisa pakai logic yang sama.
- Setelah dapat token, semua request selanjutnya sertakan header:
  ```
  Authorization: Bearer 1|abc123...
  ```

### Security
- **Rate limiting** direkomendasikan pada endpoint ini karena memanggil Google API eksternal. Contoh di `routes/api.php`:
  ```php
  Route::post('/auth/google', [AuthController::class, 'googleLogin'])
      ->middleware('throttle:10,1'); // max 10 request per menit
  ```
- **Handle multi-provider**: Jika user dengan email yang sama sudah terdaftar lewat provider berbeda (misal email/password), `updateOrCreate` akan **meng-overwrite** provider lama. Jika ingin mencegah ini, gunakan `first()` + `create()` terpisah dan tolak jika provider berbeda.
