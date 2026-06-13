# Fix: Google Login Error 500 — Email Null

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

Kode backend saat ini melakukan `User::updateOrCreate(['email' => $email], ...)` dengan `$email = null`, menyebabkan PostgreSQL reject karena kolom `email` memiliki constraint `NOT NULL`.

## Solusi

### 1. Ganti unique key dari `email` ke `google_id`

Cari/find user berdasarkan `google_id`, bukan `email`.  
Ini mencegah duplikasi dan gak bergantung pada email.

### 2. Fallback untuk email null

Kalau `$googleUser->getEmail()` return `null`, generate placeholder email:

```php
$email = $googleUser->getEmail() ?: 'google_' . $googleUser->getId() . '@placeholder.local';
```

### 3. Full method `googleLogin()` — Sesudah Fix

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
        ], 422);
    }

    $email = $googleUser->getEmail();
    if (empty($email)) {
        $email = 'google_' . $googleUser->getId() . '@placeholder.local';
    }

    $user = User::where('google_id', $googleUser->getId())->first();

    // Cek suspended
    if ($user && $user->suspended_at) {
        return response()->json([
            'message' => 'Your account has been suspended. Please contact support.'
        ], 403);
    }

    if (!$user) {
        $user = User::create([
            'name'              => $googleUser->getName() ?? 'User',
            'email'             => $email,
            'google_id'         => $googleUser->getId(),
            'email_verified_at' => now(),
            'provider'          => 'google',
            'password'          => bcrypt(Str::random(32)),
        ]);
    }

    // Hapus token lama (opsional)
    $user->tokens()->delete();

    $token = $user->createToken('mobile')->plainTextToken;

    return response()->json([
        'message' => 'Login successful',
        'user'    => $user->fresh(),
        'token'   => $token,
    ]);
}
```

> **Catatan:** Gunakan `User::where('google_id', ...)->first()` + `User::create()` 
> alih-alih `User::updateOrCreate()` agar lebih eksplisit dan gak conflict.

### 4. Pastikan migration `users` punya field `google_id`

```php
// Di migration create_users_table.php atau migration tambahan
$table->string('google_id')->nullable()->unique()->after('remember_token');
```

## Dampak ke Web

**Tidak ada.** Web login Google tetap berjalan normal karena:

- Web menggunakan flow redirect (`GET /auth/google/callback`), bukan API
- Logika `firstOrCreate` / `updateOrCreate` di controller web terpisah
- Perubahan hanya di `AuthController@googleLogin` (khusus API)
- Email tidak null pada sebagian besar akun — fallback hanya jalan pada edge case

## Testing

1. Login Google menggunakan akun yang **tidak membagikan email**
   - Harus masuk tanpa error
   - User dibuat dengan email `google_{google_id}@placeholder.local`
2. Login Google menggunakan akun normal (email shared)
   - Harus tetap jalan seperti biasa
3. Login via Web
   - Tidak terpengaruh

## File yang Diubah

| File | Perubahan |
|------|-----------|
| `app/Http/Controllers/Api/AuthController.php` | `googleLogin()` — cari by google_id, fallback email null |
