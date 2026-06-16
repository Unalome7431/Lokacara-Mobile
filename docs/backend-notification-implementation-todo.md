# Backend Notification Implementation TODO

Dokumen ini adalah handoff untuk backend ketika repo backend sudah tersedia. Android sudah siap menerima FCM generic payload, menyimpan token FCM lewat API, dan route tap notification ke screen yang sesuai.

## 1. Setup Firebase Admin di Backend

Simpan Firebase Admin private key di server/backend, jangan taruh di repo Android dan jangan commit ke git.

Contoh lokasi lokal:

```env
GOOGLE_APPLICATION_CREDENTIALS=C:\Users\advan\secrets\lokacara-firebase-adminsdk.json
```

Jika backend Laravel, opsi package yang disarankan:

```bash
composer require kreait/laravel-firebase
php artisan vendor:publish --provider="Kreait\Laravel\Firebase\ServiceProvider" --tag=config
```

Tambahkan env sesuai package yang dipakai. Minimal backend harus bisa mengirim FCM data message ke token user.

## 2. Database yang Perlu Dibuat/Diubah

### `push_tokens`

Simpan token FCM per user.

Kolom minimal:

```text
id
user_id
token unique
platform default android
last_used_at nullable
created_at
updated_at
```

Relasi:

```text
users 1..n push_tokens
```

### `notifications`

Jika tabel sudah ada, tambahkan field berikut:

```text
category nullable string
target nullable string
event_id nullable foreign key
```

Response `GET /api/notifications` harus tetap backward compatible, tapi boleh menambah field:

```json
{
  "id": 1,
  "sender_name": "Lokacara",
  "message": "Kamu berhasil terdaftar di Seminar AI.",
  "type": "system",
  "category": "registration_success",
  "target": "tickets",
  "event_id": 123,
  "is_read": false,
  "created_at": "2026-06-15T10:00:00+07:00"
}
```

### `event_notification_deliveries`

Tabel ini dipakai untuk idempotency supaya reminder/notifikasi massal tidak terkirim dobel.

Kolom minimal:

```text
id
event_id
user_id
category
reminder_offset nullable string
notification_id nullable
push_sent_at nullable
email_sent_at nullable
created_at
updated_at
```

Unique index yang disarankan:

```text
unique(event_id, user_id, category, reminder_offset)
```

Untuk kategori non-reminder, `reminder_offset` bisa `null` atau string tetap seperti `none`, tergantung database yang dipakai.

## 3. API Endpoint yang Harus Ada

### Register Push Token

```http
POST /api/user/push-tokens
Authorization: Bearer {token}
Content-Type: application/json

{
  "token": "{fcm_token}",
  "platform": "android"
}
```

Behavior:

```text
- Auth wajib.
- Validate token required string.
- Validate platform optional, default android.
- Upsert berdasarkan token.
- Set user_id ke user yang sedang login.
- Update last_used_at.
- Return MessageResponse.
```

### Delete Push Token

```http
DELETE /api/user/push-tokens
Authorization: Bearer {token}
Content-Type: application/json

{
  "token": "{fcm_token}"
}
```

Behavior:

```text
- Auth wajib.
- Hapus token milik user yang sedang login.
- Jangan error kalau token tidak ditemukan.
- Return MessageResponse.
```

## 4. Generic FCM Payload

Backend harus kirim FCM **data-only message**. Jangan bergantung pada notification payload Firebase, karena Android app sudah punya local notification renderer sendiri.

Format:

```json
{
  "category": "registration_success",
  "target": "tickets",
  "event_id": "123",
  "title": "Pendaftaran berhasil",
  "body": "Kamu berhasil terdaftar di Seminar AI."
}
```

Supported `target`:

```text
event_detail
tickets
certificates
notification
```

Fallback Android:

```text
- Unknown target -> notification screen
- target=event_detail tanpa event_id valid -> notification screen
```

## 5. Channel Policy

### Email + Push + In-App

Kategori berikut harus membuat row notification, kirim push jika `notifications_enabled = true`, dan kirim email:

```text
registration_success
event_reminder
event_updated
event_cancelled
certificate_available
```

Catatan:

```text
- event_updated email hanya untuk perubahan penting: jadwal, lokasi, atau link online.
- registration_success email dibuat ringkas saja.
- QR/token tiket jangan dikirim via email. Tiket tetap dibuka di app.
```

### Push + In-App Only

Kategori berikut membuat row notification dan kirim push jika `notifications_enabled = true`, tapi tidak kirim email:

```text
host_new_registration
host_registration_cancelled
attendance_checked_in
event_capacity_warning
bookmarked_event_reminder
```

## 6. Trigger yang Harus Diimplementasikan

### `registration_success`

Trigger:

```text
User berhasil join event.
```

Recipient:

```text
User yang baru daftar.
```

Target:

```text
tickets
```

Email:

```text
Ya, ringkas.
Subject: Pendaftaran event berhasil
Body: Kamu berhasil terdaftar di {event_title}.
Optional: tanggal/jam event.
No QR/token.
```

### `event_reminder`

Trigger:

```text
Scheduler otomatis H-7, H-3, H-1, Hari H.
Jam kirim mengikuti jam start_datetime event.
```

Recipient:

```text
Semua active registration saat scheduler jalan.
```

Target:

```text
event_detail
```

Email:

```text
Ya.
```

Idempotency:

```text
unique(event_id, user_id, category=event_reminder, reminder_offset)
reminder_offset: H-7, H-3, H-1, H-DAY
```

### `event_updated`

Trigger:

```text
Host update event.
```

Recipient:

```text
Semua active registration.
```

Kirim hanya jika field penting berubah:

```text
start_datetime
end_datetime
location_name
address
latitude
longitude
platform_name
link
```

Target:

```text
event_detail
```

Email:

```text
Ya, kalau perubahan menyangkut jadwal/lokasi/link.
```

### `event_cancelled`

Trigger:

```text
Host/admin membatalkan event atau event dihapus dengan status cancelled.
```

Recipient:

```text
Semua active registration.
```

Target:

```text
notification atau event_detail jika detail event masih bisa dibuka.
```

Email:

```text
Ya.
```

Catatan:

```text
Lebih baik backend punya status cancelled daripada hard delete, supaya user masih bisa melihat informasi pembatalan.
```

### `certificate_available`

Trigger:

```text
Host distribute certificate atau backend selesai generate certificate.
```

Recipient:

```text
User yang eligible menerima sertifikat.
```

Target:

```text
certificates
```

Email:

```text
Ya.
```

### `host_new_registration`

Trigger:

```text
Ada user baru daftar event milik host.
```

Recipient:

```text
Host event.
```

Target:

```text
event_detail
```

Email:

```text
Tidak.
```

### `host_registration_cancelled`

Trigger:

```text
Peserta batal daftar/leave event.
```

Recipient:

```text
Host event.
```

Target:

```text
event_detail
```

Email:

```text
Tidak.
```

### `attendance_checked_in`

Trigger:

```text
QR peserta berhasil discan oleh host.
```

Recipient:

```text
Peserta yang check-in.
```

Target:

```text
tickets
```

Email:

```text
Tidak.
```

### `event_capacity_warning`

Trigger:

```text
Jumlah peserta mencapai threshold 80%, 90%, dan 100%.
```

Recipient:

```text
Host event.
```

Target:

```text
event_detail
```

Email:

```text
Tidak.
```

Idempotency:

```text
Jangan kirim threshold yang sama dua kali untuk event yang sama.
```

### `bookmarked_event_reminder`

Trigger:

```text
Event yang disimpan/bookmarked hampir mulai, tapi user belum daftar.
```

Recipient:

```text
User yang bookmark event dan belum registered.
```

Target:

```text
event_detail
```

Email:

```text
Tidak.
```

## 7. Suggested Laravel Commands

Jika backend Laravel, task awal biasanya:

```bash
php artisan make:model PushToken -m
php artisan make:migration add_notification_routing_fields_to_notifications_table
php artisan make:model EventNotificationDelivery -m
php artisan make:controller Api/PushTokenController
php artisan make:service NotificationDispatchService
php artisan make:job SendPushNotificationJob
php artisan make:job SendNotificationEmailJob
php artisan make:command SendEventRemindersCommand
php artisan make:mail RegistrationSuccessMail
php artisan make:mail EventReminderMail
php artisan make:mail EventUpdatedMail
php artisan make:mail EventCancelledMail
php artisan make:mail CertificateAvailableMail
```

Kalau project tidak punya `make:service`, buat manual:

```text
app/Services/NotificationDispatchService.php
```

Route contoh:

```php
Route::middleware('auth:sanctum')->group(function () {
    Route::post('/user/push-tokens', [PushTokenController::class, 'store']);
    Route::delete('/user/push-tokens', [PushTokenController::class, 'destroy']);
});
```

Scheduler contoh:

```php
// app/Console/Kernel.php
$schedule->command('events:send-reminders')->everyMinute();
```

Cron server:

```bash
* * * * * cd /path/to/backend && php artisan schedule:run >> /dev/null 2>&1
```

Worker server:

```bash
php artisan queue:work
```

## 8. Acceptance Criteria

Backend dianggap selesai kalau:

```text
- Android bisa POST FCM token setelah login tanpa error.
- Android bisa DELETE FCM token saat logout tanpa error.
- User menerima push registration_success setelah join event.
- User menerima email ringkas registration_success setelah join event.
- Host menerima push host_new_registration saat ada peserta baru.
- User menerima reminder H-7/H-3/H-1/Hari H hanya sekali per offset.
- Event update penting mengirim push + email ke peserta aktif.
- Event cancelled mengirim push + email ke peserta aktif.
- Certificate available mengirim push + email ke user eligible.
- GET /api/notifications menampilkan notification row dengan category, target, event_id.
- notifications_enabled=false menghentikan push, tapi email transactional tetap dikirim.
```

## 9. Manual Test Payload

Untuk test Android lewat Firebase Console atau backend dev script, kirim data message:

```json
{
  "category": "registration_success",
  "target": "tickets",
  "event_id": "123",
  "title": "Pendaftaran berhasil",
  "body": "Kamu berhasil terdaftar di Seminar AI."
}
```

Expected Android behavior:

```text
- Notification muncul jika permission aktif dan notifications_enabled=true.
- Tap notification membuka halaman Tickets.
```

## 10. Implementation Status (Updated 2026-06-16)

Backend implemented at: `D:\Lokacara\Lokacara`

### ✅ Completed

| Area | Details |
|---|---|
| Firebase Admin SDK | kreait/laravel-firebase 7.2.1 installed, `config/firebase.php` ready |
| push_tokens table | Created with `id`, `user_id`, `token` (unique), `platform`, `last_used_at` |
| notifications fields | Added `category`, `target`, `event_id` (all nullable) |
| event_notification_deliveries | Created with unique index `(event_id, user_id, category, reminder_offset)` |
| events.status column | Added (`string(20)`, default `'active'`) — was missing, now fixed |
| POST /api/user/push-tokens | Upsert by token, auth required |
| DELETE /api/user/push-tokens | Soft delete by token, auth required |
| GET /api/notifications | Now returns `category`, `target`, `event_id` |
| FCM data-only payload | `SendPushNotificationJob` sends with correct format |
| registration_success | Hooked in `EventRegistrationService::joinEvent()` |
| host_new_registration | Hooked in `EventRegistrationService::joinEvent()` |
| host_registration_cancelled | Hooked in `EventRegistrationService::leaveEvent()` |
| event_capacity_warning | Threshold 80%/90%/100% with idempotency |
| attendance_checked_in | Hooked in `AttendanceApiController@scan` |
| certificate_available | Hooked in `DistributeCertificatesJob` |
| event_updated | Hooked in `EventManagementApiController@update` (important fields only) |
| event_reminder (scheduler) | `SendEventRemindersCommand`: H-7, H-3, H-1, H-DAY + idempotency |
| bookmarked_event_reminder | Part of `SendEventRemindersCommand` (24h window) |
| Email templates | RegistrationSuccess, EventUpdated, CertificateAvailable (reused existing EventReminder, EventCancelledForParticipant) |
| Channel policy | `NotificationDispatchService`: email+push vs push-only |
| notifications_enabled check | Push skipped if false, email transactional tetap jalan |
| Scheduler | `->withSchedule()` in `bootstrap/app.php`, `everyMinute()` |

### ❌ Skipped

| Item | Reason |
|---|---|
| event_cancelled trigger | No cancel event endpoint exists. Out of scope — needs organizer cancel flow first. |
