## ADDED Requirements

### Requirement: Standardize data flow across all layers
Every data type SHALL follow a consistent flow: API DTO to Domain Model to Repository to ViewModel to UI.

#### Scenario: Event data flow
- **WHEN** event data is loaded from the API
- **THEN** it SHALL pass through the complete pipeline without shortcuts

### Requirement: Eliminate duplicate data sources
No domain model field SHALL have more than one source of truth.

#### Scenario: Bookmarked events
- **WHEN** displaying bookmarked events
- **THEN** the data SHALL come from a single source

### Requirement: Consistent data mapping
All API DTO to domain model mappings SHALL use the existing Mappers.kt functions.

#### Scenario: New field mapping
- **WHEN** a new field is added to an API DTO
- **THEN** the mapping SHALL be added to the corresponding mapper function
