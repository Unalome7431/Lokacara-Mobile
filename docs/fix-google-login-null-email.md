# Fix: Google Login Error 500 — Email Null

✅ **Status: Implemented** — `fix/google-login-null-email`

## Masalah

User login dengan Google mendapatkan error "server error".  
Backend meresponse HTTP 500.

## Error di Log

```
SQLSTATE[23502]: Not null violation: 
null value in column "email" of relation "users" violates not-null constraint
DETAIL: Failing row contains (16, Daffa Arrivo, null, null, ....)
```

## Penyebab

Beberapa akun Google **tidak membagikan email** ke aplikasi pihak ketiga.  
Ketika `Socialite::driver('google')->userFromToken($token)` dipanggil, Google mengembalikan data user dengan:

| Field | Value | Status |
|-------|-------|--------|
| `name` | "Daffa Arrivo" | ✅ OK |
| `email` | `null` | ❌ **Penyebab error** |
| `id` (google_id) | "116748881137786409507" | ✅ OK |
| `avatar` | URL foto profil | ✅ OK |

Kode backend sebelumnya melakukan `User::updateOrCreate(['email' => $email], ...)` dengan `$email = null`, menyebabkan database reject karena kolom `email` memiliki constraint `NOT NULL`.

## Solusi

### 1. Ganti lookup key dari `email` ke `provider` + `provider_id`

Repo ini menggunakan skema multi-provider (kolom `provider` + `provider_id`), bukan `google_id`.  
Cari user berdasarkan `provider='google'` + `provider_id`, bukan email.

### 2. Hybrid lookup — bridging akun email existing

Selain lookup by provider_id, jika tidak ketemu dan email tersedia, coba cari by email untuk menghubungkan akun yang sebelumnya daftar via email.

### 3. Fallback untuk email null

Kalau `$googleUser->getEmail()` return `null`, generate placeholder email:

```php
$email = $googleUser->getEmail() ?: 'google_' . $googleUser->getId() . '@placeholder.local';
```

### 4. Tidak perlu migration baru

Repo sudah punya kolom `provider_id` di tabel `users` sejak migration awal — tidak ada perubahan database.

### 5. Full method `googleLogin()` — Sesudah Fix

**File:** `app/Http/Controllers/Api/AuthController.php`

```php
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

    $email = $googleUser->getEmail();
    if (empty($email)) {
        $email = 'google_' . $googleUser->getId() . '@placeholder.local';
    }

    // 1. Cari by provider + provider_id (Google-only users, atau returning Google users)
    $user = User::where('provider', 'google')
        ->where('provider_id', $googleUser->getId())
        ->first();

    // 2. Fallback: cari by email (bridging akun web yang daftar via email)
    if (!$user && $googleUser->getEmail()) {
        $user = User::where('email', $googleUser->getEmail())->first();
    }

    // 3. Tidak ditemukan sama sekali → create baru
    if (!$user) {
        $user = User::create([
            'name'              => $googleUser->getName() ?? 'User',
            'email'             => $email,
            'provider'          => 'google',
            'provider_id'       => $googleUser->getId(),
            'email_verified_at' => now(),
            'password'          => bcrypt(Str::random(32)),
            'avatar_url'        => $googleUser->getAvatar(),
        ]);
    }

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

## Perbedaan dari Proposal Awal

| Aspek | Proposal (docs) | Aktual (repo) |
|-------|-----------------|---------------|
| Lookup key | `google_id` | `provider` + `provider_id` |
| Migration baru | Tambah kolom `google_id` | **Tidak perlu** — `provider_id` sudah ada |
| Bridging akun email | Tidak ada → bisa duplicate | Hybrid: coba by email dulu |
| Daftar email terdaftar | Bisa crash duplicate key | Aman — `updateOrCreate` by email |
| `avatar_url` | Tidak di-set | Di-set dari Google |
| Token name | `'mobile'` | `'auth_token'` (konsisten) |
| `tokens()->delete()` | Ada (opsional) | Tidak dilakukan |

## Dampak ke Web

**Tidak ada.** Web login Google tetap berjalan normal karena:

- Web menggunakan flow redirect (`GET /auth/google/callback`), bukan API
- Logika `updateOrCreate` di `GoogleController` web terpisah — tidak diubah
- Perubahan hanya di `AuthController@googleLogin` (khusus API)
- Email tidak null pada sebagian besar akun — fallback hanya jalan pada edge case

## Testing

1. Login Google menggunakan akun yang **tidak membagikan email**
   - Harus masuk tanpa error
   - User dibuat dengan email `google_{google_id}@placeholder.local`
2. Login Google menggunakan akun normal (email shared)
   - Harus tetap jalan seperti biasa
3. User daftar via email, lalu login Google dengan email yang sama
   - Akun terlink (bukan duplikat)
4. Login via Web
   - Tidak terpengaruh

## File yang Diubah

| File | Perubahan |
|------|-----------|
| `app/Http/Controllers/Api/AuthController.php` | `googleLogin()` — lookup by provider+provider_id, hybrid fallback, null email fix |
