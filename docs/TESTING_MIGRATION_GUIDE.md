# Testing, Migration, and Quality Assurance Guide

## Overview

This guide documents the comprehensive testing strategy, migration process, and quality assurance measures for the Mihon-inspired improvements to IReader.

## Table of Contents

1. [Testing Strategy](#testing-strategy)
2. [Migration Guide](#migration-guide)
3. [Quality Assurance](#quality-assurance)
4. [Rollback Plan](#rollback-plan)
5. [Feature Flags](#feature-flags)

## Testing Strategy

### Unit Tests

#### Repository Tests

Unit tests for repository implementations focus on:
- Data access operations (CRUD)
- Error handling and recovery
- Flow-based reactive queries
- Partial updates with Update classes
- Transaction handling

**Location**: `data/src/commonTest/kotlin/ireader/data/repository/`

**Example Test Structure**:
```kotlin
class BookRepositoryTest {
    private lateinit var repository: BookRepository
    private lateinit var handler: DatabaseHandler
    
    @BeforeEach
    fun setup() {
        handler = mockk()
        repository = BookRepositoryImpl(handler)
    }
    
    @Test
    fun `getBookById returns book when found`() = runTest {
        // Given
        val bookId = 1L
        val expectedBook = createTestBook(id = bookId)
        coEvery { handler.awaitOne<Book>(any()) } returns expectedBook
        
        // When
        val result = repository.getBookById(bookId)
        
        // Then
        result shouldBe expectedBook
    }
}
```

#### Use Case Tests

Unit tests for use cases/interactors focus on:
- Business logic validation
- Error handling and logging
- Repository interaction
- Data transformation

**Location**: `domain/src/commonTest/kotlin/ireader/domain/usecases/`

#### State Model Tests

Unit tests for StateScreenModel implementations focus on:
- State transitions
- Loading states
- Error states
- Success states with data
- User interaction handling

**Location**: `presentation/src/commonTest/kotlin/ireader/presentation/screens/`

### Integration Tests

Integration tests verify:
- DatabaseHandler interactions
- End-to-end data flow
- Repository + Use Case integration
- Screen + StateScreenModel integration

**Location**: `data/src/commonTest/kotlin/ireader/data/integration/`

### UI Tests

UI tests using Compose Testing verify:
- All screen states (loading, error, success, empty)
- Responsive behavior across screen sizes
- User interactions
- Navigation flows
- Accessibility compliance

**Location**: `presentation/src/androidTest/kotlin/ireader/presentation/ui/`

### Test Coverage Goals

- **New Code**: Minimum 90% coverage
- **Repository Layer**: 95% coverage
- **Use Case Layer**: 90% coverage
- **State Models**: 85% coverage
- **UI Components**: 80% coverage

## Migration Guide

### Phase 1: Preparation

1. **Backup Current State**
   - Create database backup
   - Document current repository usage
   - Identify all repository injection points

2. **Feature Flag Setup**
   - Enable `USE_NEW_REPOSITORIES` flag (default: false)
   - Enable `USE_STATE_SCREEN_MODEL` flag (default: false)
   - Enable `USE_NEW_UI_COMPONENTS` flag (default: false)

### Phase 2: Repository Migration

#### Step 1: Create Consolidated Repositories

New consolidated repository structure:
- `BookRepository` - Book data operations
- `ChapterRepository` - Chapter data operations
- `CategoryRepository` - Category management
- `DownloadRepository` - Download management
- `HistoryRepository` - Reading history
- `LibraryRepository` - Library operations
- `TrackingRepository` - External tracking
- `SourceRepository` - Source management

#### Step 2: Deprecate Old Repositories

Mark old repositories as deprecated:
```kotlin
@Deprecated(
    message = "Use consolidated BookRepository instead",
    replaceWith = ReplaceWith("BookRepository"),
    level = DeprecationLevel.WARNING
)
interface OldBookRepository { ... }
```

#### Step 3: Update Dependency Injection

Update Koin modules to provide new repositories:
```kotlin
single<BookRepository> { 
    if (get<FeatureFlags>().useNewRepositories) {
        NewBookRepositoryImpl(get())
    } else {
        LegacyBookRepositoryAdapter(get())
    }
}
```

#### Step 4: Gradual Migration

1. Enable feature flag for specific screens
2. Test thoroughly
3. Expand to more screens
4. Monitor for issues
5. Full rollout when stable

### Phase 3: State Management Migration

#### Step 1: Create StateScreenModel Base Class

```kotlin
abstract class IReaderStateScreenModel<T>(
    initialState: T
) : StateScreenModel<T>(initialState) {
    
    protected fun updateState(update: (T) -> T) {
        mutableState.update(update)
    }
    
    protected fun launchIO(block: suspend CoroutineScope.() -> Unit) {
        screenModelScope.launchIO(block)
    }
}
```

#### Step 2: Migrate ViewModels to StateScreenModels

1. Identify all ViewModel classes
2. Create equivalent StateScreenModel
3. Update screen composables
4. Test state transitions
5. Remove old ViewModel

### Phase 4: UI Component Migration

#### Step 1: Replace Error Screens

Replace `ErrorScreen` with `IReaderErrorScreen`:
```kotlin
// Old
ErrorScreen(
    errorMessage = error,
    retry = { viewModel.retry() }
)

// New
IReaderErrorScreen(
    message = error,
    actions = persistentListOf(
        ErrorScreenAction(
            stringRes = MR.strings.retry,
            icon = Icons.Default.Refresh,
            onClick = { screenModel.retry() }
        )
    )
)
```

#### Step 2: Replace Loading Screens

Replace `LoadingScreen` with `IReaderLoadingScreen`

#### Step 3: Replace Scaffold

Replace `IScaffold` with `IReaderScaffold`

### Phase 5: Cleanup

1. Remove deprecated repositories
2. Remove old ViewModels
3. Remove old UI components
4. Update documentation
5. Remove feature flags

## Quality Assurance

### Code Quality Checks

#### Detekt Configuration

Enhanced Detekt rules in `config/detekt.yml`:
```yaml
complexity:
  active: true
  CyclomaticComplexMethod:
    active: true
    threshold: 15
  LongMethod:
    active: true
    threshold: 60
  LongParameterList:
    active: true
    threshold: 6

style:
  active: true
  MagicNumber:
    active: true
  MaxLineLength:
    active: true
    maxLineLength: 120

naming:
  active: true
  FunctionNaming:
    active: true
  ClassNaming:
    active: true
```

#### ktlint Configuration

Automated code formatting with stricter rules:
```kotlin
ktlint {
    version.set("0.50.0")
    android.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(false)
}
```

### Documentation Standards

#### KDoc Requirements

All public APIs must have KDoc documentation:
```kotlin
/**
 * Repository for managing book data operations.
 *
 * This repository provides methods for CRUD operations on books,
 * including reactive Flow-based queries and partial updates.
 *
 * @see Book
 * @see BookUpdate
 */
interface BookRepository {
    /**
     * Retrieves a book by its unique identifier.
     *
     * @param id The unique identifier of the book
     * @return The book with the specified ID
     * @throws DatabaseError if the book cannot be retrieved
     */
    suspend fun getBookById(id: Long): Book
}
```

### Performance Benchmarks

#### Baseline Metrics

Current performance metrics (before improvements):
- List scrolling: 60 FPS (baseline)
- Screen loading: 500ms average
- Memory usage: 150MB average
- Database query time: 50ms average

#### Target Metrics

Performance targets (after improvements):
- List scrolling: 60 FPS (maintained with FastScrollLazyColumn)
- Screen loading: 300ms average (40% improvement)
- Memory usage: 100MB average (33% reduction)
- Database query time: 35ms average (30% improvement)

#### Measurement Tools

- **Macrobenchmark**: For measuring app startup and screen loading
- **LeakCanary**: For detecting memory leaks
- **Android Profiler**: For CPU, memory, and network profiling
- **Compose Layout Inspector**: For UI performance analysis

## Rollback Plan

### Rollback Triggers

Rollback should be initiated if:
- Critical bugs affecting core functionality
- Performance degradation > 20%
- Crash rate increase > 5%
- User-reported issues > threshold
- Data corruption detected

### Rollback Procedure

#### Step 1: Disable Feature Flags

```kotlin
// In FeatureFlags.kt
object FeatureFlags {
    const val USE_NEW_REPOSITORIES = false
    const val USE_STATE_SCREEN_MODEL = false
    const val USE_NEW_UI_COMPONENTS = false
}
```

#### Step 2: Revert Database Migrations

```kotlin
// Run rollback migration
database.execSQL("DROP TABLE IF EXISTS new_books")
database.execSQL("ALTER TABLE books_backup RENAME TO books")
```

#### Step 3: Restore Old Code Paths

```kotlin
single<BookRepository> { 
    LegacyBookRepositoryImpl(get()) // Use legacy implementation
}
```

#### Step 4: Verify Rollback

1. Test core functionality
2. Verify data integrity
3. Check performance metrics
4. Monitor crash reports

### Rollback Testing

Regular rollback drills should be conducted:
- Monthly rollback simulation
- Document rollback time
- Identify rollback issues
- Update rollback procedures

## Feature Flags

### Implementation

```kotlin
/**
 * Feature flags for gradual migration to new architecture.
 */
object FeatureFlags {
    /**
     * Enable new consolidated repository pattern.
     * Default: false (use legacy repositories)
     */
    var useNewRepositories: Boolean = false
    
    /**
     * Enable StateScreenModel pattern for state management.
     * Default: false (use legacy ViewModels)
     */
    var useStateScreenModel: Boolean = false
    
    /**
     * Enable new Material Design 3 UI components.
     * Default: false (use legacy components)
     */
    var useNewUIComponents: Boolean = false
    
    /**
     * Enable responsive design with TwoPanelBox.
     * Default: false (use single-panel layouts)
     */
    var useResponsiveDesign: Boolean = false
    
    /**
     * Enable FastScrollLazyColumn for list performance.
     * Default: false (use standard LazyColumn)
     */
    var useFastScrollLists: Boolean = false
}
```

### Usage in Code

```kotlin
// In repository injection
single<BookRepository> { 
    if (FeatureFlags.useNewRepositories) {
        NewBookRepositoryImpl(get())
    } else {
        LegacyBookRepositoryAdapter(get())
    }
}

// In screen composables
@Composable
fun BookDetailScreen() {
    if (FeatureFlags.useStateScreenModel) {
        val screenModel = rememberScreenModel { BookDetailScreenModel() }
        // Use StateScreenModel
    } else {
        val viewModel = viewModel<BookDetailViewModel>()
        // Use ViewModel
    }
}
```

### Gradual Rollout Strategy

1. **Week 1**: Enable for internal testing (10% of users)
2. **Week 2**: Enable for beta testers (25% of users)
3. **Week 3**: Enable for early adopters (50% of users)
4. **Week 4**: Enable for all users (100%)

### Monitoring

Monitor key metrics during rollout:
- Crash rate
- Performance metrics
- User feedback
- Error logs
- Database integrity

## Continuous Integration

### GitHub Actions Workflow

```yaml
name: Test and Quality Checks

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Detekt
        run: ./gradlew detekt
      - name: Run ktlint
        run: ./gradlew ktlintCheck
```

### Quality Gates

Pull requests must pass:
- All unit tests
- All integration tests
- Code coverage > 90% for new code
- Detekt checks
- ktlint formatting
- No critical security issues

## Conclusion

This comprehensive testing, migration, and quality assurance strategy ensures:
- High code quality and maintainability
- Safe migration from old to new architecture
- Ability to rollback if issues arise
- Continuous monitoring and improvement
- Gradual rollout with minimal risk

For questions or issues, refer to the development team or create an issue in the project repository.
