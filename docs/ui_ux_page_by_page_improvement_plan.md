# Lokacara Mobile UI/UX Audit & Improvement Plan (Per Page Breakdown)

This document contains a comprehensive breakdown and improvement plan for every major screen in the Lokacara Mobile application.

---

## 1. ExploreScreen (`ExploreScreen.kt`)
**Current State & Purpose:**
Allows users to discover events via search, filters (price, date, category), and grid/list toggles. Uses a collapsible search bar and `LazyVerticalGrid` / `LazyColumn`.

**UX Issues:**
- **Friction in Filtering:** The user has to expand the search bar to access location and text search, but category chips are below the search bar. The visual hierarchy when the search is expanded pushes actual results down significantly.
- **Loading State:** The shimmer effect is functional but can be jarring when switching between grid and list views because the shimmer doesn't always match the selected layout perfectly.
- **Empty States:** When filters yield no results, the empty state might not provide actionable ways to clear specific filters easily (only a generic clear).

**UI Issues:**
- **Inconsistent Spacing:** The padding around the grid items (`PaddingValues(horizontal = 24.dp, vertical = 4.dp)`) feels slightly cramped vertically compared to the horizontal margins.
- **Search Bar Affordance:** The collapsed search bar doesn't clearly indicate that tapping it expands a complex filter menu.

**Improvement Plan:**
- [ ] **UX Enhancement:** Implement a Bottom Sheet or persistent Filter Chips below the app bar for quick filtering (Price, Date, Location) without requiring a full vertical expansion that shifts content.
- [ ] **UI Polish:** Adjust grid spacing to `12.dp` or `16.dp` uniform padding for better breathing room (`GridCells.Adaptive` instead of fixed 2 columns for better tablet support).
- [ ] **Empty State Component:** Enhance the empty state with a "Clear Filters" button directly embedded within the empty view.

---

## 2. TicketsScreen (`TicketsScreen.kt`)
**Current State & Purpose:**
Manages user tickets for both "Mendatang" (Upcoming) and "Riwayat" (History). Requires authentication to view. Contains a custom `TicketTabRow`.

**UX Issues:**
- **Auth Gate:** The unauthenticated state shows a generic login button. If a user taps the "Tickets" tab accidentally, they hit a hard wall.
- **Tab Navigation:** The numbers inside the tabs (e.g., "Mendatang (0)") can cause text truncation on smaller screens if the translation strings are too long.
- **Error Handling:** Errors are shown as full-screen blocking views or inline text, which disrupts the tab context.

**UI Issues:**
- **Card Design:** `SmallUpcomingEventCard` might lack contrast against the `SvgBackground` if shadows are too subtle.
- **QR Code Dialog:** The modal for showing the QR code (if applicable) can sometimes lack a clear "Close" affordance if the user taps outside.

**Improvement Plan:**
- [ ] **UX Enhancement:** Add a more engaging unauthenticated empty state (e.g., an illustration showing ticket benefits) rather than a blank screen with a button.
- [ ] **UI Polish:** Remove the explicit count from the Tab titles if it exceeds 99, or use a badge component instead of appending text.
- [ ] **Component Standardization:** Standardize `EmptyStateView` and `TicketLoadingState` to use the global theme colors defined in `Color.kt`.

---

## 3. ProfileScreen (`ProfileScreen.kt`)
**Current State & Purpose:**
Serves as the hub for user identity, my events, saved events, certificates, settings, and logout.

**UX Issues:**
- **Navigation Depth:** The shortcut tiles (My Events, Saved Events, Certificates) are very prominent, which is good, but "Edit Profile" is mixed in as a tile rather than being accessible directly by tapping the user's avatar.
- **Logout Placement:** Logout is a standard row at the bottom. Accidental taps can trigger immediate logout without confirmation.

**UI Issues:**
- **Identity Card:** The `ProfileIdentityCard` looks modern but the contrast of text against the white background might be low if the user has no avatar and a placeholder is used.
- **Tile Alignment:** The 2x2 grid of `ProfileShortcutTile` might break or stretch uncomfortably on tablets or foldables.

**Improvement Plan:**
- [ ] **UX Enhancement:** Add an "Edit" icon directly on the `ProfileIdentityCard` avatar to map to user mental models.
- [ ] **UX Enhancement:** Add an `AlertDialog` confirmation before executing the logout action.
- [ ] **UI Polish:** Wrap the grid of tiles in a `FlowRow` or adaptive layout for better responsiveness.

---

## 4. HomeScreen (`HomeScreen.kt`)
**Current State & Purpose:**
The main dashboard featuring a greeting, search quick-access, event categories, trending events, and nearby events.

**UX Issues:**
- **Information Overload:** Heavy nesting of horizontal scrolling rows inside a vertical scrolling column. If the user scrolls horizontally, vertical scroll can feel locked.
- **Offline Resilience:** If the API fails, the entire home screen shows a generic feed error, hiding cached data.

**UI Issues:**
- **Category Chips:** Hardcoded styling in `CategoryChip` sometimes doesn't align with the newly standardized `LokacaraTextField` border styles.

**Improvement Plan:**
- [ ] **UX Enhancement:** Implement offline caching using Room/DataStore so the `HomeScreen` shows stale data while fetching, rather than a loading spinner or error.
- [ ] **UI Polish:** Standardize horizontal paddings. Ensure all `LazyRow` components have `contentPadding` that matches the global screen padding (e.g., `24.dp`) but clip to bounds properly.

---

## 5. EventDetailScreen (`EventDetailScreen.kt`)
**Current State & Purpose:**
Displays full details of an event, including description, schedule, location, and the "Beli Tiket" / "Gabung Event" CTA.

**UX Issues:**
- **CTA Visibility:** The "Beli Tiket" button might require scrolling to the bottom of the screen on devices with small heights.
- **Readability:** Massive blocks of text in the description can be fatiguing.

**UI Issues:**
- **Image Header:** The collapsing toolbar effect relies on standard scroll state rather than `NestedScrollConnection`, causing slightly jerky image scaling.

**Improvement Plan:**
- [ ] **UX Enhancement:** Move the "Beli Tiket" CTA to a persistent `BottomAppBar` or `Scaffold(bottomBar = {})` so it is always visible regardless of scroll position.
- [ ] **UI Polish:** Use `AnimatedVisibility` for a "Read More" toggle on long descriptions to keep the layout compact.

---

## 6. Authentication (Login / Register / Change Password)
**Current State:** Refactored to use `LokacaraTextField` and dynamic weights.
**Remaining Improvements:**
- [ ] **UX Enhancement:** Implement inline validation (e.g., green checkmark when password meets criteria during typing, rather than waiting for API submit).
- [ ] **UI Polish:** Add transitions between Login and Register screens using Compose Navigation animations (`slideInHorizontally`).

---

## 7. CreateEventScreen (`CreateEventScreen.kt`)
**Current State:** Dismantled into modular components.
**Remaining Improvements:**
- [ ] **UX Enhancement:** Convert the single long scrolling form into a multi-step wizard (Stepper) (e.g., Step 1: Info, Step 2: Date/Location, Step 3: Publish).
- [ ] **UI Polish:** Extract the Date and Time pickers into `ModalBottomSheet` instead of `AlertDialog` for a more modern Android 14+ feel.

---

## Summary of Next Steps for Development Team
1. **Components:** Finalize migration of all remaining generic Compose `Button` to a standardized `LokacaraButton`.
2. **Navigation:** Implement Type-Safe Navigation for Compose to prevent runtime crashes caused by string-based route arguments.
3. **Accessibility:** Run a full TalkBack audit on the `TicketsScreen` and `ExploreScreen` grid items.