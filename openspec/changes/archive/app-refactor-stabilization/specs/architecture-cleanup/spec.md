## ADDED Requirements

### Requirement: Remove all dead code
All unused functions, classes, properties, imports, and files SHALL be identified and removed.

#### Scenario: Dead function detection
- **WHEN** a grep search for a function name finds zero usages outside its definition
- **THEN** the function SHALL be removed

### Requirement: Remove deprecated utilities and obsolete services
All utility classes and services superseded by newer implementations SHALL be removed.

#### Scenario: Obsolete manager removal
- **WHEN** a data manager class has been replaced by API integration
- **THEN** the old manager SHALL be removed

### Requirement: Standardize naming conventions
All code SHALL follow Kotlin conventions.

#### Scenario: Inconsistent naming fix
- **WHEN** a class or function name violates Kotlin conventions
- **THEN** it SHALL be renamed to conform
