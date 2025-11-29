package ireader.domain.models.entities

import ireader.core.source.model.MangaInfo
import kotlin.test.*

/**
 * Unit tests for Book entity and related functions
 */
class BookTest {

    // ==================== Book Creation Tests ====================

    @Test
    fun `book default values are correct`() {
        val book = Book(
            sourceId = 1L,
            title = "Test Book",
            key = "test-key"
        )
        
        assertEquals(0L, book.id)
        assertEquals("", book.author)
        assertEquals("", book.description)
        assertTrue(book.genres.isEmpty())
        assertEquals(0L, book.status)
        assertEquals("", book.cover)
        assertEquals("", book.customCover)
        assertFalse(book.favorite)
        assertEquals(0L, book.lastUpdate)
        assertFalse(book.initialized)
        assertEquals(0L, book.dateAdded)
        assertEquals(0L, book.viewer)
        assertEquals(0L, book.flags)
        assertFalse(book.isPinned)
        assertEquals(0, book.pinnedOrder)
        assertFalse(book.isArchived)
    }

    @Test
    fun `book with all fields set`() {
        val genres = listOf("Fantasy", "Adventure")
        val book = Book(
            id = 1L,
            sourceId = 100L,
            title = "Epic Novel",
            key = "epic-novel",
            author = "John Doe",
            description = "An epic adventure",
            genres = genres,
            status = Book.ONGOING.toLong(),
            cover = "https://example.com/cover.jpg",
            customCover = "https://example.com/custom.jpg",
            favorite = true,
            lastUpdate = 1234567890L,
            initialized = true,
            dateAdded = 1234567800L,
            viewer = 1L,
            flags = 2L,
            isPinned = true,
            pinnedOrder = 5,
            isArchived = false
        )
        
        assertEquals(1L, book.id)
        assertEquals(100L, book.sourceId)
        assertEquals("Epic Novel", book.title)
        assertEquals("epic-novel", book.key)
        assertEquals("John Doe", book.author)
        assertEquals("An epic adventure", book.description)
        assertEquals(genres, book.genres)
        assertEquals(Book.ONGOING.toLong(), book.status)
        assertTrue(book.favorite)
        assertTrue(book.isPinned)
        assertEquals(5, book.pinnedOrder)
    }

    // ==================== Status Tests ====================

    @Test
    fun `getStatusByName returns correct status names`() {
        val unknownBook = Book(sourceId = 1L, title = "Test", key = "test", status = 0L)
        val ongoingBook = Book(sourceId = 1L, title = "Test", key = "test", status = 1L)
        val completedBook = Book(sourceId = 1L, title = "Test", key = "test", status = 2L)
        val licensedBook = Book(sourceId = 1L, title = "Test", key = "test", status = 3L)
        val invalidBook = Book(sourceId = 1L, title = "Test", key = "test", status = 99L)
        
        assertEquals("UNKNOWN", unknownBook.getStatusByName())
        assertEquals("ONGOING", ongoingBook.getStatusByName())
        assertEquals("COMPLETED", completedBook.getStatusByName())
        assertEquals("LICENSED", licensedBook.getStatusByName())
        assertEquals("UNKNOWN", invalidBook.getStatusByName())
    }

    @Test
    fun `allStatus returns all status options`() {
        val book = Book(sourceId = 1L, title = "Test", key = "test")
        
        val statuses = book.allStatus()
        
        assertEquals(4, statuses.size)
        assertTrue(statuses.contains("UNKNOWN"))
        assertTrue(statuses.contains("ONGOING"))
        assertTrue(statuses.contains("COMPLETED"))
        assertTrue(statuses.contains("LICENSED"))
    }

    @Test
    fun `status constants are correct`() {
        assertEquals(0, Book.UNKNOWN)
        assertEquals(1, Book.ONGOING)
        assertEquals(2, Book.COMPLETED)
        assertEquals(3, Book.LICENSED)
    }

    // ==================== Conversion Tests ====================

    @Test
    fun `toBookInfo converts book to MangaInfo`() {
        val book = Book(
            id = 1L,
            sourceId = 100L,
            title = "Test Book",
            key = "test-key",
            author = "Author Name",
            description = "Book description",
            genres = listOf("Genre1", "Genre2"),
            status = Book.ONGOING.toLong(),
            cover = "https://example.com/cover.jpg"
        )
        
        val mangaInfo = Book.Companion.run { book.toBookInfo() }
        
        assertEquals("Test Book", mangaInfo.title)
        assertEquals("test-key", mangaInfo.key)
        assertEquals("Author Name", mangaInfo.author)
        assertEquals("Book description", mangaInfo.description)
        assertEquals(listOf("Genre1", "Genre2"), mangaInfo.genres)
        assertEquals(Book.ONGOING.toLong(), mangaInfo.status)
        assertEquals("https://example.com/cover.jpg", mangaInfo.cover)
    }

    @Test
    fun `MangaInfo toBook conversion`() {
        val mangaInfo = MangaInfo(
            key = "manga-key",
            title = "Manga Title",
            author = "Manga Author",
            description = "Manga description",
            genres = listOf("Action", "Drama"),
            status = Book.COMPLETED.toLong(),
            cover = "https://example.com/manga.jpg"
        )
        
        val book = mangaInfo.toBook(sourceId = 50L, bookId = 10L)
        
        assertEquals(10L, book.id)
        assertEquals(50L, book.sourceId)
        assertEquals("Manga Title", book.title)
        assertEquals("manga-key", book.key)
        assertEquals("Manga Author", book.author)
        assertEquals("Manga description", book.description)
        assertEquals(listOf("Action", "Drama"), book.genres)
        assertEquals(Book.COMPLETED.toLong(), book.status)
        assertEquals("https://example.com/manga.jpg", book.cover)
        assertFalse(book.favorite)
    }

    @Test
    fun `fromBookInfo creates book with default id`() {
        val mangaInfo = MangaInfo(
            key = "info-key",
            title = "Info Title",
            author = "Info Author",
            description = "Info description",
            genres = listOf("Romance"),
            status = Book.LICENSED.toLong(),
            cover = "https://example.com/info.jpg"
        )
        
        val book = mangaInfo.fromBookInfo(sourceId = 25L)
        
        assertEquals(0L, book.id)
        assertEquals(25L, book.sourceId)
        assertEquals("Info Title", book.title)
        assertEquals("info-key", book.key)
        assertEquals(0L, book.dateAdded)
        assertEquals(0L, book.lastUpdate)
    }

    // ==================== BookItem Tests ====================

    @Test
    fun `BookItem toBook conversion`() {
        val bookItem = BookItem(
            id = 5L,
            sourceId = 10L,
            title = "Item Title",
            key = "item-key",
            cover = "https://example.com/item.jpg",
            customCover = "https://example.com/custom.jpg",
            author = "Item Author",
            description = "Item description",
            favorite = true
        )
        
        val book = bookItem.toBook()
        
        assertEquals(5L, book.id)
        assertEquals(10L, book.sourceId)
        assertEquals("Item Title", book.title)
        assertEquals("item-key", book.key)
        assertEquals("https://example.com/item.jpg", book.cover)
        assertEquals("https://example.com/custom.jpg", book.customCover)
        assertEquals("Item Author", book.author)
        assertTrue(book.favorite)
    }

    @Test
    fun `Book toBookItem conversion`() {
        val book = Book(
            id = 7L,
            sourceId = 15L,
            title = "Book Title",
            key = "book-key",
            cover = "https://example.com/book.jpg",
            customCover = "https://example.com/book-custom.jpg",
            author = "Book Author",
            description = "Book description",
            favorite = false
        )
        
        val bookItem = book.toBookItem()
        
        assertEquals(7L, bookItem.id)
        assertEquals(15L, bookItem.sourceId)
        assertEquals("Book Title", bookItem.title)
        assertEquals("book-key", bookItem.key)
        assertEquals("https://example.com/book.jpg", bookItem.cover)
        assertEquals("https://example.com/book-custom.jpg", bookItem.customCover)
        assertEquals("Book Author", bookItem.author)
        assertEquals("Book description", bookItem.description)
        assertFalse(bookItem.favorite)
    }

    // ==================== LibraryBook Tests ====================

    @Test
    fun `LibraryBook totalChapters calculation`() {
        val libraryBook = LibraryBook(
            id = 1L,
            sourceId = 1L,
            key = "key",
            title = "Title",
            status = 0L,
            cover = ""
        )
        libraryBook.readCount = 5
        libraryBook.unreadCount = 10
        
        assertEquals(15, libraryBook.totalChapters)
    }

    @Test
    fun `LibraryBook hasStarted is true when readCount greater than zero`() {
        val libraryBook = LibraryBook(
            id = 1L,
            sourceId = 1L,
            key = "key",
            title = "Title",
            status = 0L,
            cover = ""
        )
        libraryBook.readCount = 1
        
        assertTrue(libraryBook.hasStarted)
    }

    @Test
    fun `LibraryBook hasStarted is false when readCount is zero`() {
        val libraryBook = LibraryBook(
            id = 1L,
            sourceId = 1L,
            key = "key",
            title = "Title",
            status = 0L,
            cover = ""
        )
        libraryBook.readCount = 0
        
        assertFalse(libraryBook.hasStarted)
    }

    @Test
    fun `LibraryBook toBookItem conversion`() {
        val libraryBook = LibraryBook(
            id = 3L,
            sourceId = 5L,
            key = "lib-key",
            title = "Library Book",
            status = Book.ONGOING.toLong(),
            cover = "https://example.com/lib.jpg"
        )
        libraryBook.unreadCount = 3
        libraryBook.readCount = 7
        libraryBook.lastRead = 1234567890L
        libraryBook.isPinned = true
        libraryBook.pinnedOrder = 2
        libraryBook.isArchived = false
        
        val bookItem = libraryBook.toBookItem()
        
        assertEquals(3L, bookItem.id)
        assertEquals(5L, bookItem.sourceId)
        assertEquals("Library Book", bookItem.title)
        assertEquals("lib-key", bookItem.key)
        assertEquals(3, bookItem.unread)
        assertEquals(7, bookItem.downloaded)
        assertEquals(10, bookItem.totalChapters)
        assertEquals(1234567890L, bookItem.lastRead)
        assertTrue(bookItem.isPinned)
        assertEquals(2, bookItem.pinnedOrder)
        assertFalse(bookItem.isArchived)
    }

    @Test
    fun `LibraryBook toBook conversion`() {
        val libraryBook = LibraryBook(
            id = 4L,
            sourceId = 6L,
            key = "lib-key-2",
            title = "Another Library Book",
            status = Book.COMPLETED.toLong(),
            cover = "https://example.com/lib2.jpg"
        )
        
        val book = libraryBook.toBook()
        
        assertEquals(4L, book.id)
        assertEquals(6L, book.sourceId)
        assertEquals("Another Library Book", book.title)
        assertEquals("lib-key-2", book.key)
    }

    // ==================== String Extension Tests ====================

    @Test
    fun `takeIf returns value when condition is true`() {
        val value = "test"
        
        val result = value.takeIf({ true }, "default")
        
        assertEquals("test", result)
    }

    @Test
    fun `takeIf returns default when condition is false`() {
        val value = "test"
        
        val result = value.takeIf({ false }, "default")
        
        assertEquals("default", result)
    }
}
