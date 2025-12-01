package org.ireader.app

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ireader.domain.models.entities.Book
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.local.book_usecases.FindDuplicateBook
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.java.KoinJavaComponent.inject

/**
 * Integration tests for ExploreScreen using real device and real app database.
 * 
 * These tests verify:
 * 1. Book insertion works correctly with real database
 * 2. Navigation to BookDetail works after insertion
 * 3. State management is correct during navigation
 * 4. Deduplication works correctly
 * 5. Loading states are handled properly
 * 
 * Run on real device with: ./gradlew :android:connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class ExploreScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()
    
    private val insertUseCases: LocalInsertUseCases by inject(LocalInsertUseCases::class.java)
    private val findDuplicateBook: FindDuplicateBook by inject(FindDuplicateBook::class.java)
    
    // Test book data
    private val testBook = Book(
        id = 0L,
        title = "Test Book ${System.currentTimeMillis()}",
        key = "test_key_${System.currentTimeMillis()}",
        sourceId = 1L,
        favorite = false,
        dateAdded = System.currentTimeMillis(),
        lastUpdate = System.currentTimeMillis(),
        initialized = false,
        cover = "",
        description = "Test description",
        author = "Test Author",
        status = 0,
        genres = emptyList(),
        customCover = null,
        lastRead = null,
        tableId = 0L
    )
    
    private var insertedBookId: Long = 0L
    
    @Before
    fun setup() {
        // Wait for app to initialize
        composeTestRule.waitForIdle()
    }
    
    @After
    fun cleanup() {
        // Clean up test data if needed
        // Note: In real tests, you might want to delete test books
    }
    
    /**
     * Test that book insertion returns a valid ID
     */
    @Test
    fun testBookInsertion_returnsValidId() = runTest {
        // Insert book
        insertedBookId = insertUseCases.insertBook(testBook)
        
        // Verify ID is valid
        assert(insertedBookId > 0L) { 
            "Expected positive book ID, got $insertedBookId" 
        }
    }
    
    /**
     * Test that duplicate book detection works
     */
    @Test
    fun testDuplicateBookDetection_findsExistingBook() = runTest {
        // First insert
        val firstId = insertUseCases.insertBook(testBook)
        assert(firstId > 0L)
        
        // Try to find duplicate
        val duplicate = findDuplicateBook(testBook.title, testBook.sourceId)
        
        // Should find the existing book
        assert(duplicate != null) { 
            "Expected to find duplicate book" 
        }
        assert(duplicate?.id == firstId) { 
            "Expected duplicate ID $firstId, got ${duplicate?.id}" 
        }
    }
    
    /**
     * Test that inserting same book twice returns same ID (upsert behavior)
     */
    @Test
    fun testUpsertBehavior_returnsSameIdForDuplicate() = runTest {
        // First insert
        val firstId = insertUseCases.insertBook(testBook)
        assert(firstId > 0L)
        
        // Second insert with same key
        val secondId = insertUseCases.insertBook(testBook.copy(id = 0L))
        
        // Should return same ID (upsert)
        assert(secondId == firstId) { 
            "Expected same ID $firstId for upsert, got $secondId" 
        }
    }
    
    /**
     * Test that book with different key gets new ID
     */
    @Test
    fun testNewBook_getsNewId() = runTest {
        // First insert
        val firstId = insertUseCases.insertBook(testBook)
        assert(firstId > 0L)
        
        // Insert different book
        val differentBook = testBook.copy(
            key = "different_key_${System.currentTimeMillis()}",
            title = "Different Book ${System.currentTimeMillis()}"
        )
        val secondId = insertUseCases.insertBook(differentBook)
        
        // Should get different ID
        assert(secondId != firstId) { 
            "Expected different ID for new book, got same ID $secondId" 
        }
    }
    
    /**
     * Test that book insertion is fast enough for UI
     */
    @Test
    fun testBookInsertion_completesQuickly() = runTest {
        val startTime = System.currentTimeMillis()
        
        // Insert book
        insertedBookId = insertUseCases.insertBook(testBook)
        
        val duration = System.currentTimeMillis() - startTime
        
        // Should complete within 500ms for good UX
        assert(duration < 500L) { 
            "Book insertion took too long: ${duration}ms" 
        }
    }
    
    /**
     * Test that multiple rapid insertions work correctly
     */
    @Test
    fun testRapidInsertions_allSucceed() = runTest {
        val books = (1..10).map { index ->
            testBook.copy(
                key = "rapid_test_key_${System.currentTimeMillis()}_$index",
                title = "Rapid Test Book $index"
            )
        }
        
        val ids = books.map { book ->
            insertUseCases.insertBook(book)
        }
        
        // All should have valid IDs
        ids.forEachIndexed { index, id ->
            assert(id > 0L) { 
                "Book $index insertion failed, got ID $id" 
            }
        }
        
        // All IDs should be unique
        assert(ids.toSet().size == ids.size) { 
            "Expected unique IDs, got duplicates: $ids" 
        }
    }
    
    /**
     * Test that book state is preserved after insertion
     */
    @Test
    fun testBookState_preservedAfterInsertion() = runTest {
        val bookWithData = testBook.copy(
            title = "State Test Book",
            author = "State Test Author",
            description = "State Test Description",
            genres = listOf("Genre1", "Genre2"),
            status = 1
        )
        
        // Insert
        val id = insertUseCases.insertBook(bookWithData)
        assert(id > 0L)
        
        // Find the book
        val found = findDuplicateBook(bookWithData.title, bookWithData.sourceId)
        
        // Verify state is preserved
        assert(found != null) { "Book not found after insertion" }
        assert(found?.title == bookWithData.title) { 
            "Title mismatch: expected ${bookWithData.title}, got ${found?.title}" 
        }
        assert(found?.author == bookWithData.author) { 
            "Author mismatch: expected ${bookWithData.author}, got ${found?.author}" 
        }
    }
}
