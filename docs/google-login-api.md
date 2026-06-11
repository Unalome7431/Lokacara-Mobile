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

### Invalid Token (422)

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
use App\Http\Controllers\AuthController;

Route::post('auth/google', [AuthController::class, 'googleLogin']);
```

### 2. Controller — `app/Http/Controllers/AuthController.php`

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
        ], 422);
    }

    $user = User::where('email', $googleUser->getEmail())->first();

    // Cek suspended
    if ($user && $user->suspended_at) {
        return response()->json([
            'message' => 'Your account has been suspended. Please contact support.'
        ], 403);
    }

    // Find or create user
    if (!$user) {
        $user = User::create([
            'name'              => $googleUser->getName(),
            'email'             => $googleUser->getEmail(),
            'email_verified_at' => now(),
            'provider'          => 'google',
            'password'          => bcrypt(Str::random(32)),
        ]);
    }

    // Hapus token lama user (optional — biar gak numpuk)
    // $user->tokens()->delete();

    $token = $user->createToken('mobile')->plainTextToken;

    return response()->json([
        'message' => 'Login successful',
        'user'    => $user->fresh(),
        'token'   => $token,
    ]);
}
```

### 3. Pastikan `provider` field ada di migration `users`

```php
// database/migrations/xxxx_create_users_table.php
$table->string('provider')->nullable()->after('remember_token');
```

### 4. Pastikan Google credentials di `config/services.php`

```php
'google' => [
    'client_id'     => env('GOOGLE_CLIENT_ID'),
    'client_secret' => env('GOOGLE_CLIENT_SECRET'),
    'redirect'      => env('GOOGLE_REDIRECT_URI'),
],
```

---

## Catatan

- **Google Cloud Console** harus punya credential dengan tipe **Web application** (bukan Android) — karena verifikasi dilakukan di server.
- **Client ID & Client Secret** di `.env` adalah milik server, bukan milik app Android.
- Di Android, `requestIdToken()` pakai **Web Client ID** (server-side OAuth), bukan Android Client ID.
- Endpoint ini **tidak pakai Sanctum middleware** (tidak butuh `auth:sanctum`) karena user belum login.
- Response format SAMA dengan `POST /api/auth/login` dan `POST /api/auth/register` — frontend bisa pakai logic yang sama.
