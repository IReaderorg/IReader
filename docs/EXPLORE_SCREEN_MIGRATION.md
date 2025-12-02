# Explore Screen Migration to Mihon Pattern

## Overview

This document describes the migration of the Explore screen to follow Mihon's StateScreenModel pattern for improved performance, reliability, and maintainability.

## Key Changes

### 1. Immutable State with StateFlow

**Before:**
```kotlin
// Mutable state scattered across multiple classes
open class ExploreStateImpl: ExploreState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var error by mutableStateOf<UiText?>(null)
    // ... many more mutable properties
}

class BooksState {
    var books : List<Book> by mutableStateOf(emptyList())
    var book : Book? by mutableStateOf(null)
}
```

**After:**
```kotlin
// Single immutable state class
@Immutable
data class ExploreScreenState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
    val layout: DisplayMode = DisplayMode.CompactGrid,
    val isSearchModeEnabled: Boolean = false,
    val searchQuery: String? = null,
    val catalog: CatalogLocal? = null,
    val books: List<Book> = emptyList(),
    // ... all state in one place
)

// ViewModel uses MutableStateFlow
private val _state = MutableStateFlow(ExploreScreenState())
val state: StateFlow<ExploreScreenState> = _state.asStateFlow()
```

### 2. Atomic State Updates

**Before:**
```kotlin
// Multiple state updates could cause race conditions
isLoading = true
error = null
booksState.books = emptyList()
```

**After:**
```kotlin
// Single atomic update
_state.update { 
    it.copy(
        isLoading = true,
        error = null,
        books = emptyList()
    )
}
```

### 3. Efficient Deduplication

**Before:**
```kotlin
// Called findDuplicateBook for ALL books on every page
val newBooks = items.mangas.fastMap { manga ->
    val book = manga.toBook(sourceId = sourceId)
    findDuplicateBook(book.title, book.sourceId) ?: book
}
```

**After:**
```kotlin
// Use HashSet for O(1) deduplication like Mihon
private val seenBooks = hashSetOf<String>()

val newBooks = pageInfo.mangas
    .asSequence()
    .filter { manga -> 
        val key = "${manga.key}_$sourceId"
        seenBooks.add(key)  // Returns false if already seen
    }
    .map { manga -> 
        val book = manga.toBook(sourceId = sourceId)
        findDuplicateBook(book.title, book.sourceId) ?: book
    }
    .toList()
```

### 4. Derived State Properties

**Before:**
```kotlin
// Computed in composable, causing recomposition
when {
    vm.isLoading && vm.page == 1 -> { /* loading */ }
    vm.error != null && vm.page == 1 -> { /* error */ }
    // ...
}
```

**After:**
```kotlin
// Computed once in state class
@Immutable
data class ExploreScreenState(...) {
    @Stable
    val isInitialLoading: Boolean
        get() = isLoading && page == 1 && books.isEmpty()
    
    @Stable
    val hasContent: Boolean
        get() = books.isNotEmpty()
    
    @Stable
    val isErrorWithNoContent: Boolean
        get() = error != null && books.isEmpty() && !isLoading
}
```

### 5. Proper Job Cancellation

**Before:**
```kotlin
private var getBooksJob: Job? = null

fun loadItems(reset: Boolean = false) {
    getBooksJob?.cancel()
    // ... but no cleanup in onCleared
}
```

**After:**
```kotlin
private var loadJob: Job? = null

fun loadItems(reset: Boolean = false) {
    loadJob?.cancel()
    // ...
}

override fun onDestroy() {
    loadJob?.cancel()
    seenBooks.clear()
    super.onDestroy()
}
```

## Performance Improvements

### 1. Reduced Recompositions
- Immutable state with `@Immutable` annotation
- Stable key functions for list items
- Memoized click handlers

### 2. Efficient Memory Usage
- HashSet for deduplication instead of repeated database queries
- Sequence operations for lazy evaluation
- Proper cleanup in onDestroy

### 3. Faster Loading
- Atomic state updates prevent UI flicker
- Derived properties computed once
- Efficient pagination with proper cancellation

## Migration Checklist

- [x] Create immutable `ExploreScreenState` data class
- [x] Migrate to `MutableStateFlow` in ViewModel
- [x] Implement atomic state updates with `update { }`
- [x] Add HashSet-based deduplication
- [x] Add derived state properties
- [x] Implement proper job cancellation
- [x] Update ExploreScreen to use new state
- [x] Update ExploreScreenSpec to collect state
- [x] Update BrowseTopAppBar to accept ViewModel
- [x] Update DI module
- [x] Create integration tests

## Files Changed

1. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/viewmodel/ExploreState.kt`
   - New immutable `ExploreScreenState` data class
   - `ExploreDialog` sealed interface
   - `ExploreListing` sealed class

2. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/viewmodel/ExploreViewModel.kt`
   - Migrated to StateFlow pattern
   - Added HashSet deduplication
   - Proper job cancellation
   - `BooksStateHolder` for backward compatibility

3. `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/ExploreScreenSpec.kt`
   - Collects state with `collectAsState()`
   - Passes state to ExploreScreen
   - Improved error handling

4. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/ExploreScreen.kt`
   - Accepts `ExploreScreenState` parameter
   - Uses derived state properties
   - Added `ExploreScreenError` composable

5. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/BrowseTopAppBar.kt`
   - Updated to accept `ExploreViewModel` instead of `ExploreState`

6. `presentation/src/commonMain/kotlin/ireader/presentation/core/di/PresentationModules.kt`
   - Updated DI registration for new ViewModel signature

## Testing

Integration tests are available in:
- `android/src/androidTest/kotlin/org/ireader/app/ExploreScreenIntegrationTest.kt`
- `android/src/androidTest/kotlin/org/ireader/app/ExploreNavigationFlowTest.kt`

Run tests with:
```bash
./gradlew :android:connectedAndroidTest
```

## Backward Compatibility

The `BooksStateHolder` class provides backward compatibility for code that still uses the old `booksState.books` pattern. This allows gradual migration of dependent code.

```kotlin
@Stable
class BooksStateHolder(private val stateFlow: MutableStateFlow<ExploreScreenState>) {
    var books: List<Book>
        get() = stateFlow.value.books
        set(value) { stateFlow.update { it.copy(books = value) } }
    
    fun replaceBook(book: Book?) { /* ... */ }
    fun empty() { /* ... */ }
}
```
