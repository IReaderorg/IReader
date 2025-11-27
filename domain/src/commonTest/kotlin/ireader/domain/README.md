# Domain Layer Unit Tests

This directory contains unit tests for the domain layer of the IReader application.

## Test Coverage

### Use Cases
- **Book Management**: AddToLibrary, RemoveFromLibrary, UpdateBook
- **Chapter Management**: UpdateChapterReadStatus, GetChapters
- **Category Management**: CreateCategory, DeleteCategory, GetCategories
- **Authentication**: SignIn, SignUp, SignOut
- **History**: GetHistory, UpdateHistory, DeleteHistory, ClearHistory
- **Sync**: SyncBookToRemote, CheckSyncAvailability
- **Catalogs**: ExtensionManager, ExtensionSecurityManager

### Models
- **Book**: Entity tests for book data model
- **Chapter**: Entity tests for chapter data model

### Utilities
- **BookIdNormalizer**: URL normalization and ID generation tests
- **JSFilterConverter**: LNReader to IReader filter conversion tests

## Running Tests

### Run all domain tests
```bash
./gradlew :domain:testDebugUnitTest
```

### Run specific test class
```bash
./gradlew :domain:testDebugUnitTest --tests "ireader.domain.usecases.book.AddToLibraryTest"
```

### Run tests with coverage
```bash
./gradlew :domain:testDebugUnitTest :domain:jacocoTestReport
```

## Test Structure

Tests follow the AAA (Arrange-Act-Assert) pattern:
- **Given**: Setup test data and mocks
- **When**: Execute the code under test
- **Then**: Verify the results

## Mocking

Tests use MockK for mocking dependencies:
- `mockk<Type>()`: Create a mock
- `coEvery { }`: Setup suspend function behavior
- `coVerify { }`: Verify suspend function calls
- `just Runs`: Mock void functions

## Test Naming Convention

Test names follow the pattern:
```
`methodName should expectedBehavior when condition`
```

Examples:
- `await should add book to library successfully`
- `invoke should mark chapter as read`
- `signIn should return failure with invalid credentials`
