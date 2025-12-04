//package ireader.domain.usecases.book
//
//import ireader.domain.models.updates.BookUpdate
//import io.mockk.*
//import kotlinx.coroutines.test.runTest
//import kotlin.test.*
//
///**
// * Comprehensive tests for AddToLibrary use case
// */
//class AddToLibraryTest {
//
//    private lateinit var addToLibrary: AddToLibrary
//    private lateinit var updateBook: UpdateBook
//
//    @BeforeTest
//    fun setup() {
//        updateBook = mockk()
//        addToLibrary = AddToLibrary(updateBook)
//    }
//
//    @AfterTest
//    fun tearDown() {
//        unmockkAll()
//    }
//
//    @Test
//    fun `await should successfully add book to library`() = runTest {
//        // Given
//        val bookId = 1L
//        coEvery { updateBook.await(any()) } returns true
//
//        // When
//        val result = addToLibrary.await(bookId)
//
//        // Then
//        assertTrue(result)
//        coVerify {
//            updateBook.await(match { update ->
//                update.id == bookId && update.favorite == true && update.dateAdded != null
//            })
//        }
//    }
//
//    @Test
//    fun `await should return false when update fails`() = runTest {
//        // Given
//        val bookId = 1L
//        coEvery { updateBook.await(any()) } returns false
//
//        // When
//        val result = addToLibrary.await(bookId)
//
//        // Then
//        assertFalse(result)
//        coVerify { updateBook.await(any()) }
//    }
//
//    @Test
//    fun `await should handle exceptions gracefully`() = runTest {
//        // Given
//        val bookId = 1L
//        coEvery { updateBook.await(any()) } throws RuntimeException("Database error")
//
//        // When
//        val result = addToLibrary.await(bookId)
//
//        // Then
//        assertFalse(result)
//    }
//
//    @Test
//    fun `awaitAll should successfully add multiple books to library`() = runTest {
//        // Given
//        val bookIds = listOf(1L, 2L, 3L)
//        coEvery { updateBook.awaitAll(any()) } returns true
//
//        // When
//        val result = addToLibrary.awaitAll(bookIds)
//
//        // Then
//        assertTrue(result)
//        coVerify {
//            updateBook.awaitAll(match { updates ->
//                updates.size == 3 &&
//                updates.all { it.favorite == true && it.dateAdded != null }
//            })
//        }
//    }
//
//    @Test
//    fun `awaitAll should return false when update fails`() = runTest {
//        // Given
//        val bookIds = listOf(1L, 2L, 3L)
//        coEvery { updateBook.awaitAll(any()) } returns false
//
//        // When
//        val result = addToLibrary.awaitAll(bookIds)
//
//        // Then
//        assertFalse(result)
//    }
//
//    @Test
//    fun `awaitAll should handle empty list`() = runTest {
//        // Given
//        val bookIds = emptyList<Long>()
//        coEvery { updateBook.awaitAll(any()) } returns true
//
//        // When
//        val result = addToLibrary.awaitAll(bookIds)
//
//        // Then
//        assertTrue(result)
//        coVerify { updateBook.awaitAll(emptyList()) }
//    }
//
//    @Test
//    fun `awaitAll should handle exceptions gracefully`() = runTest {
//        // Given
//        val bookIds = listOf(1L, 2L, 3L)
//        coEvery { updateBook.awaitAll(any()) } throws RuntimeException("Database error")
//
//        // When
//        val result = addToLibrary.awaitAll(bookIds)
//
//        // Then
//        assertFalse(result)
//    }
//
//    @Test
//    fun `awaitAll should use same timestamp for all books`() = runTest {
//        // Given
//        val bookIds = listOf(1L, 2L, 3L)
//        var capturedUpdates: List<BookUpdate>? = null
//        coEvery { updateBook.awaitAll(any()) } answers {
//            capturedUpdates = firstArg()
//            true
//        }
//
//        // When
//        addToLibrary.awaitAll(bookIds)
//
//        // Then
//        assertNotNull(capturedUpdates)
//        val timestamps = capturedUpdates!!.mapNotNull { it.dateAdded }.distinct()
//        assertEquals(1, timestamps.size, "All books should have the same timestamp")
//    }
//}
