//package ireader.domain.usecases.book
//
//import ireader.domain.models.entities.Book
//import io.mockk.*
//import kotlinx.coroutines.test.runTest
//import kotlin.test.*
//
///**
// * Comprehensive tests for ToggleFavorite use case
// */
//class ToggleFavoriteTest {
//
//    private lateinit var toggleFavorite: ToggleFavorite
//    private lateinit var getBook: GetBook
//    private lateinit var addToLibrary: AddToLibrary
//    private lateinit var removeFromLibrary: RemoveFromLibrary
//
//    @BeforeTest
//    fun setup() {
//        getBook = mockk()
//        addToLibrary = mockk()
//        removeFromLibrary = mockk()
//        toggleFavorite = ToggleFavorite(getBook, addToLibrary, removeFromLibrary)
//    }
//
//    @AfterTest
//    fun tearDown() {
//        unmockkAll()
//    }
//
//    @Test
//    fun `await with bookId should add non-favorite book to library`() = runTest {
//        // Given
//        val bookId = 1L
//        val book = createTestBook(bookId, favorite = false)
//        coEvery { getBook.await(bookId) } returns book
//        coEvery { addToLibrary.await(bookId) } returns true
//
//        // When
//        val result = toggleFavorite.await(bookId)
//
//        // Then
//        assertTrue(result)
//        coVerify { addToLibrary.await(bookId) }
//        coVerify(exactly = 0) { removeFromLibrary.await(any()) }
//    }
//
//    @Test
//    fun `await with bookId should remove favorite book from library`() = runTest {
//        // Given
//        val bookId = 1L
//        val book = createTestBook(bookId, favorite = true)
//        coEvery { getBook.await(bookId) } returns book
//        coEvery { removeFromLibrary.await(bookId) } returns true
//
//        // When
//        val result = toggleFavorite.await(bookId)
//
//        // Then
//        assertTrue(result)
//        coVerify { removeFromLibrary.await(bookId) }
//        coVerify(exactly = 0) { addToLibrary.await(any()) }
//    }
//
//    @Test
//    fun `await with bookId should return false when book not found`() = runTest {
//        // Given
//        val bookId = 999L
//        coEvery { getBook.await(bookId) } returns null
//
//        // When
//        val result = toggleFavorite.await(bookId)
//
//        // Then
//        assertFalse(result)
//        coVerify(exactly = 0) { addToLibrary.await(any()) }
//        coVerify(exactly = 0) { removeFromLibrary.await(any()) }
//    }
//
//    @Test
//    fun `await with bookId should return false when add fails`() = runTest {
//        // Given
//        val bookId = 1L
//        val book = createTestBook(bookId, favorite = false)
//        coEvery { getBook.await(bookId) } returns book
//        coEvery { addToLibrary.await(bookId) } returns false
//
//        // When
//        val result = toggleFavorite.await(bookId)
//
//        // Then
//        assertFalse(result)
//    }
//
//    @Test
//    fun `await with bookId should return false when remove fails`() = runTest {
//        // Given
//        val bookId = 1L
//        val book = createTestBook(bookId, favorite = true)
//        coEvery { getBook.await(bookId) } returns book
//        coEvery { removeFromLibrary.await(bookId) } returns false
//
//        // When
//        val result = toggleFavorite.await(bookId)
//
//        // Then
//        assertFalse(result)
//    }
//
//    @Test
//    fun `await with bookId should handle exceptions gracefully`() = runTest {
//        // Given
//        val bookId = 1L
//        coEvery { getBook.await(bookId) } throws RuntimeException("Database error")
//
//        // When
//        val result = toggleFavorite.await(bookId)
//
//        // Then
//        assertFalse(result)
//    }
//
//    @Test
//    fun `await with book entity should add non-favorite book to library`() = runTest {
//        // Given
//        val book = createTestBook(1L, favorite = false)
//        coEvery { addToLibrary.await(book.id) } returns true
//
//        // When
//        val result = toggleFavorite.await(book)
//
//        // Then
//        assertTrue(result)
//        coVerify { addToLibrary.await(book.id) }
//        coVerify(exactly = 0) { removeFromLibrary.await(any()) }
//    }
//
//    @Test
//    fun `await with book entity should remove favorite book from library`() = runTest {
//        // Given
//        val book = createTestBook(1L, favorite = true)
//        coEvery { removeFromLibrary.await(book.id) } returns true
//
//        // When
//        val result = toggleFavorite.await(book)
//
//        // Then
//        assertTrue(result)
//        coVerify { removeFromLibrary.await(book.id) }
//        coVerify(exactly = 0) { addToLibrary.await(any()) }
//    }
//
//    @Test
//    fun `await with book entity should return false when add fails`() = runTest {
//        // Given
//        val book = createTestBook(1L, favorite = false)
//        coEvery { addToLibrary.await(book.id) } returns false
//
//        // When
//        val result = toggleFavorite.await(book)
//
//        // Then
//        assertFalse(result)
//    }
//
//    @Test
//    fun `await with book entity should return false when remove fails`() = runTest {
//        // Given
//        val book = createTestBook(1L, favorite = true)
//        coEvery { removeFromLibrary.await(book.id) } returns false
//
//        // When
//        val result = toggleFavorite.await(book)
//
//        // Then
//        assertFalse(result)
//    }
//
//    @Test
//    fun `await with book entity should handle exceptions gracefully`() = runTest {
//        // Given
//        val book = createTestBook(1L, favorite = false)
//        coEvery { addToLibrary.await(book.id) } throws RuntimeException("Database error")
//
//        // When
//        val result = toggleFavorite.await(book)
//
//        // Then
//        assertFalse(result)
//    }
//
//    private fun createTestBook(
//        id: Long,
//        title: String = "Test Book",
//        favorite: Boolean = false
//    ): Book {
//        return Book(
//            id = id,
//            sourceId = 1L,
//            title = title,
//            key = "test-book-$id",
//            author = "Test Author",
//            description = "Test Description",
//            cover = "https://example.com/cover.jpg",
//            favorite = favorite
//        )
//    }
//}
