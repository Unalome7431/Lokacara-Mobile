# Performance Baseline

Captured on 2026-06-21 before app performance edits.

## Environment

- AVD: `Pixel_5`, Android 17/API 37 system image, 1080x2340 at 440 dpi.
- Emulator: headless, 4096 MB RAM, 4 cores, software GPU.
- Build: current debug APK installed with `adb install -r -d -t`.
- Backend/account: existing authenticated account against the configured backend.
- Startup: five force-stop/cold activity launches.
- Scroll: repeated deterministic `adb shell input swipe` gestures after `dumpsys gfxinfo reset`.
- Organizer-only routes: opened with the existing notification route extra and event ID `1`; empty/error data is accepted for render baseline and no production data is mutated.

The software-rendered headless AVD produces high absolute frame times. These numbers are only valid for before/after comparison under the same AVD flags and interaction sequence.

## Results

| Journey | Frames | Janky frames | P50 | P90 | P95 | P99 |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Navbar destinations | 73 | 63 (86.30%) | 65 ms | 250 ms | 800 ms | 1100 ms |
| Explore scroll | 96 | 78 (81.25%) | 77 ms | 133 ms | 150 ms | 200 ms |
| Event Detail scroll | 112 | 85 (75.89%) | 65 ms | 125 ms | 133 ms | 150 ms |
| Create Event scroll | 87 | 59 (67.82%) | 61 ms | 101 ms | 117 ms | 200 ms |
| Profile scroll | 213 | 138 (64.79%) | 57 ms | 81 ms | 97 ms | 150 ms |
| Attendees empty/error route | 6 | 5 (83.33%) | 85 ms | 125 ms | 125 ms | 125 ms |
| QR camera active | 28 | 28 (100.00%) | 250 ms | 400 ms | 400 ms | 450 ms |
| Home empty/settled | 0 | 0 | n/a | n/a | n/a | n/a |
| Tickets empty/settled | 0 | 0 | n/a | n/a | n/a | n/a |

Cold startup `TotalTime` runs: 6959, 6812, 6823, 6724, and 6938 ms. Median: 6823 ms.

Raw `dumpsys gfxinfo` extracts are stored beside this summary as `baseline-*.txt`.

## Highest-Impact Hotspots

1. Lifecycle and invalidation scope
   - Screens consistently use `collectAsState`; Home has 12 observed flows, Explore and Create Event have 20 each, Event Detail has 10, and Tickets/Attendees have 9 each.
   - State changes can invalidate broad screen trees while destinations are not fully active.

2. Explore rendering and data ownership
   - List and grid duplicate most header/filter composition and wrap the full state in `AnimatedContent`.
   - Query/filter/pagination jobs require strict cancellation and latest-result ownership.
   - Explore baseline has 81.25% janky frames and P95 150 ms on the reference AVD.

3. Eager form and detail composition
   - Create Event uses one large vertically scrolling composition with many independent fields and media/map interactions.
   - Event Detail is a large multi-section screen with image and derived content work.

4. Shared media and animation policy
   - Coil enables crossfade globally while individual image requests and prefetch sizes vary.
   - Entry, shimmer, destination, image, and navbar animations can overlap during navigation or scroll.

5. Lazy item stability
   - Stable keys/content types are present on some event lists but missing from several notifications, categories, ticket sections, and supporting lists.
   - Loading and media placeholders are not governed by one stable-size contract.

6. Resource-heavy surfaces
   - QR camera produces the worst baseline at P95 400 ms and requires explicit analyzer/executor disposal.
   - Map/location callbacks and image/file processing need lifecycle and dispatcher review.

7. Startup and duplicated initialization
   - Debug cold startup median is 6823 ms on the reference AVD.
   - Main tab ViewModels perform independent initialization, dashboard loads, mapping, and image prefetch that must be checked for duplicate work after navigation.
