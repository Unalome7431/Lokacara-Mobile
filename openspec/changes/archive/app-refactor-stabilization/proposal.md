## Why

Lokacara-Mobile has completed backend integration with all API endpoints connected, but the app still carries significant technical debt from development: hardcoded data, mock values, placeholder assets, temporary logic, and UI inconsistencies remain throughout the codebase. These artifacts prevent the app from being production-ready, create maintenance overhead, and cause mismatches between displayed data and actual backend responses. This change systematically eliminates all development-stage artifacts and stabilizes the application for production deployment.

## What Changes

- Remove all hardcoded text, images, lists, IDs, labels, status values, counts, statistics, and configuration values; replace with API-driven or database-backed sources
- Standardize data mapping across API responses, models, repositories, and UI layers to ensure single sources of truth
- Restructure project architecture: remove dead code, unused components, deprecated utilities, obsolete services, and redundant logic
- Improve separation of concerns and standardize naming conventions and coding patterns
- Optimize performance: eliminate unnecessary recompositions, redundant API requests, excessive queries, and memory leaks
- Enhance UX: improve loading, empty, and error states with proper retry mechanisms
- Audit and fix existing bugs: UI inconsistencies, navigation issues, state synchronization problems, API integration gaps, and validation issues
- Remove all development-only code, debugging artifacts, and temporary implementations

## Capabilities

### New Capabilities

- data-source-cleanup: Identify and replace all hardcoded data, mock values, and placeholder content with backend-driven or database-backed sources across all screens
- data-consistency: Standardize data mapping and flow between API DTOs, domain models, repositories, ViewModels, and UI composables; establish single sources of truth
- architecture-cleanup: Remove dead code, unused imports, deprecated utilities, obsolete services, and redundant logic; restructure project for better separation of concerns and consistent naming
- performance-optimization: Eliminate unnecessary recompositions, redundant network requests, excessive database queries, and memory leaks; improve caching and loading strategies
- ux-reliability: Improve loading, empty, error, and retry states across all screens; handle slow networks and failures gracefully
- bug-audit: Comprehensive review of UI inconsistencies, navigation issues, state synchronization, API integration gaps, validation issues; fix all discovered defects
- production-readiness: Remove development-only implementations, debugging artifacts, and temporary code; verify production deployment readiness

### Modified Capabilities

*(None — no existing specs in openspec/specs/)*

## Impact

- **Codebase (app/src/)**: Nearly every package will be touched — screens, ViewModels, repositories, models, DTOs, DI modules, components, theme, navigation
- **API Layer**: No new endpoints needed, but response handling and error recovery will be refined
- **Data Layer**: DataStore managers, bookmark manager, session manager may see consolidation
- **DI**: AppModule and NetworkModule may be refined but no structural changes expected
- **UI**: Significant churn in screens and components to replace hardcoded content with dynamic sources
- **Dependencies**: No new external dependencies; existing stack (Compose, Hilt, Retrofit, Moshi, Coil, DataStore) unchanged
- **Breaking Changes**: None expected — this is a refactor that preserves all existing functionality while improving implementation quality
