# KDoc Documentation Guide

## Overview

This guide establishes standards for KDoc documentation in the IReader codebase, ensuring consistent, comprehensive, and helpful documentation for all public APIs.

## Table of Contents

1. [General Guidelines](#general-guidelines)
2. [Documentation Requirements](#documentation-requirements)
3. [KDoc Syntax](#kdoc-syntax)
4. [Examples](#examples)
5. [Best Practices](#best-practices)

## General Guidelines

### When to Document

**MUST document:**
- All public classes, interfaces, and objects
- All public functions and properties
- All public constructors
- Complex algorithms or business logic
- Non-obvious behavior or side effects

**SHOULD document:**
- Protected members that are part of the API
- Internal members that are complex or non-obvious
- Private members with complex logic

**MAY skip documentation:**
- Simple getters/setters with obvious behavior
- Overridden functions that don't change behavior
- Test classes and functions

### Documentation Quality

Good documentation should:
- Be clear and concise
- Explain **what** the code does
- Explain **why** it does it (when not obvious)
- Describe parameters and return values
- Document exceptions that may be thrown
- Include usage examples for complex APIs
- Reference related classes/functions

## Documentation Requirements

### Classes and Interfaces

```kotlin
/**
 * Brief one-line description of the class.
 *
 * More detailed description explaining:
 * - Purpose and responsibility
 * - Key features
 * - Usage patterns
 * - Important constraints or limitations
 *
 * Example usage:
 * ```kotlin
 * val repository = BookRepository()
 * val book = repository.getBookById(1L)
 * ```
 *
 * @property propertyName Description of the property
 * @constructor Creates an instance with the specified parameters
 * @param paramName Description of constructor parameter
 * @see RelatedClass
 * @since 1.0.0
 */
class BookRepository(
    private val handler: DatabaseHandler
) {
    // Implementation
}
```

### Functions

```kotlin
/**
 * Brief one-line description of what the function does.
 *
 * More detailed description if needed, explaining:
 * - Algorithm or approach
 * - Side effects
 * - Thread safety
 * - Performance considerations
 *
 * @param id The unique identifier of the book to retrieve
 * @param includeChapters Whether to include chapter data (default: false)
 * @return The book with the specified ID, or null if not found
 * @throws DatabaseError if the database operation fails
 * @throws IllegalArgumentException if id is negative
 * @see Book
 * @see Chapter
 */
suspend fun getBookById(
    id: Long,
    includeChapters: Boolean = false
): Book?
```

### Properties

```kotlin
/**
 * The unique identifier for this book.
 *
 * This ID is assigned by the database and is guaranteed to be unique
 * across all books in the system.
 */
val id: Long

/**
 * Whether this book is marked as a favorite.
 *
 * Setting this to true will add the book to the user's library.
 * Setting it to false will remove it from the library but not delete it.
 */
var favorite: Boolean
```

### Enums

```kotlin
/**
 * Represents the reading status of a book.
 *
 * @property value The integer value stored in the database
 */
enum class ReadingStatus(val value: Int) {
    /**
     * The book has not been started yet.
     */
    NOT_STARTED(0),
    
    /**
     * The book is currently being read.
     */
    IN_PROGRESS(1),
    
    /**
     * The book has been completed.
     */
    COMPLETED(2),
    
    /**
     * The book has been dropped and will not be continued.
     */
    DROPPED(3)
}
```

### Data Classes

```kotlin
/**
 * Represents a partial update to a book entity.
 *
 * Only non-null fields will be updated in the database.
 * This allows for efficient partial updates without loading
 * the entire entity.
 *
 * Example:
 * ```kotlin
 * val update = BookUpdate(
 *     id = 1L,
 *     title = "New Title",
 *     favorite = true
 * )
 * repository.update(update)
 * ```
 *
 * @property id The ID of the book to update (required)
 * @property title The new title, or null to keep existing
 * @property author The new author, or null to keep existing
 * @property favorite The new favorite status, or null to keep existing
 */
data class BookUpdate(
    val id: Long,
    val title: String? = null,
    val author: String? = null,
    val favorite: Boolean? = null
)
```

### Sealed Classes

```kotlin
/**
 * Represents the state of a screen in the application.
 *
 * This sealed class hierarchy provides type-safe state management
 * for UI screens, ensuring all possible states are handled.
 *
 * @param T The type of data contained in the success state
 */
sealed class ScreenState<out T> {
    /**
     * The screen is currently loading data.
     *
     * @property message Optional loading message to display
     */
    data class Loading(val message: String? = null) : ScreenState<Nothing>()
    
    /**
     * The screen has successfully loaded data.
     *
     * @property data The loaded data
     */
    data class Success<T>(val data: T) : ScreenState<T>()
    
    /**
     * The screen encountered an error while loading.
     *
     * @property message The error message to display
     * @property throwable The underlying exception, if any
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : ScreenState<Nothing>()
    
    /**
     * The screen has no data to display.
     *
     * @property message Optional message explaining why there's no data
     */
    data class Empty(val message: String? = null) : ScreenState<Nothing>()
}
```

## KDoc Syntax

### Tags

| Tag | Purpose | Example |
|-----|---------|---------|
| `@param` | Describes a function parameter | `@param id The book identifier` |
| `@return` | Describes the return value | `@return The book, or null if not found` |
| `@throws` | Documents exceptions | `@throws DatabaseError if query fails` |
| `@property` | Describes a class property | `@property title The book title` |
| `@constructor` | Describes the constructor | `@constructor Creates a new book` |
| `@see` | References related code | `@see BookRepository` |
| `@since` | Version when added | `@since 1.0.0` |
| `@deprecated` | Marks as deprecated | `@deprecated Use newMethod instead` |
| `@sample` | Links to sample code | `@sample samples.BookSamples.create` |

### Formatting

**Bold text:**
```kotlin
/**
 * This is **important** information.
 */
```

**Italic text:**
```kotlin
/**
 * This is *emphasized* text.
 */
```

**Code inline:**
```kotlin
/**
 * Use `null` to indicate absence of value.
 */
```

**Code blocks:**
```kotlin
/**
 * Example usage:
 * ```kotlin
 * val book = Book(id = 1L, title = "Example")
 * repository.insert(book)
 * ```
 */
```

**Lists:**
```kotlin
/**
 * This function:
 * - Validates input
 * - Queries database
 * - Returns result
 */
```

**Links:**
```kotlin
/**
 * See [BookRepository.getBookById] for details.
 * See [Book] for the data model.
 */
```

## Examples

### Repository Interface

```kotlin
/**
 * Repository for managing book data operations.
 *
 * This repository provides methods for CRUD operations on books,
 * including reactive Flow-based queries and partial updates.
 * All operations are thread-safe and can be called from any coroutine context.
 *
 * Implementation notes:
 * - Uses DatabaseHandler for database access
 * - Implements proper error handling with logging
 * - Supports both suspend functions and Flow-based queries
 * - Uses Update classes for efficient partial updates
 *
 * Example usage:
 * ```kotlin
 * class BookViewModel(private val repository: BookRepository) {
 *     fun loadBook(id: Long) = viewModelScope.launch {
 *         val book = repository.getBookById(id)
 *         // Handle book
 *     }
 * }
 * ```
 *
 * @see Book
 * @see BookUpdate
 * @see DatabaseHandler
 * @since 1.0.0
 */
interface BookRepository {
    
    /**
     * Retrieves a book by its unique identifier.
     *
     * This is a suspend function that performs a database query.
     * It will suspend the calling coroutine until the query completes.
     *
     * @param id The unique identifier of the book
     * @return The book with the specified ID
     * @throws DatabaseError if the book cannot be retrieved
     * @throws NoSuchElementException if no book exists with the given ID
     */
    suspend fun getBookById(id: Long): Book
    
    /**
     * Observes a book by its unique identifier.
     *
     * Returns a Flow that emits the book whenever it changes in the database.
     * The Flow will complete if the book is deleted.
     *
     * @param id The unique identifier of the book
     * @return A Flow that emits the book on changes
     */
    fun getBookByIdAsFlow(id: Long): Flow<Book>
    
    /**
     * Updates a book with partial data.
     *
     * Only non-null fields in the update object will be modified.
     * This allows for efficient updates without loading the entire entity.
     *
     * @param update The partial update to apply
     * @return true if the update succeeded, false otherwise
     * @throws DatabaseError if the update operation fails
     */
    suspend fun update(update: BookUpdate): Boolean
    
    /**
     * Updates multiple books in a single transaction.
     *
     * All updates are performed atomically. If any update fails,
     * all changes are rolled back.
     *
     * @param updates The list of updates to apply
     * @return true if all updates succeeded, false otherwise
     * @throws DatabaseError if the transaction fails
     */
    suspend fun updateAll(updates: List<BookUpdate>): Boolean
}
```

### Use Case

```kotlin
/**
 * Use case for retrieving a book by its ID.
 *
 * This use case encapsulates the business logic for fetching a book,
 * including error handling and logging. It provides a clean separation
 * between the repository layer and the presentation layer.
 *
 * Example usage:
 * ```kotlin
 * class BookDetailViewModel(private val getBook: GetBook) {
 *     fun loadBook(id: Long) = viewModelScope.launch {
 *         val book = getBook.await(id)
 *         if (book != null) {
 *             // Handle book
 *         } else {
 *             // Handle error
 *         }
 *     }
 * }
 * ```
 *
 * @property bookRepository The repository for book data access
 * @constructor Creates a new GetBook use case
 * @see BookRepository
 * @since 1.0.0
 */
class GetBook(
    private val bookRepository: BookRepository
) {
    
    /**
     * Retrieves a book by its ID.
     *
     * This method handles errors gracefully and returns null if the book
     * cannot be retrieved. Errors are logged for debugging purposes.
     *
     * @param id The unique identifier of the book
     * @return The book if found, null otherwise
     */
    suspend fun await(id: Long): Book? {
        return try {
            bookRepository.getBookById(id)
        } catch (e: Exception) {
            Log.error("Failed to get book: $id", e)
            null
        }
    }
    
    /**
     * Observes a book by its ID.
     *
     * Returns a Flow that emits the book whenever it changes.
     * Errors are logged but not propagated to the caller.
     *
     * @param id The unique identifier of the book
     * @return A Flow that emits the book on changes
     */
    fun subscribe(id: Long): Flow<Book> {
        return bookRepository.getBookByIdAsFlow(id)
            .catch { e ->
                Log.error("Failed to subscribe to book: $id", e)
                emit(null)
            }
            .filterNotNull()
    }
}
```

### StateScreenModel

```kotlin
/**
 * Screen model for the book detail screen.
 *
 * This screen model manages the state of the book detail screen,
 * including loading the book data, handling errors, and managing
 * user interactions.
 *
 * State management:
 * - Uses sealed State class for type-safe state representation
 * - Provides reactive state updates via StateFlow
 * - Handles loading, success, and error states
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun BookDetailScreen(bookId: Long) {
 *     val screenModel = rememberScreenModel { BookDetailScreenModel(bookId) }
 *     val state by screenModel.state.collectAsState()
 *     
 *     when (state) {
 *         is State.Loading -> LoadingScreen()
 *         is State.Success -> BookDetailContent(state.book)
 *         is State.Error -> ErrorScreen(state.message)
 *     }
 * }
 * ```
 *
 * @property bookId The ID of the book to display
 * @property getBook Use case for retrieving book data
 * @property getChapters Use case for retrieving chapter data
 * @constructor Creates a new BookDetailScreenModel
 * @see IReaderStateScreenModel
 * @since 1.0.0
 */
class BookDetailScreenModel(
    private val bookId: Long,
    private val getBook: GetBook,
    private val getChapters: GetChapters
) : IReaderStateScreenModel<BookDetailScreenModel.State>(State()) {
    
    /**
     * Represents the state of the book detail screen.
     *
     * @property book The book being displayed, or null if loading/error
     * @property chapters The list of chapters for the book
     * @property isLoading Whether data is currently being loaded
     * @property error Error message if loading failed, or null if no error
     */
    data class State(
        val book: Book? = null,
        val chapters: List<Chapter> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null
    )
    
    init {
        loadBookDetails()
    }
    
    /**
     * Loads the book details and chapters.
     *
     * This method is called automatically when the screen model is created.
     * It can also be called manually to refresh the data.
     */
    fun loadBookDetails() {
        launchIO {
            updateState { it.copy(isLoading = true, error = null) }
            
            try {
                val book = getBook.await(bookId)
                val chapters = getChapters.await(bookId)
                
                updateState {
                    it.copy(
                        book = book,
                        chapters = chapters,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.error("Failed to load book details", e)
                updateState {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }
    
    /**
     * Retries loading the book details after an error.
     *
     * This method clears the error state and attempts to load the data again.
     */
    fun retry() {
        loadBookDetails()
    }
}
```

## Best Practices

### DO

✅ Write clear, concise descriptions
✅ Document all public APIs
✅ Include usage examples for complex APIs
✅ Document exceptions that may be thrown
✅ Use proper grammar and punctuation
✅ Keep documentation up-to-date with code changes
✅ Reference related classes and functions
✅ Explain non-obvious behavior
✅ Document thread safety and performance characteristics

### DON'T

❌ State the obvious ("This function returns a value")
❌ Copy-paste documentation without customizing
❌ Leave TODO comments in documentation
❌ Use vague descriptions ("Does stuff")
❌ Document implementation details that may change
❌ Use abbreviations without explanation
❌ Forget to update documentation when code changes
❌ Write overly long descriptions (keep it concise)

### Common Mistakes

**Too vague:**
```kotlin
/**
 * Gets a book.
 */
suspend fun getBook(id: Long): Book
```

**Better:**
```kotlin
/**
 * Retrieves a book by its unique identifier from the database.
 *
 * @param id The unique identifier of the book
 * @return The book with the specified ID
 * @throws DatabaseError if the database query fails
 * @throws NoSuchElementException if no book exists with the given ID
 */
suspend fun getBook(id: Long): Book
```

**Too verbose:**
```kotlin
/**
 * This function is responsible for the retrieval of a book entity
 * from the underlying database storage system by utilizing the
 * provided unique identifier parameter to query the database and
 * return the corresponding book object if it exists in the system.
 */
suspend fun getBook(id: Long): Book
```

**Better:**
```kotlin
/**
 * Retrieves a book by its unique identifier.
 *
 * @param id The unique identifier of the book
 * @return The book with the specified ID
 * @throws NoSuchElementException if the book doesn't exist
 */
suspend fun getBook(id: Long): Book
```

## Tools and Automation

### Dokka

Use Dokka to generate HTML documentation:

```bash
./gradlew dokkaHtml
```

Output will be in `build/dokka/html/`

### IDE Support

IntelliJ IDEA and Android Studio provide:
- KDoc generation templates (/** + Enter)
- Quick documentation popup (Ctrl+Q / Cmd+J)
- Documentation warnings for missing docs
- KDoc formatting and validation

### CI/CD Integration

Add documentation checks to CI pipeline:

```yaml
- name: Check documentation
  run: ./gradlew dokkaHtml
  
- name: Verify documentation coverage
  run: ./gradlew dokkaCheck
```

## Conclusion

Good documentation is essential for maintainable code. Follow these guidelines to ensure your code is well-documented and easy to understand for other developers (and your future self!).

For questions or suggestions, please contact the development team or create an issue in the project repository.
