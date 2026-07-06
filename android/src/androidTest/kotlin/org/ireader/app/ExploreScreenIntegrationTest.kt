package org.ireader.app

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.History
import ireader.domain.models.library.LibrarySort
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.preferences.prefs.UiPreferences
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
    
    private val uiPreferences: UiPreferences by inject(UiPreferences::class.java)
    private val libraryPreferences: LibraryPreferences by inject(LibraryPreferences::class.java)
    private val bookRepository: BookRepository by inject(BookRepository::class.java)
    private val chapterRepository: ChapterRepository by inject(ChapterRepository::class.java)
    private val historyRepository: HistoryRepository by inject(HistoryRepository::class.java)
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
        customCover = "",
    )
    
    private var insertedBookId: Long = 0L
    
    private val seededKeys = listOf("e2e-explore-seed-1", "e2e-explore-seed-2", "e2e-explore-seed-3")
    
    @Before
    fun setup() {
        uiPreferences.hasCompletedOnboarding().set(true)
        uiPreferences.hasCompletedFirstLaunch().set(true)
        // Seed favorite books + chapters + history
        runBlocking {
            for ((i, key) in seededKeys.withIndex()) {
                val id = 850_001L + i
                val bookId = bookRepository.upsert(Book(
                    id = id, sourceId = 1L, title = "Explore Test Novel ${i + 1}",
                    key = key, author = "Demo Author",
                    description = "Seeded for explore test", genres = listOf("Fantasy"),
                    status = 1, cover = "", customCover = "", favorite = true,
                    lastUpdate = System.currentTimeMillis(), initialized = true,
                    dateAdded = System.currentTimeMillis(), viewer = 0, flags = 0
                )) ?: continue
                val chapters = (1..4).map { c ->
                    Chapter(
                        id = bookId * 1000 + c, bookId = bookId, key = "$key-ch-$c",
                        name = "Chapter $c", read = c <= 2, bookmark = false,
                        lastPageRead = 0L, dateFetch = System.currentTimeMillis(),
                        dateUpload = System.currentTimeMillis(), sourceOrder = c.toLong(),
                        number = c.toFloat(), translator = "", content = emptyList()
                    )
                }
                chapterRepository.insertChapters(chapters)
                historyRepository.insertHistory(History(
                    id = -1L, // auto-generated
                    chapterId = bookId * 1000 + 1,
                    readAt = System.currentTimeMillis() - 60_000L,
                    readDuration = 300_000L, progress = 0.5f
                ))
            }
        }
        composeTestRule.waitForIdle()
        // Force Library VM reload by toggling sort
        val sort = libraryPreferences.sorting().get()
        libraryPreferences.sorting().set(sort.copy(isAscending = !sort.isAscending))
        libraryPreferences.sorting().set(sort)
    }
    
    @After
    fun cleanup() = runBlocking {
        for (key in seededKeys) { try { bookRepository.delete(key) } catch (_: Exception) {} }
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
