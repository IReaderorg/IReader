package ireader.domain.usecases.remote

import ireader.core.source.model.Page
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.local.chapter_usecases.FindChapterById
import ireader.i18n.UiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * TDD Tests for FetchAndSaveChapterContentUseCase race condition fix.
 * 
 * These tests verify that concurrent fetch operations don't cause race conditions
 * where chapters are saved and then immediately deleted or overwritten.
 */
class FetchAndSaveChapterContentUseCaseTest {
    
    private lateinit var chapterRepository: FakeChapterRepository
    private lateinit var findChapterById: FindChapterById
    private lateinit var useCase: FetchAndSaveChapterContentUseCase
    
    @BeforeTest
    fun setup() {
        chapterRepository = FakeChapterRepository()
        findChapterById = FindChapterById(chapterRepository)
        useCase = FetchAndSaveChapterContentUseCase(chapterRepository, findChapterById)
    }
    
    /**
     * RED TEST: This test should FAIL initially because there's no synchronization
     * between concurrent fetch operations.
     */
    @Test
    fun `concurrent fetch operations should not cause race condition`() = runTest {
        // Arrange
        val chapter1 = createTestChapter(id = 1L, key = "chapter-1", bookId = 100L)
        val chapter2 = createTestChapter(id = 2L, key = "chapter-2", bookId = 100L)
        val catalog = createTestCatalog()
        
        var success1Called = false
        var success2Called = false
        
        // Act - Launch two concurrent fetch operations
        val job1 = launch {
            useCase(
                chapter = chapter1,
                catalog = catalog,
                onSuccess = { 
                    success1Called = true
                },
                onError = { }
            )
        }
        
        val job2 = launch {
            useCase(
                chapter = chapter2,
                catalog = catalog,
                onSuccess = { 
                    success2Called = true
                },
                onError = { }
            )
        }
        
        job1.join()
        job2.join()
        
        // Assert - Both chapters should be saved successfully
        assertTrue(success1Called, "Chapter 1 fetch should succeed")
        assertTrue(success2Called, "Chapter 2 fetch should succeed")
        
        val savedChapter1 = chapterRepository.findChapterById(1L)
        val savedChapter2 = chapterRepository.findChapterById(2L)
        
        assertNotNull(savedChapter1, "Chapter 1 should be in database")
        assertNotNull(savedChapter2, "Chapter 2 should be in database")
        assertTrue(savedChapter1.content.isNotEmpty(), "Chapter 1 should have content")
        assertTrue(savedChapter2.content.isNotEmpty(), "Chapter 2 should have content")
    }
    
    /**
     * RED TEST: This test should FAIL initially because prefetch and fetch
     * can interfere with each other.
     */
    @Test
    fun `prefetch and fetch of same chapter should not cause data loss`() = runTest {
        // Arrange
        val chapter = createTestChapter(id = 1L, key = "chapter-1", bookId = 100L)
        val catalog = createTestCatalog()
        
        var fetchSuccessCalled = false
        var prefetchSuccessCalled = false
        
        // Act - Simulate prefetch and fetch happening simultaneously
        val fetchJob = launch {
            delay(10) // Small delay to simulate network
            useCase(
                chapter = chapter,
                catalog = catalog,
                onSuccess = { 
                    fetchSuccessCalled = true
                },
                onError = { }
            )
        }
        
        val prefetchJob = launch {
            delay(5) // Prefetch starts slightly earlier
            useCase(
                chapter = chapter,
                catalog = catalog,
                onSuccess = { 
                    prefetchSuccessCalled = true
                },
                onError = { }
            )
        }
        
        fetchJob.join()
        prefetchJob.join()
        
        // Assert - Chapter should be saved and not deleted
        val savedChapter = chapterRepository.findChapterById(1L)
        assertNotNull(savedChapter, "Chapter should be in database after concurrent operations")
        assertTrue(savedChapter.content.isNotEmpty(), "Chapter should have content")
        assertTrue(fetchSuccessCalled || prefetchSuccessCalled, "At least one operation should succeed")
    }
    
    /**
     * RED TEST: This test should FAIL initially because there's no protection
     * against the same chapter being fetched multiple times simultaneously.
     */
    @Test
    fun `multiple fetch attempts for same chapter should be deduplicated`() = runTest {
        // Arrange
        val chapter = createTestChapter(id = 1L, key = "chapter-1", bookId = 100L)
        val catalog = createTestCatalog()
        
        var fetchCount = 0
        val mutex = Mutex()
        
        // Act - Launch 5 concurrent fetch operations for the same chapter
        val jobs = List(5) {
            launch {
                useCase(
                    chapter = chapter,
                    catalog = catalog,
                    onSuccess = { 
                        mutex.lock()
                        fetchCount++
                        mutex.unlock()
                    },
                    onError = { }
                )
            }
        }
        
        jobs.forEach { it.join() }
        
        // Assert - Should only fetch once (or at least not cause corruption)
        val savedChapter = chapterRepository.findChapterById(1L)
        assertNotNull(savedChapter, "Chapter should be in database")
        assertTrue(savedChapter.content.isNotEmpty(), "Chapter should have content")
        
        // Repository insert count should be reasonable (not 5x)
        assertTrue(
            chapterRepository.insertCallCount <= 2,
            "Should not insert same chapter multiple times (actual: ${chapterRepository.insertCallCount})"
        )
    }
    
    /**
     * RED TEST: Verify that read-back after save doesn't return null due to race condition
     */
    @Test
    fun `chapter should be readable immediately after save`() = runTest {
        // Arrange
        val chapter = createTestChapter(id = 1L, key = "chapter-1", bookId = 100L)
        val catalog = createTestCatalog()
        
        var savedChapter: Chapter? = null
        
        // Act
        useCase(
            chapter = chapter,
            catalog = catalog,
            onSuccess = { 
                savedChapter = it
            },
            onError = { }
        )
        
        // Assert
        assertNotNull(savedChapter, "onSuccess should be called with saved chapter")
        assertTrue(savedChapter!!.content.isNotEmpty(), "Saved chapter should have content")
        
        // Verify it's actually in the database
        val dbChapter = chapterRepository.findChapterById(1L)
        assertNotNull(dbChapter, "Chapter should be in database")
        assertEquals(savedChapter!!.id, dbChapter.id, "IDs should match")
    }
    
    // Helper functions
    
    private fun createTestChapter(
        id: Long,
        key: String,
        bookId: Long,
        content: List<Page> = emptyList()
    ): Chapter {
        return Chapter(
            id = id,
            bookId = bookId,
            key = key,
            name = "Test Chapter $id",
            content = content,
            number = id.toFloat(),
            sourceOrder = id.toInt(),
            dateFetch = 0L,
            dateUpload = 0L,
            translator = "",
            read = false,
            bookmark = false,
            lastPageRead = 0L,
            type = ""
        )
    }
    
    private fun createTestCatalog(): CatalogLocal {
        return CatalogLocal(
            sourceId = 1L,
            name = "Test Source",
            source = FakeSource()
        )
    }
}


// Fake implementations for testing

class FakeChapterRepository : ChapterRepository {
    private val chapters = mutableMapOf<Long, Chapter>()
    var insertCallCount = 0
    
    override suspend fun insertChapter(chapter: Chapter): Long {
        insertCallCount++
        // Simulate database delay
        delay(10)
        chapters[chapter.id] = chapter
        return chapter.id
    }
    
    override suspend fun findChapterById(chapterId: Long): Chapter? {
        return chapters[chapterId]
    }
    
    override suspend fun insertChapters(chapters: List<Chapter>): List<Long> {
        return chapters.map { insertChapter(it) }
    }
    
    // Stub implementations for other methods
    override fun subscribeChapterById(chapterId: Long) = kotlinx.coroutines.flow.flowOf(chapters[chapterId])
    override suspend fun findAllChapters() = chapters.values.toList()
    override suspend fun findAllInLibraryChapter() = emptyList<Chapter>()
    override suspend fun findChaptersByBookId(bookId: Long) = chapters.values.filter { it.bookId == bookId }
    override suspend fun findLastReadChapter(bookId: Long) = null
    override suspend fun subscribeLastReadChapter(bookId: Long) = kotlinx.coroutines.flow.flowOf(null)
    override suspend fun deleteChaptersByBookId(bookId: Long) { chapters.values.removeIf { it.bookId == bookId } }
    override suspend fun deleteChapters(chapters: List<Chapter>) { chapters.forEach { this.chapters.remove(it.id) } }
    override suspend fun deleteChapter(chapter: Chapter) { chapters.remove(chapter.id) }
    override suspend fun deleteAllChapters() { chapters.clear() }
    override suspend fun updateLastPageRead(chapterId: Long, lastPageRead: Long) {}
    override fun subscribeChaptersByBookId(bookId: Long) = kotlinx.coroutines.flow.flowOf(findChaptersByBookId(bookId))
    override suspend fun findChaptersByBookIdWithContent(bookId: Long) = findChaptersByBookId(bookId)
}

class FakeSource : ireader.core.source.CatalogSource {
    override val id: Long = 1L
    override val name: String = "Test Source"
    override val lang: String = "en"
    
    override suspend fun getPageList(chapter: ireader.core.source.model.ChapterInfo, commands: ireader.core.source.model.CommandList): List<Page> {
        // Simulate network delay
        delay(50)
        return listOf(
            ireader.core.source.model.Text("Test content for ${chapter.name}")
        )
    }
    
    override suspend fun getBookDetail(book: ireader.core.source.model.MangaInfo, commands: ireader.core.source.model.CommandList) = book
    override suspend fun getChapterList(book: ireader.core.source.model.MangaInfo, commands: ireader.core.source.model.CommandList) = emptyList<ireader.core.source.model.ChapterInfo>()
    override suspend fun getMangaList(sort: ireader.core.source.model.Listing?, page: Int) = ireader.core.source.model.MangasPageInfo(emptyList(), false)
    override suspend fun getMangaList(filters: ireader.core.source.model.FilterList, page: Int) = ireader.core.source.model.MangasPageInfo(emptyList(), false)
    override fun getFilters() = emptyList<ireader.core.source.model.Filter<*>>()
    override fun getListings() = emptyList<ireader.core.source.model.Listing>()
}
