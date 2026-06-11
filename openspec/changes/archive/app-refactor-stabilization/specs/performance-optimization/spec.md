## ADDED Requirements

### Requirement: Eliminate unnecessary recompositions
All composable functions SHALL use remember and derivedStateOf to prevent unnecessary recompositions.

#### Scenario: Stable state reference
- **WHEN** a composable reads a StateFlow value
- **THEN** it SHALL use collectAsStateWithLifecycle and avoid recreating lambdas

### Requirement: Eliminate redundant API requests
No API endpoint SHALL be called more times than necessary.

#### Scenario: Concurrent identical requests
- **WHEN** two composables request the same data simultaneously
- **THEN** only one network request SHALL be made

### Requirement: Optimize DataStore reads
DataStore preferences SHALL be read with appropriate caching.

#### Scenario: Repeated preference read
- **WHEN** a ViewModel reads the same preference in multiple functions
- **THEN** the value SHALL be read once and cached
