# Repository API Documentation

This document provides comprehensive documentation for all repository interfaces in the IReader application. These repositories provide data access operations for books, chapters, and categories.

## Stability Annotations

- **@Stable**: API is stable and will not change without a major version bump
- **@Experimental**: API is experimental and may change in future releases
- **@Deprecated**: API is deprecated and will be removed in a future release

---

## BookRepository

Repository interface for book data access operations. Provides methods for managing books including CRUD operations, library management, and reactive queries.

**Package**: `ireader.domain.data.repository`

### Stable Methods

#### findAllBooks()
```kotlin
@Stable
suspend fun findAllBooks(): List<Book>
```

Retrieves all books from the database.

**Returns**: List of all books

**Usage Example**:
```kotlin
val books = bookRepository.findAllBooks()
books.forEach { book ->
    println("${book.title} by ${book.author}")
}
```

**Error Cases**: None - returns empty list if no books exist

---

#### subscribeBookById(id: Long)
```kotlin
@Stable
fun subscribeBookById(id: Long): Flow<Book?>
```

Subscribes to book changes by ID. Emits updates whenever the book data changes.

**Parameters**:
- `id`: The unique identifier of the book

**Returns**: Flow emitting the book when it changes, or null if not found

**Usage Example**:
```kotlin
bookRepository.subscribeBookById(bookId)
    .collect { book ->
        if (book != null) {
            updateUI(book)
        }
    }
```

**Error Cases**: Returns null in flow if book doesn't exist

---

#### findBookById(id: Long)
```kotlin
@Stable
suspend fun findBookById(id: Long): Book?
```

Finds a book by its unique identifier.

**Parameters**:
- `id`: The unique identifier of the book

**Returns**: The book if found, null otherwise

**Usage Example**:
```kotlin
val book = bookRepository.findBookById(123L)
if (book != null) {
    displayBook(book)
} else {
    showError("Book not found")
}
```

**Error Cases**: Returns null if book doesn't exist

---

#### find(key: String, sourceId: Long)
```kotlin
@Stable
suspend fun find(key: String, sourceId: Long): Book?
```

Finds a book by its key and source ID.

**Parameters**:
- `key`: The book's unique key within the source
- `sourceId`: The source identifier

**Returns**: The book if found, null otherwise

**Usage Example**:
```kotlin
val book = bookRepository.find("novel-123", sourceId = 1L)
```

**Error Cases**: Returns null if book doesn't exist

---

#### findAllInLibraryBooks(sortType: LibrarySort, isAsc: Boolean, unreadFilter: Boolean)
```kotlin
@Stable
suspend fun findAllInLibraryBooks(
    sortType: LibrarySort,
    isAsc: Boolean = false,
    unreadFilter: Boolean = false,
): List<Book>
```

Retrieves all books in the library with sorting and filtering.

**Parameters**:
- `sortType`: The sorting method to apply (ALPHABETICAL, LAST_READ, LAST_UPDATED, etc.)
- `isAsc`: Whether to sort in ascending order (default: false)
- `unreadFilter`: Whether to filter for unread books only (default: false)

**Returns**: List of library books matching the criteria

**Usage Example**:
```kotlin
val libraryBooks = bookRepository.findAllInLibraryBooks(
    sortType = LibrarySort.ALPHABETICAL,
    isAsc = true,
    unreadFilter = false
)
```

**Error Cases**: Returns empty list if no books match criteria

---

#### findBookByKey(key: String)
```kotlin
@Stable
suspend fun findBookByKey(key: String): Book?
```

Finds a single book by its key.

**Parameters**:
- `key`: The book's unique key

**Returns**: The book if found, null otherwise

**Usage Example**:
```kotlin
val book = bookRepository.findBookByKey("novel-key-123")
```

**Error Cases**: Returns null if book doesn't exist

---

#### findBooksByKey(key: String)
```kotlin
@Stable
suspend fun findBooksByKey(key: String): List<Book>
```

Finds all books matching a specific key.

**Parameters**:
- `key`: The book's key to search for

**Returns**: List of books with matching keys

**Usage Example**:
```kotlin
val duplicates = bookRepository.findBooksByKey("novel-key")
if (duplicates.size > 1) {
    handleDuplicates(duplicates)
}
```

**Error Cases**: Returns empty list if no books match

---

#### subscribeBooksByKey(key: String, title: String)
```kotlin
@Stable
suspend fun subscribeBooksByKey(key: String, title: String): Flow<List<Book>>
```

Subscribes to books matching a key and title.

**Parameters**:
- `key`: The book's key
- `title`: The book's title

**Returns**: Flow emitting list of matching books when they change

**Usage Example**:
```kotlin
bookRepository.subscribeBooksByKey("novel-key", "Novel Title")
    .collect { books ->
        updateBookList(books)
    }
```

**Error Cases**: Emits empty list if no books match

---

#### deleteBooks(book: List<Book>)
```kotlin
@Stable
suspend fun deleteBooks(book: List<Book>)
```

Deletes multiple books from the database.

**Parameters**:
- `book`: List of books to delete

**Usage Example**:
```kotlin
val booksToDelete = selectedBooks.toList()
bookRepository.deleteBooks(booksToDelete)
```

**Error Cases**: Silently ignores books that don't exist

---

#### insertBooksAndChapters(books: List<Book>, chapters: List<Chapter>)
```kotlin
@Stable
suspend fun insertBooksAndChapters(books: List<Book>, chapters: List<Chapter>)
```

Inserts books and their chapters in a single transaction.

**Parameters**:
- `books`: List of books to insert
- `chapters`: List of chapters to insert

**Usage Example**:
```kotlin
bookRepository.insertBooksAndChapters(
    books = listOf(newBook),
    chapters = newChapters
)
```

**Error Cases**: Throws exception if transaction fails

---

#### deleteBookById(id: Long)
```kotlin
@Stable
suspend fun deleteBookById(id: Long)
```

Deletes a book by its unique identifier.

**Parameters**:
- `id`: The unique identifier of the book to delete

**Usage Example**:
```kotlin
bookRepository.deleteBookById(bookId)
```

**Error Cases**: Silently succeeds if book doesn't exist

---

#### findDuplicateBook(title: String, sourceId: Long)
```kotlin
@Stable
suspend fun findDuplicateBook(title: String, sourceId: Long): Book?
```

Finds a duplicate book by title and source ID.

**Parameters**:
- `title`: The book's title
- `sourceId`: The source identifier

**Returns**: The duplicate book if found, null otherwise

**Usage Example**:
```kotlin
val duplicate = bookRepository.findDuplicateBook("Novel Title", sourceId)
if (duplicate != null) {
    showDuplicateWarning(duplicate)
}
```

**Error Cases**: Returns null if no duplicate exists

---

#### deleteAllBooks()
```kotlin
@Stable
suspend fun deleteAllBooks()
```

Deletes all books from the database.

**⚠️ WARNING**: This operation cannot be undone.

**Usage Example**:
```kotlin
// Always confirm with user before calling
if (userConfirmed) {
    bookRepository.deleteAllBooks()
}
```

**Error Cases**: None

---

#### deleteNotInLibraryBooks()
```kotlin
@Stable
suspend fun deleteNotInLibraryBooks()
```

Deletes all books that are not in the user's library.

**Usage Example**:
```kotlin
// Clean up books that were browsed but not added to library
bookRepository.deleteNotInLibraryBooks()
```

**Error Cases**: None

---

#### updateBook(book: Book)
```kotlin
@Stable
suspend fun updateBook(book: Book)
```

Updates an existing book in the database.

**Parameters**:
- `book`: The book with updated information

**Usage Example**:
```kotlin
val updatedBook = book.copy(title = "New Title")
bookRepository.updateBook(updatedBook)
```

**Error Cases**: Silently succeeds if book doesn't exist

---

#### updateBook(book: LibraryBook, favorite: Boolean)
```kotlin
@Stable
suspend fun updateBook(book: LibraryBook, favorite: Boolean)
```

Updates a library book's favorite status.

**Parameters**:
- `book`: The library book to update
- `favorite`: Whether the book is marked as favorite

**Usage Example**:
```kotlin
bookRepository.updateBook(libraryBook, favorite = true)
```

**Error Cases**: None

---

#### updateBook(book: List<Book>)
```kotlin
@Stable
suspend fun updateBook(book: List<Book>)
```

Updates multiple books in a batch operation.

**Parameters**:
- `book`: List of books to update

**Usage Example**:
```kotlin
val updatedBooks = books.map { it.copy(lastUpdate = System.currentTimeMillis()) }
bookRepository.updateBook(updatedBooks)
```

**Error Cases**: None

---

#### upsert(book: Book)
```kotlin
@Stable
suspend fun upsert(book: Book): Long
```

Inserts a new book or updates if it already exists.

**Parameters**:
- `book`: The book to upsert

**Returns**: The ID of the inserted or updated book

**Usage Example**:
```kotlin
val bookId = bookRepository.upsert(book)
println("Book saved with ID: $bookId")
```

**Error Cases**: Throws exception on database error

---

#### updatePartial(book: Book)
```kotlin
@Stable
suspend fun updatePartial(book: Book): Long
```

Updates only the changed fields of a book.

**Parameters**:
- `book`: The book with partial updates

**Returns**: The ID of the updated book

**Usage Example**:
```kotlin
// Only update specific fields
val partialUpdate = Book(id = bookId, title = "New Title")
bookRepository.updatePartial(partialUpdate)
```

**Error Cases**: Throws exception on database error

---

#### insertBooks(book: List<Book>)
```kotlin
@Stable
suspend fun insertBooks(book: List<Book>): List<Long>
```

Inserts multiple books in a batch operation.

**Parameters**:
- `book`: List of books to insert

**Returns**: List of IDs for the inserted books

**Usage Example**:
```kotlin
val bookIds = bookRepository.insertBooks(newBooks)
println("Inserted ${bookIds.size} books")
```

**Error Cases**: Throws exception on database error

---

#### delete(key: String)
```kotlin
@Stable
suspend fun delete(key: String)
```

Deletes a book by its key.

**Parameters**:
- `key`: The book's unique key

**Usage Example**:
```kotlin
bookRepository.delete("novel-key-123")
```

**Error Cases**: Silently succeeds if book doesn't exist

---

#### findFavoriteSourceIds()
```kotlin
@Stable
suspend fun findFavoriteSourceIds(): List<Long>
```

Retrieves source IDs for all books marked as favorites.

**Returns**: List of source IDs that have favorite books

**Usage Example**:
```kotlin
val favoriteSourceIds = bookRepository.findFavoriteSourceIds()
// Use to filter sources or show favorite sources
```

**Error Cases**: Returns empty list if no favorites exist

---

#### repairCategoryAssignments()
```kotlin
@Stable
suspend fun repairCategoryAssignments()
```

Ensures all books have the default category assigned.

**Usage Example**:
```kotlin
// Run during app initialization or after database migration
bookRepository.repairCategoryAssignments()
```

**Error Cases**: None

---

#### updatePinStatus(bookId: Long, isPinned: Boolean, pinnedOrder: Int)
```kotlin
@Stable
suspend fun updatePinStatus(bookId: Long, isPinned: Boolean, pinnedOrder: Int)
```

Updates pin status for a book.

**Parameters**:
- `bookId`: The unique identifier of the book
- `isPinned`: Whether the book is pinned
- `pinnedOrder`: The order of the pinned book

**Usage Example**:
```kotlin
bookRepository.updatePinStatus(bookId, isPinned = true, pinnedOrder = 1)
```

**Error Cases**: None

---

#### updatePinnedOrder(bookId: Long, pinnedOrder: Int)
```kotlin
@Stable
suspend fun updatePinnedOrder(bookId: Long, pinnedOrder: Int)
```

Updates pinned order for a book.

**Parameters**:
- `bookId`: The unique identifier of the book
- `pinnedOrder`: The new order value

**Usage Example**:
```kotlin
bookRepository.updatePinnedOrder(bookId, pinnedOrder = 2)
```

**Error Cases**: None

---

#### getMaxPinnedOrder()
```kotlin
@Stable
suspend fun getMaxPinnedOrder(): Int
```

Gets the maximum pinned order value.

**Returns**: The highest pinned order value, or 0 if no books are pinned

**Usage Example**:
```kotlin
val nextOrder = bookRepository.getMaxPinnedOrder() + 1
bookRepository.updatePinStatus(bookId, isPinned = true, pinnedOrder = nextOrder)
```

**Error Cases**: Returns 0 if no pinned books exist

---

#### updateArchiveStatus(bookId: Long, isArchived: Boolean)
```kotlin
@Stable
suspend fun updateArchiveStatus(bookId: Long, isArchived: Boolean)
```

Updates archive status for a book.

**Parameters**:
- `bookId`: The unique identifier of the book
- `isArchived`: Whether the book is archived

**Usage Example**:
```kotlin
bookRepository.updateArchiveStatus(bookId, isArchived = true)
```

**Error Cases**: None

---

## ChapterRepository

Repository interface for chapter data access operations. Chapters represent individual sections or episodes of a book.

**Package**: `ireader.domain.data.repository`

### Stable Methods

#### subscribeChapterById(chapterId: Long)
```kotlin
@Stable
fun subscribeChapterById(chapterId: Long): Flow<Chapter?>
```

Subscribes to chapter changes by ID.

**Parameters**:
- `chapterId`: The unique identifier of the chapter

**Returns**: Flow emitting the chapter when it changes, or null if not found

**Usage Example**:
```kotlin
chapterRepository.subscribeChapterById(chapterId)
    .collect { chapter ->
        if (chapter != null) {
            updateReaderUI(chapter)
        }
    }
```

**Error Cases**: Returns null in flow if chapter doesn't exist

---

#### findChapterById(chapterId: Long)
```kotlin
@Stable
suspend fun findChapterById(chapterId: Long): Chapter?
```

Finds a chapter by its unique identifier.

**Parameters**:
- `chapterId`: The unique identifier of the chapter

**Returns**: The chapter if found, null otherwise

**Usage Example**:
```kotlin
val chapter = chapterRepository.findChapterById(chapterId)
if (chapter != null) {
    loadChapterContent(chapter)
}
```

**Error Cases**: Returns null if chapter doesn't exist

---

#### findAllChapters()
```kotlin
@Stable
suspend fun findAllChapters(): List<Chapter>
```

Retrieves all chapters from the database.

**Returns**: List of all chapters

**Usage Example**:
```kotlin
val allChapters = chapterRepository.findAllChapters()
println("Total chapters: ${allChapters.size}")
```

**Error Cases**: Returns empty list if no chapters exist

---

#### findAllInLibraryChapter()
```kotlin
@Stable
suspend fun findAllInLibraryChapter(): List<Chapter>
```

Retrieves all chapters for books in the user's library.

**Returns**: List of chapters from library books

**Usage Example**:
```kotlin
val libraryChapters = chapterRepository.findAllInLibraryChapter()
val unreadCount = libraryChapters.count { !it.read }
```

**Error Cases**: Returns empty list if no library chapters exist

---

#### findChaptersByBookId(bookId: Long)
```kotlin
@Stable
suspend fun findChaptersByBookId(bookId: Long): List<Chapter>
```

Retrieves all chapters for a specific book.

**Parameters**:
- `bookId`: The unique identifier of the book

**Returns**: List of chapters belonging to the book

**Usage Example**:
```kotlin
val chapters = chapterRepository.findChaptersByBookId(bookId)
chapters.forEach { chapter ->
    println("Chapter ${chapter.number}: ${chapter.name}")
}
```

**Error Cases**: Returns empty list if book has no chapters

---

#### findLastReadChapter(bookId: Long)
```kotlin
@Stable
suspend fun findLastReadChapter(bookId: Long): Chapter?
```

Finds the last read chapter for a specific book.

**Parameters**:
- `bookId`: The unique identifier of the book

**Returns**: The last read chapter if found, null otherwise

**Usage Example**:
```kotlin
val lastChapter = chapterRepository.findLastReadChapter(bookId)
if (lastChapter != null) {
    resumeReading(lastChapter)
} else {
    startFromBeginning()
}
```

**Error Cases**: Returns null if no chapters have been read

---

#### subscribeLastReadChapter(bookId: Long)
```kotlin
@Stable
suspend fun subscribeLastReadChapter(bookId: Long): Flow<Chapter?>
```

Subscribes to the last read chapter for a specific book.

**Parameters**:
- `bookId`: The unique identifier of the book

**Returns**: Flow emitting the last read chapter when it changes

**Usage Example**:
```kotlin
chapterRepository.subscribeLastReadChapter(bookId)
    .collect { chapter ->
        updateContinueReadingButton(chapter)
    }
```

**Error Cases**: Emits null if no chapters have been read

---

#### insertChapter(chapter: Chapter)
```kotlin
@Stable
suspend fun insertChapter(chapter: Chapter): Long
```

Inserts a new chapter into the database.

**Parameters**:
- `chapter`: The chapter to insert

**Returns**: The ID of the inserted chapter

**Usage Example**:
```kotlin
val chapterId = chapterRepository.insertChapter(newChapter)
println("Chapter inserted with ID: $chapterId")
```

**Error Cases**: Throws exception on database error

---

#### insertChapters(chapters: List<Chapter>)
```kotlin
@Stable
suspend fun insertChapters(chapters: List<Chapter>): List<Long>
```

Inserts multiple chapters in a batch operation.

**Parameters**:
- `chapters`: List of chapters to insert

**Returns**: List of IDs for the inserted chapters

**Usage Example**:
```kotlin
val chapterIds = chapterRepository.insertChapters(newChapters)
println("Inserted ${chapterIds.size} chapters")
```

**Error Cases**: Throws exception on database error

---

#### deleteChaptersByBookId(bookId: Long)
```kotlin
@Stable
suspend fun deleteChaptersByBookId(bookId: Long)
```

Deletes all chapters for a specific book.

**Parameters**:
- `bookId`: The unique identifier of the book

**Usage Example**:
```kotlin
// When refreshing book chapters
chapterRepository.deleteChaptersByBookId(bookId)
chapterRepository.insertChapters(freshChapters)
```

**Error Cases**: Silently succeeds if book has no chapters

---

#### deleteChapters(chapters: List<Chapter>)
```kotlin
@Stable
suspend fun deleteChapters(chapters: List<Chapter>)
```

Deletes multiple chapters in a batch operation.

**Parameters**:
- `chapters`: List of chapters to delete

**Usage Example**:
```kotlin
val chaptersToDelete = selectedChapters.toList()
chapterRepository.deleteChapters(chaptersToDelete)
```

**Error Cases**: Silently ignores chapters that don't exist

---

#### deleteChapter(chapter: Chapter)
```kotlin
@Stable
suspend fun deleteChapter(chapter: Chapter)
```

Deletes a single chapter from the database.

**Parameters**:
- `chapter`: The chapter to delete

**Usage Example**:
```kotlin
chapterRepository.deleteChapter(chapter)
```

**Error Cases**: Silently succeeds if chapter doesn't exist

---

#### deleteAllChapters()
```kotlin
@Stable
suspend fun deleteAllChapters()
```

Deletes all chapters from the database.

**⚠️ WARNING**: This operation cannot be undone.

**Usage Example**:
```kotlin
// Always confirm with user before calling
if (userConfirmed) {
    chapterRepository.deleteAllChapters()
}
```

**Error Cases**: None

---

#### subscribeChaptersByBookId(bookId: Long)
```kotlin
@Stable
fun subscribeChaptersByBookId(bookId: Long): Flow<List<Chapter>>
```

Subscribes to chapter changes for a specific book.

**Parameters**:
- `bookId`: The unique identifier of the book

**Returns**: Flow emitting list of chapters when they change

**Usage Example**:
```kotlin
chapterRepository.subscribeChaptersByBookId(bookId)
    .collect { chapters ->
        updateChapterList(chapters)
    }
```

**Error Cases**: Emits empty list if book has no chapters

---

## CategoryRepository

Repository interface for category data access operations. Categories are used to organize books in the user's library.

**Package**: `ireader.domain.data.repository`

### Stable Methods

#### subscribe()
```kotlin
@Stable
fun subscribe(): Flow<List<CategoryWithCount>>
```

Subscribes to category changes with book counts.

**Returns**: Flow emitting list of categories with their book counts when data changes

**Usage Example**:
```kotlin
categoryRepository.subscribe()
    .collect { categories ->
        updateCategoryList(categories)
    }
```

**Error Cases**: Emits empty list if no categories exist

---

#### findAll()
```kotlin
@Stable
suspend fun findAll(): List<CategoryWithCount>
```

Retrieves all categories with their book counts.

**Returns**: List of categories with book counts

**Usage Example**:
```kotlin
val categories = categoryRepository.findAll()
categories.forEach { category ->
    println("${category.name}: ${category.bookCount} books")
}
```

**Error Cases**: Returns empty list if no categories exist

---

#### get(id: Long)
```kotlin
@Stable
suspend fun get(id: Long): Category?
```

Retrieves a category by its unique identifier.

**Parameters**:
- `id`: The unique identifier of the category

**Returns**: The category if found, null otherwise

**Usage Example**:
```kotlin
val category = categoryRepository.get(categoryId)
if (category != null) {
    displayCategory(category)
}
```

**Error Cases**: Returns null if category doesn't exist

---

#### getAll()
```kotlin
@Stable
suspend fun getAll(): List<Category>
```

Retrieves all categories without book counts.

**Returns**: List of all categories

**Usage Example**:
```kotlin
val categories = categoryRepository.getAll()
showCategoryPicker(categories)
```

**Error Cases**: Returns empty list if no categories exist

---

#### getAllAsFlow()
```kotlin
@Stable
fun getAllAsFlow(): Flow<List<Category>>
```

Subscribes to all category changes.

**Returns**: Flow emitting list of categories when data changes

**Usage Example**:
```kotlin
categoryRepository.getAllAsFlow()
    .collect { categories ->
        updateCategoryDropdown(categories)
    }
```

**Error Cases**: Emits empty list if no categories exist

---

#### getCategoriesByMangaId(mangaId: Long)
```kotlin
@Stable
suspend fun getCategoriesByMangaId(mangaId: Long): List<Category>
```

Retrieves categories assigned to a specific book.

**Parameters**:
- `mangaId`: The unique identifier of the book

**Returns**: List of categories assigned to the book

**Usage Example**:
```kotlin
val bookCategories = categoryRepository.getCategoriesByMangaId(bookId)
displayBookCategories(bookCategories)
```

**Error Cases**: Returns empty list if book has no categories

---

#### getCategoriesByMangaIdAsFlow(mangaId: Long)
```kotlin
@Stable
fun getCategoriesByMangaIdAsFlow(mangaId: Long): Flow<List<Category>>
```

Subscribes to category changes for a specific book.

**Parameters**:
- `mangaId`: The unique identifier of the book

**Returns**: Flow emitting list of categories assigned to the book

**Usage Example**:
```kotlin
categoryRepository.getCategoriesByMangaIdAsFlow(bookId)
    .collect { categories ->
        updateBookCategoryChips(categories)
    }
```

**Error Cases**: Emits empty list if book has no categories

---

#### insert(category: Category)
```kotlin
@Stable
suspend fun insert(category: Category)
```

Inserts a new category into the database.

**Parameters**:
- `category`: The category to insert

**Usage Example**:
```kotlin
val newCategory = Category(name = "Fantasy", order = 1)
categoryRepository.insert(newCategory)
```

**Error Cases**: Throws exception on database error

---

#### insert(category: List<Category>)
```kotlin
@Stable
suspend fun insert(category: List<Category>)
```

Inserts multiple categories in a batch operation.

**Parameters**:
- `category`: List of categories to insert

**Usage Example**:
```kotlin
val categories = listOf(
    Category(name = "Fantasy", order = 1),
    Category(name = "Sci-Fi", order = 2)
)
categoryRepository.insert(categories)
```

**Error Cases**: Throws exception on database error

---

#### update(category: Category)
```kotlin
@Stable
suspend fun update(category: Category)
```

Updates an existing category.

**Parameters**:
- `category`: The category with updated information

**Usage Example**:
```kotlin
val updatedCategory = category.copy(name = "New Name")
categoryRepository.update(updatedCategory)
```

**Error Cases**: Silently succeeds if category doesn't exist

---

#### updateBatch(categories: List<Category>)
```kotlin
@Stable
suspend fun updateBatch(categories: List<Category>)
```

Updates multiple categories in a batch operation. This method is used for operations like reordering categories.

**Parameters**:
- `categories`: List of categories to update

**Usage Example**:
```kotlin
// Reorder categories
val reorderedCategories = categories.mapIndexed { index, category ->
    category.copy(order = index)
}
categoryRepository.updateBatch(reorderedCategories)
```

**Error Cases**: None

---

#### updatePartial(update: CategoryUpdate)
```kotlin
@Stable
suspend fun updatePartial(update: CategoryUpdate)
```

Updates only specific fields of a category.

**Parameters**:
- `update`: The category update with partial changes

**Usage Example**:
```kotlin
val update = CategoryUpdate(id = categoryId, name = "New Name")
categoryRepository.updatePartial(update)
```

**Error Cases**: None

---

#### updatePartial(updates: List<CategoryUpdate>)
```kotlin
@Stable
suspend fun updatePartial(updates: List<CategoryUpdate>)
```

Updates specific fields of multiple categories.

**Parameters**:
- `updates`: List of category updates with partial changes

**Usage Example**:
```kotlin
val updates = categories.map { CategoryUpdate(id = it.id, order = newOrder) }
categoryRepository.updatePartial(updates)
```

**Error Cases**: None

---

#### updateAllFlags(flags: Long?)
```kotlin
@Stable
suspend fun updateAllFlags(flags: Long?)
```

Updates flags for all categories.

**Parameters**:
- `flags`: The flags value to set, or null to clear flags

**Usage Example**:
```kotlin
// Clear all category flags
categoryRepository.updateAllFlags(null)
```

**Error Cases**: None

---

#### delete(categoryId: Long)
```kotlin
@Stable
suspend fun delete(categoryId: Long)
```

Deletes a category by its unique identifier.

**Parameters**:
- `categoryId`: The unique identifier of the category to delete

**Usage Example**:
```kotlin
categoryRepository.delete(categoryId)
```

**Error Cases**: Silently succeeds if category doesn't exist

---

#### deleteAll()
```kotlin
@Stable
suspend fun deleteAll()
```

Deletes all categories from the database.

**⚠️ WARNING**: This operation cannot be undone.

**Usage Example**:
```kotlin
// Always confirm with user before calling
if (userConfirmed) {
    categoryRepository.deleteAll()
}
```

**Error Cases**: None

---

## Migration Notes

All repository methods documented above are marked as **@Stable** and are safe to use in production code. No deprecated methods exist in the current API.

If you encounter any issues with these APIs, please refer to the [Repository Migration Guide](../migration/RepositoryMigration.md).

## Testing

All repository methods should be tested with:
1. Normal operation cases
2. Edge cases (empty lists, null values)
3. Error cases (database errors, concurrent access)
4. Performance tests for batch operations

## See Also

- [CatalogStore API Documentation](CatalogStoreAPI.md)
- [TranslationEngine API Documentation](TranslationEngineAPI.md)
- [Repository Migration Guide](../migration/RepositoryMigration.md)
