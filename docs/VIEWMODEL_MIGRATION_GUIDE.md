# ViewModel Migration Guide

This guide shows how to migrate existing ViewModels to the sealed state pattern used by Mihon.

## Before: Current Pattern

```kotlin
class LibraryViewModel(
    // ... dependencies
) : BaseViewModel(), LibraryState by state {
    
    // Multiple mutable states scattered throughout
    var isLoading by mutableStateOf(false)
    var books by mutableStateOf<List<Book>>(emptyList())
    var error by mutableStateOf<String?>(null)
    var searchQuery by mutableStateOf<String?>(null)
    
    init {
        scope.launch {
            isLoading = true
            try {
                books = getBooks()
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
}
```

**Problems:**
- Multiple mutable states cause multiple recompositions
- No clear loading/success/error states
- Compose can't optimize skipping

---

## After: Sealed State Pattern

```kotlin
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LibraryViewModel(
    // ... dependencies
) : BaseViewModel() {
    
    // Single sealed state
    sealed interface State {
        @Immutable
        data object Loading : State
        
        @Immutable
        data class Success(
            val books: ImmutableList<BookItem>,
            val categories: ImmutableList<Category>,
            val selectedBooks: Set<Long> = emptySet(),
            val searchQuery: String? = null,
            val isRefreshing: Boolean = false,
        ) : State
        
        @Immutable
        data class Error(val message: String) : State
    }
    
    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()
    
    // Helper to update only Success state
    private inline fun updateSuccessState(crossinline update: (State.Success) -> State.Success) {
        _state.update { current ->
            when (current) {
                is State.Loading -> current
                is State.Success -> update(current)
                is State.Error -> current
            }
        }
    }
    
    init {
        // Subscribe to data flows
        scope.launch {
            combine(
                getLibraryBooks.subscribe(),
                getCategories.subscribe(),
            ) { books, categories ->
                State.Success(
                    books = books.toImmutableList(),
                    categories = categories.toImmutableList(),
                )
            }
            .catch { e -> 
                _state.value = State.Error(e.message ?: "Unknown error")
            }
            .collect { _state.value = it }
        }
    }
    
    // Actions update the Success state
    fun toggleBookSelection(bookId: Long) {
        updateSuccessState { state ->
            val newSelection = if (bookId in state.selectedBooks) {
                state.selectedBooks - bookId
            } else {
                state.selectedBooks + bookId
            }
            state.copy(selectedBooks = newSelection)
        }
    }
    
    fun setSearchQuery(query: String?) {
        updateSuccessState { it.copy(searchQuery = query) }
    }
    
    fun refresh() {
        updateSuccessState { it.copy(isRefreshing = true) }
        scope.launch {
            try {
                refreshLibrary()
            } finally {
                updateSuccessState { it.copy(isRefreshing = false) }
            }
        }
    }
}
```

---

## Screen Usage

### CRITICAL: Always Use collectAsState()

The most common mistake when migrating to StateFlow is forgetting to collect the state reactively.

**WRONG - Using convenience accessors (snapshot values):**
```kotlin
@Composable
fun HistoryScreen(vm: HistoryViewModel) {
    // ❌ WRONG: These are snapshot values that don't trigger recomposition!
    val items = vm.histories  // Returns _state.value.histories - a snapshot
    val searchQuery = vm.searchQuery  // Returns _state.value.searchQuery - a snapshot
    
    // This will NOT update when state changes!
    LazyColumn {
        items(items) { ... }
    }
}
```

**CORRECT - Using collectAsState():**
```kotlin
@Composable
fun HistoryScreen(vm: HistoryViewModel) {
    // ✅ CORRECT: Collect state reactively
    val state by vm.state.collectAsState()
    val items = state.histories
    val searchQuery = state.searchQuery
    
    // This WILL update when state changes!
    LazyColumn {
        items(items) { ... }
    }
}
```

### Full Example

```kotlin
@Composable
fun LibraryScreen(viewModel: LibraryViewModel) {
    // Always collect state at the top of the composable
    val state by viewModel.state.collectAsState()
    
    when (val s = state) {
        is LibraryViewModel.State.Loading -> {
            LoadingScreen()
        }
        is LibraryViewModel.State.Success -> {
            LibraryContent(
                books = s.books,
                categories = s.categories,
                selectedBooks = s.selectedBooks,
                searchQuery = s.searchQuery,
                isRefreshing = s.isRefreshing,
                onBookClick = { /* navigate */ },
                onBookLongClick = viewModel::toggleBookSelection,
                onSearchQueryChange = viewModel::setSearchQuery,
                onRefresh = viewModel::refresh,
            )
        }
        is LibraryViewModel.State.Error -> {
            ErrorScreen(
                message = s.message,
                onRetry = viewModel::refresh,
            )
        }
    }
}
```

---

## Key Changes

### 1. Single StateFlow Instead of Multiple MutableStates

```kotlin
// Before
var isLoading by mutableStateOf(false)
var books by mutableStateOf<List<Book>>(emptyList())
var error by mutableStateOf<String?>(null)

// After
private val _state = MutableStateFlow<State>(State.Loading)
val state: StateFlow<State> = _state.asStateFlow()
```

### 2. @Immutable Annotation on Data Classes

```kotlin
@Immutable
data class Success(
    val books: ImmutableList<BookItem>,
    // ...
) : State
```

### 3. ImmutableList Instead of List

```kotlin
// Before
val books: List<Book>

// After
val books: ImmutableList<Book>

// Conversion
books.toImmutableList()
```

### 4. Helper Function for State Updates

```kotlin
private inline fun updateSuccessState(crossinline update: (State.Success) -> State.Success) {
    _state.update { current ->
        when (current) {
            is State.Loading -> current
            is State.Success -> update(current)
            is State.Error -> current
        }
    }
}
```

### 5. collectAsStateWithLifecycle in Composables

```kotlin
// Before
val books by viewModel.books.collectAsState()

// After
val state by viewModel.state.collectAsStateWithLifecycle()
```

---

## Migration Checklist

- [ ] Create sealed interface `State` with `Loading`, `Success`, `Error`
- [ ] Add `@Immutable` annotation to all state data classes
- [ ] Replace `List<T>` with `ImmutableList<T>`
- [ ] Replace multiple `mutableStateOf` with single `MutableStateFlow<State>`
- [ ] Create `updateSuccessState` helper function
- [ ] Update init block to emit states via Flow
- [ ] **Ensure async operations wait for `Success` state before updating** (see Bug #1 in MIGRATION_LESSONS_LEARNED.md)
- [ ] **Preserve all data transformations (filtering, sorting)** (see Bug #2)
- [ ] Update all action methods to use `updateSuccessState`
- [ ] Update Screen composable to use `when` on state
- [ ] Use `collectAsState()` (not `collectAsStateWithLifecycle` in KMP common code)
- [ ] Test loading, success, and error states
- [ ] Test all data transformations (filters, search, sort)

---

## Priority Order for Migration

### ✅ Completed Migrations

1. **BookDetailViewModel** ✅ - Most impactful, navigation freezing
2. **LibraryViewModel** ✅ - Main screen, affects first impression
3. **ReaderScreenViewModel** ✅ - Core reading experience
4. **ExploreViewModel** ✅ - Browse functionality
5. **HistoryViewModel** ✅ - History screen
6. **UpdatesViewModel** ✅ - Updates screen
7. **ExtensionViewModel** ✅ - Sources/Extensions list
8. **PopularBooksViewModel** ✅ - Community popular books
9. **AllReviewsViewModel** ✅ - Community reviews
10. **LeaderboardViewModel** ✅ - Reading leaderboard
11. **DonationLeaderboardViewModel** ✅ - Donation leaderboard
12. **GlobalSearchViewModel** ✅ - Global search

### Already Using StateViewModel Pattern

- **AITTSSettingsViewModel** - Uses StateViewModel base class
- **GradioTTSSettingsViewModel** - Uses StateViewModel base class
- **VoiceSelectionViewModel** - Uses StateViewModel base class
- **AuthViewModel** - Uses StateViewModel base class
- **ProfileViewModel** - Uses StateViewModel base class
- **DonationViewModel** - Uses StateViewModel base class
- **StatisticsViewModel** - Uses MutableStateFlow with sealed SyncState

### Using Simple mutableStateOf (Acceptable for Settings)

These ViewModels use `mutableStateOf` for simple dialog visibility flags or settings state.
This is acceptable for settings screens where the state is simple and doesn't require
complex loading/success/error handling:

- **AdvanceSettingViewModel** - Simple settings state
- **ReaderSettingScreenViewModel** - Reader preferences
- **SettingsReaderViewModel** - Dialog visibility flags
- **SettingsTrackingViewModel** - Dialog visibility flags
- **SettingsSecurityViewModel** - Dialog visibility flags
- **SettingsNotificationViewModel** - Dialog visibility flags
- **SettingsLibraryViewModel** - Dialog visibility flags
- **SettingsDownloadViewModel** - Dialog visibility flags
- **SettingsDataViewModel** - Dialog visibility flags
- **CategoryScreenViewModel** - Simple category list
- **BackupScreenViewModel** - Simple backup state
- **FontScreenViewModel** - Font preview state
- **TranslationSettingsViewModel** - Translation test state
- **SourceRepositoryViewModel** - Repository dialog state
- **DonationTriggerViewModel** - Donation prompt state

---

## Performance Benefits

| Metric | Before | After |
|--------|--------|-------|
| Recomposition count | Multiple per state change | Single per state change |
| State stability | Unstable (List) | Stable (ImmutableList) |
| Loading state | Implicit | Explicit |
| Error handling | Scattered | Centralized |
| Compose skipping | Limited | Optimized |
