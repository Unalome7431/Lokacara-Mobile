## ADDED Requirements

### Requirement: Identify all hardcoded display text
The system SHALL scan every screen for string literals used for display purposes that are not sourced from API responses.

#### Scenario: Grep-based hardcoded text scan
- **WHEN** the audit runs on a screen file
- **THEN** every string literal rendered as text is flagged for verification

### Requirement: Replace hardcoded text with API-driven content
Every hardcoded display string SHALL be replaced with data from the appropriate API response field.

#### Scenario: Event title on EventDetailScreen
- **WHEN** an event detail is loaded from the API
- **THEN** all fields SHALL display values from the API response

### Requirement: Eliminate hardcoded image URLs and media
All image URLs SHALL be loaded from backend-provided URLs, not hardcoded drawable references.

#### Scenario: Event card image
- **WHEN** an event card is displayed
- **THEN** the image SHALL be loaded from the API-provided imageUrl via Coil
