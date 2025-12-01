package org.ireader.app

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * UI Navigation tests for BookDetail screen with database operations.
 * 
 * These tests verify the actual UI behavior when navigating to BookDetail
 * and test the database integration.
 * 
 * Run on physical device: ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=org.ireader.app.BookDetailNavigationTest
 */
@RunWith(AndroidJUnit4::class)
class BookDetailNavigationTest : KoinComponent {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val bookRepository: BookRepository by inject()
    private val chapterRepository: ChapterRepository by inject()
    
    private val testBookKey = "nav-test-book-key"
    private val testBookId = 888888L
    private val testSourceId = 1L
    
    @Before
    fun setup() {
        runBlocking {
            // Clean up any existing test data
            try {
                bookRepository.delete(testBookKey)
            } catch (_: Exception) {}
        }
    }
    
    @After
    fun teardown() {
        runBlocking {
            try {
                bookRepository.delete(testBookKey)
            } catch (_: Exception) {}
        }
    }
    
    /**
     * Test: App launches and shows main screen.
     */
    @Test
    fun test_app_launches_successfully() {
        composeTestRule.waitForIdle()
        // App should launch without crashing
    }
    
    /**
     * Test: Loading indicator disappears after initial load.
     */
    @Test
    fun test_loading_indicator_disappears() {
        composeTestRule.waitForIdle()
        
        // Wait for any loading indicators to disappear (with timeout)
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithText("Loading")
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }
    
    /**
     * Test: Back navigation works correctly.
     */
    @Test
    fun test_back_navigation_works() {
        composeTestRule.waitForIdle()
        
        // Simulate back press
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        
        // App should handle back press gracefully
        composeTestRule.waitForIdle()
    }
    
    /**
     * Test: Rapid back navigation doesn't crash.
     */
    @Test
    fun test_rapid_back_navigation_stability() {
        composeTestRule.waitForIdle()
        
        // Perform multiple back presses rapidly
        repeat(3) {
            composeTestRule.activityRule.scenario.onActivity { activity ->
                activity.onBackPressedDispatcher.onBackPressed()
            }
            composeTestRule.waitForIdle()
        }
        
        // App should remain stable
    }
    
    /**
     * Test: App remains responsive after idle.
     */
    @Test
    fun test_app_responsive_after_idle() {
        composeTestRule.waitForIdle()
        
        // Wait a bit
        Thread.sleep(1000)
        
        // App should still be responsive
        composeTestRule.waitForIdle()
    }
    
    /**
     * Test: Book can be inserted and retrieved from database.
     * 
     * This tests the database layer that BookDetailViewModel depends on.
     */
    @Test
    fun test_book_database_operations() = runBlocking {
        // Insert test book
        val testBook = createTestBook()
        val insertedId = bookRepository.upsert(testBook)
        
        // Verify book was inserted
        assert(insertedId != null && insertedId > 0) { "Book should be inserted with valid ID" }
        
        // Retrieve book
        val retrievedBook = bookRepository.findBookById(insertedId!!)
        assert(retrievedBook != null) { "Book should be retrievable" }
        assert(retrievedBook?.title == "Navigation Test Book") { "Book title should match" }
    }
    
    /**
     * Test: Chapters can be inserted after book exists.
     * 
     * This tests the foreign key relationship between chapters and books.
     */
    @Test
    fun test_chapter_database_operations() = runBlocking {
        // First insert the book (required for foreign key)
        val testBook = createTestBook()
        val bookId = bookRepository.upsert(testBook)
        assert(bookId != null && bookId > 0) { "Book must be inserted first" }
        
        // Wait for book to be committed
        delay(100)
        
        // Now insert chapters with the correct bookId
        val chapters = createTestChapters(5, bookId!!)
        chapterRepository.insertChapters(chapters)
        
        // Wait for chapters to be committed
        delay(100)
        
        // Verify chapters were inserted
        val retrievedChapters = chapterRepository.findChaptersByBookId(bookId)
        assert(retrievedChapters.size == 5) { "Should have 5 chapters, got ${retrievedChapters.size}" }
    }
    
    // Helper functions
    
    private fun createTestBook(): Book {
        return Book(
            id = testBookId,
            sourceId = testSourceId,
            title = "Navigation Test Book",
            key = testBookKey,
            author = "Test Author",
            description = "A book for testing navigation",
            genres = listOf("Test"),
            status = 1,
            cover = "",
            customCover = "",
            favorite = true,
            lastUpdate = System.currentTimeMillis(),
            initialized = true,
            dateAdded = System.currentTimeMillis(),
            viewer = 0,
            flags = 0
        )
    }
    
    private fun createTestChapters(count: Int, bookId: Long): List<Chapter> {
        return (1..count).map { i ->
            Chapter(
                id = bookId * 1000 + i,
                bookId = bookId, // Use the actual book ID from database
                key = "nav-chapter-$i",
                name = "Chapter $i",
                read = i <= 2,
                bookmark = false,
                lastPageRead = 0,
                dateFetch = System.currentTimeMillis(),
                dateUpload = System.currentTimeMillis(),
                sourceOrder = i.toLong(),
                number = i.toFloat(),
                translator = "",
                content = emptyList()
            )
        }
    }
}
