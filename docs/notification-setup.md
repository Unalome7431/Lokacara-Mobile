# Notification Setup

Android support is implemented for event lifecycle push notifications, but real delivery still needs backend triggers, email delivery, and FCM delivery.

## Android

1. Add the Firebase Android app for package `com.app.lokacara`.
2. Place `google-services.json` in `app/`.
3. Apply the Google Services plugin in `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.google.services)
}
```

The app already includes Firebase Messaging, notification permission handling, token sync, generic data-message parsing, and notification tap routing.

## Backend Contract

Push-token endpoints expected by Android:

```http
POST /api/user/push-tokens
Authorization: Bearer {token}
Content-Type: application/json

{ "token": "{fcm_token}", "platform": "android" }
```

```http
DELETE /api/user/push-tokens
Authorization: Bearer {token}
Content-Type: application/json

{ "token": "{fcm_token}" }
```

FCM payloads must be data-only:

```json
{
  "category": "registration_success",
  "target": "tickets",
  "event_id": "123",
  "title": "Pendaftaran berhasil",
  "body": "Kamu berhasil terdaftar di Seminar AI."
}
```

Supported `target` values:
- `event_detail`
- `tickets`
- `certificates`
- `notification`

Unknown targets fall back to the notification screen. `event_detail` payloads should include a positive `event_id`.

## Notification Categories

Email + push + in-app:
- `registration_success`
- `event_reminder`
- `event_updated` when schedule, location, or online link changes
- `event_cancelled`
- `certificate_available`

Push + in-app only:
- `host_new_registration`
- `host_registration_cancelled`
- `attendance_checked_in`
- `event_capacity_warning`
- `bookmarked_event_reminder`

Registration success email should stay brief: confirm the user is registered, optionally include event date/time, and keep QR/ticket tokens inside the app.

`GET /api/notifications` should keep the existing fields and may add these optional fields:

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

Automatic reminder schedule:
- H-7, H-3, H-1, and Hari H.
- Send at the same clock time as the event `start_datetime`.
- Use active registrations at send time.
- Send email for each active registration.
- Send push and create in-app notification only when `notifications_enabled = true`.
- Enforce idempotency per `event_id + user_id + reminder_offset`.

Backend still needs Firebase Admin SDK, mail configuration, a queue worker, and scheduler/cron.
