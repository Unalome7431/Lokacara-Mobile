## ADDED Requirements

### Requirement: Consistent loading states
Every screen that fetches data from an API SHALL display a loading indicator while the request is in flight.

#### Scenario: Loading indicator on data fetch
- **WHEN** a screen begins loading data from the API
- **THEN** a loading spinner SHALL be displayed

### Requirement: Consistent empty states
Every screen that displays a list or collection SHALL show an appropriate empty state message when the data source returns zero items.

#### Scenario: Empty event list
- **WHEN** a screen has zero items to display
- **THEN** a user-friendly empty state message SHALL be shown

### Requirement: Consistent error states
Every screen that fetches data SHALL handle API errors gracefully with a retry option.

#### Scenario: Network error on screen load
- **WHEN** an API request fails due to network issues
- **THEN** the screen SHALL display an error message with a retry button

### Requirement: Retry mechanism on failure
Every failed API request that the user can trigger SHALL have an accessible retry mechanism.

#### Scenario: Manual retry after failure
- **WHEN** a network error occurs and the user taps retry
- **THEN** the failed request SHALL be re-executed
