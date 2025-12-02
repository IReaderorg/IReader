# Migration Lessons Learned

This document captures bugs encountered during the BookDetailViewModel migration to sealed state pattern, and how to avoid them in future migrations.

---

## Bug #1: Initial Remote Fetch Timing

### Problem
When migrating from multiple `mutableStateOf` to a single `StateFlow<SealedState>`, the initial remote data fetch was triggered before the state transitioned from `Loading` to `Success`.

### Root Cause
```kotlin
// OLD CODE (BROKEN)
private fun initializeBook(bookId: Long) {
    scope.launch {
        val book = getBookUseCases.findBookById(bookId)
        val catalogSource = getLocalCatalog.get(book?.sourceId)
        
        subscribeToBookAndChapters(bookId, catalogSource)
        
        // BUG: This runs immediately, but state is still Loading!
        if (book != null && book.lastUpdate < 1L) {
            refreshBookFromSource(book, catalogSource)  // updateSuccessState does nothing
            refreshChaptersFromSource()                  // updateSuccessState does nothing
        }
    }
}
```

The `updateSuccessState` helper only updates when state is `Success`:
```kotlin
private inline fun updateSuccessState(update: (Success) -> Success) {
    _state.update { current ->
        when (current) {
            is Loading -> current  // Does nothing!
            is Success -> update(current)
            is Error -> current
        }
    }
}
```

### Solution
Track initial fetch requirement and trigger it after first `Success` emission:

```kotlin
// Track if initial fetch is needed
private var needsInitialFetch = false
private var initialBook: Book? = null
private var initialCatalog: CatalogLocal? = null

private fun initializeBook(bookId: Long) {
    scope.launch {
        val book = getBookUseCases.findBookById(bookId)
        val catalogSource = getLocalCatalog.get(book?.sourceId)
        
        // Mark for initial fetch, don't execute yet
        if (book != null && book.lastUpdate < 1L && catalogSource?.source != null) {
            needsInitialFetch = true
            initialBook = book
            initialCatalog = catalogSource
        }
        
        subscribeToBookAndChapters(bookId, catalogSource)
    }
}

private fun subscribeToBookAndChapters(bookId: Long, initialCatalog: CatalogLocal?) {
    combine(...).onEach { newState ->
        _state.value = newState
        
        // Trigger initial fetch AFTER state is Success
        if (needsInitialFetch && newState is BookDetailState.Success) {
            needsInitialFetch = false
            val book = initialBook
            val catalog = initialCatalog
            initialBook = null
            initialCatalog = null
            
            if (book != null) {
                scope.launch {
                    getRemoteBookDetail(book, catalog)
                    getRemoteChapterDetail(book, catalog)
                }
            }
        }
    }.launchIn(scope)
}
```

### Lesson
**When migrating to sealed state, ensure async operations that depend on `Success` state are triggered AFTER the state transition, not before.**

---

## Bug #2: Missing Chapter Filtering Logic

### Problem
Chapters were not being filtered by search query or filter settings after migration.

### Root Cause
The old ViewModel had a `@Composable fun getChapters()` method that applied filtering:

```kotlin
// OLD CODE
@Composable
fun getChapters(bookId: Long?): State<List<Chapter>> {
    val unfiltered = getChapterUseCase.subscribeChaptersByBookId(bookId, sort)
    
    return remember(query, filters) {
        if (query.isNullOrBlank()) {
            unfiltered
        } else {
            unfiltered.map { chapters ->
                chapters.filter { it.name.contains(query, true) }
            }
        }.map { it.filteredWith(filters) }
    }.collectAsState(emptyList())
}
```

The new implementation passed raw chapters without filtering:
```kotlin
// NEW CODE (BROKEN)
val chaptersState = remember(state.chapters) {
    mutableStateOf(state.chapters.toList())  // No filtering!
}
```

### Solution
Apply filtering in the Screen before passing to UI:

```kotlin
// Apply filtering based on vm.query and vm.filters
val filteredChapters by remember(state.chapters, vm.query, vm.filters.value) {
    derivedStateOf {
        var result = state.chapters.toList()
        
        // Apply search filter
        val query = vm.query
        if (!query.isNullOrBlank()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }
        }
        
        // Apply chapter filters
        for (filter in vm.filters.value) {
            if (filter.value == ChaptersFilters.Value.Missing) continue
            
            val predicate: (Chapter) -> Boolean = when (filter.type) {
                ChaptersFilters.Type.Unread -> { ch -> !ch.read }
                ChaptersFilters.Type.Read -> { ch -> ch.read }
                ChaptersFilters.Type.Bookmarked -> { ch -> ch.bookmark }
                ChaptersFilters.Type.Downloaded -> { ch -> ch.content.joinToString("").isNotBlank() }
                ChaptersFilters.Type.Duplicate -> { ch ->
                    state.chapters.any { other ->
                        other.id != ch.id && other.name.trim().equals(ch.name.trim(), ignoreCase = true)
                    }
                }
            }
            
            result = when (filter.value) {
                ChaptersFilters.Value.Included -> result.filter(predicate)
                ChaptersFilters.Value.Excluded -> result.filterNot(predicate)
                ChaptersFilters.Value.Missing -> result
            }
        }
        
        result
    }
}

val chaptersState = remember { mutableStateOf<List<Chapter>>(emptyList()) }
LaunchedEffect(filteredChapters) {
    chaptersState.value = filteredChapters
}
```

### Lesson
**When migrating, trace ALL data transformations from source to UI. Don't assume raw data can replace transformed data.**

---

## Bug #3: State Capture in remember/derivedStateOf

### Problem
Using `remember(state.chapters)` with `mutableStateOf` doesn't update when `state` changes.

### Root Cause
```kotlin
// BROKEN - captures state.chapters at creation time
val chaptersState = remember(state.chapters) {
    mutableStateOf(state.chapters.toList())
}
// chaptersState.value is never updated when state.chapters changes!
```

The `remember` key only triggers recreation, but `mutableStateOf` creates a snapshot that doesn't track external changes.

### Solution
Use `LaunchedEffect` to update the state:

```kotlin
val chaptersState = remember { mutableStateOf<List<Chapter>>(emptyList()) }
LaunchedEffect(filteredChapters) {
    chaptersState.value = filteredChapters
}
```

Or use `derivedStateOf` if the source is a Compose state:
```kotlin
val chaptersState by remember {
    derivedStateOf { state.chapters.toList() }
}
```

### Lesson
**`remember` with a key only recreates the lambda, it doesn't make the result reactive. Use `LaunchedEffect` or `derivedStateOf` for reactive updates.**

---

## Bug #4: Missing Method After Refactoring

### Problem
`vm.booksState.book` and `vm.downloadUndownloadedChapters()` were referenced but didn't exist in the new ViewModel.

### Root Cause
The old ViewModel had:
- `val booksState: BooksState` - a separate state holder
- `fun downloadUndownloadedChapters()` - a download method

The new ViewModel removed `booksState` and forgot to add `downloadUndownloadedChapters`.

### Solution
1. Add direct property access: `val book: Book? get() = (_state.value as? Success)?.book`
2. Add missing methods to the new ViewModel

### Lesson
**Before deleting old code, grep for ALL usages across the codebase. Use IDE "Find Usages" or `grep -r "oldMethod"` to find all references.**

---

## Bug #5: Race Condition - Book Not Found on Navigation

### Problem
When navigating from Explore screen to BookDetail, the book sometimes shows "Book not found" error even though the book was just clicked.

### Root Cause
The navigation happens immediately after `insertBook()` is called, but the Flow subscription in BookDetailViewModel might emit before the database transaction is complete:

```kotlin
// ExploreScreenSpec.kt
onBook = { book ->
    vm.scope.launch {
        val bookId = vm.insertUseCases.insertBook(newBook)
        if (bookId != 0L) {
            navController.navigate(NavigationRoutes.bookDetail(bookId))  // Navigation happens
        }
    }
}

// BookDetailViewModel.kt - BROKEN
combine(
    getBookUseCases.subscribeBookById(bookId),  // Might emit null before insert completes!
    ...
) { book, chapters, history ->
    if (book != null) {
        BookDetailState.Success(...)
    } else {
        BookDetailState.Error("Book not found")  // Emits error immediately!
    }
}
```

### Solution
Filter out null emissions and add a timeout:

```kotlin
combine(
    getBookUseCases.subscribeBookById(bookId),
    getChapterUseCase.subscribeChaptersByBookId(bookId),
    historyUseCase.subscribeHistoryByBookId(bookId),
) { book, chapters, history ->
    if (book != null) {
        BookDetailState.Success(...)
    } else {
        null  // Return null instead of error
    }
}
.filterNotNull()  // Wait for book to be available
.timeout(10.seconds)  // Timeout if book is never found
.catch { e ->
    val message = when (e) {
        is TimeoutCancellationException -> "Book not found - it may not have been saved properly"
        else -> e.message ?: "Unknown error"
    }
    _state.value = BookDetailState.Error(message)
}
```

### Lesson
**When using Flow subscriptions that depend on data being inserted, handle the race condition where the subscription might emit before the data is available. Use `filterNotNull()` to wait for valid data and `timeout()` to handle the case where data never arrives.**

---

## Bug #6: Variable Shadowing in Nested Scopes

### Problem
Trying to reassign a class property inside a lambda that's within a function with a parameter of the same name.

### Root Cause
```kotlin
class ViewModel {
    private var initialCatalog: CatalogLocal? = null  // Class property
    
    // Function parameter shadows class property!
    private fun subscribeToBookAndChapters(bookId: Long, initialCatalog: CatalogLocal?) {
        combine(...).onEach { newState ->
            // This tries to reassign the function PARAMETER, not the class property!
            initialCatalog = null  // ERROR: 'val' cannot be reassigned
        }
    }
}
```

### Solution
Use explicit `this@ClassName` to reference the class property:

```kotlin
private fun subscribeToBookAndChapters(bookId: Long, initialCatalog: CatalogLocal?) {
    combine(...).onEach { newState ->
        // Explicitly reference class property
        this@BookDetailViewModel.initialCatalog = null  // Works!
    }
}
```

Or rename the function parameter to avoid shadowing:
```kotlin
private fun subscribeToBookAndChapters(bookId: Long, catalog: CatalogLocal?) {
    // Now initialCatalog unambiguously refers to class property
}
```

### Lesson
**Be careful with variable naming. Function parameters shadow class properties in nested lambdas. Use `this@ClassName` or rename to avoid ambiguity.**

---

## Bug #6: collectAsStateWithLifecycle Not Available in Common Code

### Problem
`collectAsStateWithLifecycle` is Android-specific and not available in Kotlin Multiplatform common code.

### Root Cause
```kotlin
// BROKEN in commonMain
import androidx.lifecycle.compose.collectAsStateWithLifecycle
val state by vm.state.collectAsStateWithLifecycle()
```

### Solution
Use `collectAsState()` which is available in common code:
```kotlin
import androidx.compose.runtime.collectAsState
val state by vm.state.collectAsState()
```

### Lesson
**In KMP projects, verify imports are available in commonMain. Android-specific APIs like `lifecycle-runtime-compose` are not available in common code.**

---

## Migration Checklist

Use this checklist when migrating ViewModels to sealed state pattern:

### Before Migration
- [ ] Document all public properties and methods
- [ ] Document all data transformations (filtering, sorting, mapping)
- [ ] Grep for all usages of the ViewModel across codebase
- [ ] Identify async operations that depend on state

### During Migration
- [ ] Ensure initial async operations wait for `Success` state
- [ ] Preserve all data transformations (filters, sorts, maps)
- [ ] Add all public methods that were in the old ViewModel
- [ ] Use KMP-compatible imports (no Android-specific APIs in common)

### After Migration
- [ ] Verify all usages compile
- [ ] Test initial data loading
- [ ] Test refresh functionality
- [ ] Test filtering/sorting
- [ ] Test all user actions (selection, download, etc.)

---

## Debugging Tips

### Add Logging to State Changes
```kotlin
.onEach { newState ->
    _state.value = newState
    
    when (newState) {
        is Success -> Log.info { "Success: ${newState.book.title}, chapters=${newState.chapters.size}" }
        is Loading -> Log.info { "Loading" }
        is Error -> Log.error { "Error: ${newState.message}" }
    }
}
```

### Add Logging to Remote Fetches
```kotlin
suspend fun getRemoteChapterDetail(...) {
    Log.info { "Fetching remote chapters for: ${book.title}" }
    
    remoteUseCases.getRemoteChapters(
        onSuccess = { result ->
            Log.info { "Successfully fetched ${result.size} chapters" }
            // ...
        },
        onError = { message ->
            Log.error { "Error fetching chapters: $message" }
            // ...
        }
    )
}
```

### Verify State Before Operations
```kotlin
suspend fun getRemoteChapterDetail(...) {
    if (_state.value is BookDetailState.Success) {
        updateSuccessState { it.copy(isRefreshingChapters = true) }
    } else {
        Log.warn { "Cannot update loading state - not in Success state" }
    }
    // ...
}
```
