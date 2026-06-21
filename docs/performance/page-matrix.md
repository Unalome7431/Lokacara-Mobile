# Android Performance Page Matrix

## Reference Configuration

- Build: `debug` for functional iteration, followed by a release-like build for final comparison.
- Device: Android Studio AVD `Pixel_5`, cold boot, fixed emulator configuration.
- Dataset: the same authenticated account and backend dataset before and after optimization.
- Network: the same host connection; network-dependent timings are reported separately from render timings.
- Cache conditions:
  - Cold: app force-stopped and OS page/image caches not assumed warm.
  - Warm: app process retained and the target route visited once before measurement.
- Repetitions: at least five startup runs and three scroll/navigation runs; report median and worst observed run.
- Package/activity: `com.app.lokacara/.MainActivity`.

## Critical Journeys

| Journey | Route(s) | Preconditions | Actions | Primary observations |
| --- | --- | --- | --- | --- |
| Cold/warm startup | Onboarding/Login/Home | Onboarding completed; authenticated for Home run | Force-stop, launch, wait for first stable frame | `am start -W`, slow/frozen frames |
| Navbar switching | Home, Explore, Create Event, Tickets, Profile | Authenticated, each destination visited once for warm run | Switch through all navbar destinations and return Home | Transition frame stats, duplicate loads, retained state |
| Home feed | Home | Feed has multiple categories and images | Fling to the bottom, load more, return to top, refresh | Slow frames, image decode, stable cards, duplicate pagination |
| Explore list/grid | Explore | Search returns at least two pages | Fling list, load next page, toggle grid/list, apply sort/filter | Slow frames, viewport retention, duplicate IDs/requests |
| Explore search | Explore | Network available | Type a query rapidly, replace it, clear it | Debounce/cancellation, stale result ownership, input latency |
| Event detail | Event Detail | Select an event with poster/location/action state | Open, scroll full detail, bookmark/share, navigate back | Navigation/render timing, image decode, action correctness |
| Create/edit event | Create Event | Organizer account; existing event for edit path | Fill each section, select image/location, scroll, publish validation | Field recomposition, input latency, retained form state |
| Organizer attendees | My Events, Attendees | Owned event with more than one attendee page | Open attendees, search/filter, load more, toggle attendance | List identity, pagination, row-only updates |
| Tickets/certificates | Tickets, Certificates | Account with upcoming/history/certificate data | Switch sections, scroll, download certificate | Dashboard duplication, media loading, row progress updates |
| Profile/settings | Profile, Edit Profile, Settings | Authenticated profile with avatar and hosted events | Scroll profile, open/edit profile, return, open settings | State retention, avatar decode, navigation frames |
| Certificate management | Certificate Management | Finished owned event with present attendees | Pick template, adjust controls, scroll preview | Preview decode, file IO, control latency |
| QR scanner | QR Scan | Camera permission granted | Open scanner, scan/wait, leave and reopen | Camera startup, analysis frames, resource disposal |

## Full Route Smoke Matrix

| Group | Routes/screens | Required checks |
| --- | --- | --- |
| Entry/auth | Onboarding, Login, Register | Input, validation, loading, Google auth entry, back navigation |
| Main tabs | Home, Explore, Create Event, Tickets, Profile | Consistent transition, retained tab state, no duplicate initialization |
| Event | Event Detail, Bookmark, Saved Events | Scroll, media, bookmark, participant/organizer actions |
| Organizer | My Events, Attendees, QR Scan, Certificate Management, Edit Event | Stable lists, pagination, camera lifecycle, forms, uploads |
| Profile | Edit Profile, Settings, Change Password | Form state, avatar, toggles, dialogs, account actions |
| Communication | Notification | Filter tabs, stable item identity, empty/loading/error states |
| Certificates | Certificates and ticket certificate flows | Stable media, download/progress/error states |
| Information | About, Help Center, Terms Conditions, Privacy Policy | Scroll, search/expand where applicable, back navigation |

## Measurement Commands

Use `scripts/performance/measure-android-performance.ps1`:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\performance\measure-android-performance.ps1 -Action Startup -Runs 5
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\performance\measure-android-performance.ps1 -Action ResetFrames
# Perform one journey on the emulator.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\performance\measure-android-performance.ps1 -Action DumpFrames -OutputPath docs/performance/results/baseline-home.txt
```

Frame output records total, slow, and frozen frames reported by Android. Trace/recomposition inspection is performed with Android Studio Layout Inspector/System Trace using the same journey and configuration.
