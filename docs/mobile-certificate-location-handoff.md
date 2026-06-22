# Mobile Certificate and Location Backend Handoff

Android change `fix-mobile-event-certificate-experience` is usable before the backend rollout, with two deliberate fallbacks.

## Organizer Certificate State

Android first requests `GET /api/organizer/events/{event}/certificates`. Until that endpoint exists, it restores a private template copy and layout from DataStore keyed by user ID and event ID.

The backend follow-up `support-mobile-certificate-location-contracts` must return event eligibility, saved configuration, template availability, issued count, last issued time, and distribution status. The backend state becomes authoritative when available. Local fallback does not survive reinstall or another device.

## Canonical City Search

Android sends `location=<canonical city>` on every `/api/events/search` page. Until the backend applies this query, Android rejects results whose `city` or exact administrative address component does not equal the selected city.

This prevents venue-name false positives but cannot recover matching events omitted from the server's current 15-item page. The backend follow-up must persist `event.city` and apply exact normalized city filtering before pagination.

## Participant Certificate Files

Participant preview and download use authenticated `GET /api/events/{event}/certificate`. The dashboard certificate payload must continue including `event_registration.event.id`; direct public access to `file_url` is not required.
