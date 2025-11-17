# IReader New Architecture Guide

This document describes the new business logic layer and state management architecture implemented following Mihon's proven patterns.

## Overview

The new architecture introduces:

1. **Use Case/Interactor Layer**: Clean business logic separation
2. **StateScreenModel Pattern**: Predictable UI state management
3. **Update Classes**: Partial entity updates
4. **Comprehensive Error Handling**: Structured logging and error recovery
5. **Dependency Injection**: Proper module organization

## Architecture Components

### 1. Use Case Layer

Use cases encapsulate business logic and provide a clean interface between the UI and data layers.

#### Core Use Cases

- `GetBook`: Retrieve book information
- `GetChapters`: Retrieve chapter information  
- `GetCategories`: Retrieve category information
- `UpdateBook`: Update book data
- `AddToLibrary`: Add books to library
- `RemoveFromLibrary`: Remove books from library
- `ToggleFavorite`: Toggle book favorite status

#### Usage Example

```kotlin
class BookDetailScreenModelNew(
    private val getBook: GetBook,
    private val toggleFavorite: ToggleFavorite,
) : IReaderStateScreenModel<State>(State()) {
    
    fun loadBook(bookId: Long) {
        launchIO {
            val book = getBook.await(bookId)
            updateState { it.copy(book = book) }
        }
    }
    
    fun toggleFavorite() {
        val book = state.value.book ?: return
        launchIO {
            val success = toggleFavorite.await(book)
            // Handle result...
        }
    }
}
```

### 2. StateScreenModel Pattern

Replaces ViewModels with StateScreenModel for predictable state management.

#### Key Features

- **Sealed State Classes**: Clear state representation
- **Utility Methods**: `updateState()`, `launchIO()`, `launchMain()`
- **Error Handling**: Built-in error handling and logging
- **Coroutine Management**: Proper scope and context handling

#### Example Implementation

```kotlin
class BookDetailScreenModelNew(
    // dependencies...
) : IReaderStateScreenModel<BookDetailScreenModelNew.State>(State()) {

    data class State(
        val book: Book? = null,
        val chapters: List<Chapter> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null,
    )

    init {
        loadBookDetails()
    }

    private fun loadBookDetails() {
        launchIO {
            try {
                val book = getBook.await(bookId)
                updateState { it.copy(book = book, isLoading = false) }
            } catch (e: Exception) {
                updateState { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
```

### 3. Update Classes

Partial update classes allow updating specific entity fields without affecting others.

#### Available Update Classes

- `BookUpdate`: Partial book updates
- `ChapterUpdate`: Partial chapter updates
- `CategoryUpdate`: Partial category updates (already existed)

#### Usage Example

```kotlin
// Update only the favorite status and date added
val update = BookUpdate(
    id = bookId,
    favorite = true,
    dateAdded = Calendar.getInstance().timeInMillis,
)

val success = updateBook.await(update)
```

### 4. Error Handling

Comprehensive error handling with structured logging.

#### IReaderLog Utility

```kotlin
// Debug logging
IReaderLog.debug("Loading book details for ID: $bookId", "BookDetailScreenModel")

// Error logging with exception
IReaderLog.error("Failed to load book", exception, "BookDetailScreenModel")

// Warning logging
IReaderLog.warn("Book not found: $bookId", tag = "BookDetailScreenModel")
```

#### Error Recovery

```kotlin
override fun handleError(error: Throwable) {
    updateState { currentState ->
        currentState.copy(
            isLoading = false,
            error = error.message ?: "Unknown error occurred"
        )
    }
    super.handleError(error)
}
```

### 5. UI Components

New UI components following Mihon's patterns.

#### IReaderErrorScreen

```kotlin
@Composable
fun BookDetailScreen() {
    val state by screenModel.state.collectAsState()
    
    when {
        state.error != null -> {
            IReaderErrorScreen(
                message = state.error!!,
                onRetry = { screenModel.retry() },
                onDismiss = { screenModel.clearError() }
            )
        }
        // ... other states
    }
}
```

#### IReaderLoadingScreen

```kotlin
@Composable
fun LoadingContent() {
    IReaderLoadingScreen(
        progress = 0.5f, // Optional progress
        message = "Loading book details..." // Optional message
    )
}
```

## Migration Guide

### From ViewModel to StateScreenModel

1. **Replace BaseViewModel with IReaderStateScreenModel**:
   ```kotlin
   // Old
   class MyViewModel : BaseViewModel()
   
   // New
   class MyScreenModel : IReaderStateScreenModel<State>(State())
   ```

2. **Define State Class**:
   ```kotlin
   data class State(
       val data: List<Item> = emptyList(),
       val isLoading: Boolean = false,
       val error: String? = null,
   )
   ```

3. **Use State Updates**:
   ```kotlin
   // Old
   var isLoading by mutableStateOf(false)
   
   // New
   updateState { it.copy(isLoading = false) }
   ```

4. **Replace Direct Repository Calls with Use Cases**:
   ```kotlin
   // Old
   val book = bookRepository.getBookById(id)
   
   // New
   val book = getBook.await(id)
   ```

### Dependency Injection Updates

Add the new modules to your DI configuration:

```kotlin
// In DomainModules.kt
includes(useCaseModule)

// In PresentationModules.kt  
includes(screenModelModule)
```

## Testing

### Use Case Testing

```kotlin
class GetBookTest {
    @Test
    fun `await returns book when found`() = runTest {
        // Given
        val expectedBook = createTestBook()
        coEvery { bookRepository.getBookById(1L) } returns expectedBook
        
        // When
        val result = getBook.await(1L)
        
        // Then
        assertEquals(expectedBook, result)
    }
}
```

### StateScreenModel Testing

```kotlin
class BookDetailScreenModelNewTest {
    @Test
    fun `loads book successfully`() = runTest {
        // Given
        val book = createTestBook()
        coEvery { getBook.subscribe(1L) } returns flowOf(book)
        
        // When
        val screenModel = BookDetailScreenModelNew(1L, getBook, ...)
        advanceUntilIdle()
        
        // Then
        assertEquals(book, screenModel.state.value.book)
        assertFalse(screenModel.state.value.isLoading)
    }
}
```

## Benefits

1. **Separation of Concerns**: Clear boundaries between UI, business logic, and data
2. **Testability**: Easy to test use cases and state models in isolation
3. **Maintainability**: Consistent patterns across the codebase
4. **Error Handling**: Comprehensive error handling and recovery
5. **Performance**: Efficient state updates and memory management
6. **Scalability**: Easy to add new features following established patterns

## Best Practices

1. **Use Cases**: Keep use cases focused on single responsibilities
2. **State Management**: Use sealed classes for complex state
3. **Error Handling**: Always handle errors gracefully with user-friendly messages
4. **Logging**: Use structured logging with appropriate levels
5. **Testing**: Write comprehensive tests for use cases and state models
6. **Documentation**: Document complex business logic and state transitions

## Next Steps

1. Gradually migrate existing ViewModels to StateScreenModels
2. Replace direct repository calls with use cases
3. Add comprehensive error handling to existing screens
4. Implement UI components following Mihon's patterns
5. Add performance monitoring and analytics

This new architecture provides a solid foundation for scalable, maintainable, and testable code following industry best practices.