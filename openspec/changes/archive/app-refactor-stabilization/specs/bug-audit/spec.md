## ADDED Requirements

### Requirement: Comprehensive UI consistency audit
Every screen SHALL be reviewed for visual consistency with theme definitions.

#### Scenario: Typography audit
- **WHEN** reviewing each screen
- **THEN** all text SHALL use MaterialTheme.typography styles

### Requirement: Navigation consistency audit
All navigation routes SHALL be verified for correctness.

#### Scenario: Route argument mismatch
- **WHEN** navigating to EventDetail with an eventId
- **THEN** the eventId SHALL be correctly passed and received

### Requirement: State synchronization audit
All ViewModel state SHALL be consistent with the UI at all times.

#### Scenario: Post-mutation state update
- **WHEN** the user joins or leaves an event
- **THEN** the UI SHALL immediately reflect the new state

### Requirement: API integration gap audit
Every screen SHALL be verified against actual API responses.

#### Scenario: Missing API field
- **WHEN** a screen expects a field from the API that does not exist
- **THEN** the field SHALL be handled with a default or the UI SHALL handle its absence
