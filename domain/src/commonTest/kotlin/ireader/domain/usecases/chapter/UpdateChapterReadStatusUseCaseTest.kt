package ireader.domain.usecases.chapter

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Unit tests for UpdateChapterReadStatusUseCase
 * Tests chapter read status management functionality
 */
class UpdateChapterReadStatusUseCaseTest {
    
    private lateinit var useCase: UpdateChapterReadStatusUseCase
    private lateinit var chapterRepository: ChapterRepository
    
    @BeforeTest
    fun setup() {
        chapterRepository = mockk()
        useCase = UpdateChapterReadStatusUseCase(chapterRepository)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `invoke should mark chapter as read`() = runTest {
        // Given
        val chapterId = 1L
        val chapter = createTestChapter(chapterId, read = false)
        coEvery { chapterRepository.findChapterById(chapterId) } returns chapter
        coEvery { chapterRepository.insertChapter(any()) } just Runs
        
        // When
        useCase.invoke(chapterId, isRead = true)
        
        // Then
        coVerify {
            chapterRepository.insertChapter(match { it.id == chapterId && it.read == true })
        }
    }
    
    @Test
    fun `invoke should mark chapter as unread`() = runTest {
        // Given
        val chapterId = 1L
        val chapter = createTestChapter(chapterId, read = true)
        coEvery { chapterRepository.findChapterById(chapterId) } returns chapter
        coEvery { chapterRepository.insertChapter(any()) } just Runs
        
        // When
        useCase.invoke(chapterId, isRead = false)
        
        // Then
        coVerify {
            chapterRepository.insertChapter(match { it.id == chapterId && it.read == false })
        }
    }
    
    @Test
    fun `invoke should do nothing when chapter not found`() = runTest {
        // Given
        val chapterId = 1L
        coEvery { chapterRepository.findChapterById(chapterId) } returns null
        
        // When
        useCase.invoke(chapterId, isRead = true)
        
        // Then
        coVerify(exactly = 0) { chapterRepository.insertChapter(any()) }
    }
    
    @Test
    fun `updateMultiple should update all chapters`() = runTest {
        // Given
        val chapterIds = listOf(1L, 2L, 3L)
        chapterIds.forEach { id ->
            coEvery { chapterRepository.findChapterById(id) } returns createTestChapter(id, read = false)
        }
        coEvery { chapterRepository.insertChapter(any()) } just Runs
        
        // When
        useCase.updateMultiple(chapterIds, isRead = true)
        
        // Then
        coVerify(exactly = 3) { chapterRepository.insertChapter(any()) }
    }
    
    @Test
    fun `markAllAsRead should mark all book chapters as read`() = runTest {
        // Given
        val bookId = 1L
        val chapters = listOf(
            createTestChapter(1L, read = false),
            createTestChapter(2L, read = false),
            createTestChapter(3L, read = true)
        )
        coEvery { chapterRepository.findChaptersByBookId(bookId) } returns chapters
        coEvery { chapterRepository.insertChapters(any()) } just Runs
        
        // When
        useCase.markAllAsRead(bookId)
        
        // Then
        coVerify {
            chapterRepository.insertChapters(match { updatedChapters ->
                updatedChapters.size == 3 && updatedChapters.all { it.read == true }
            })
        }
    }
    
    @Test
    fun `markAllAsUnread should mark all book chapters as unread`() = runTest {
        // Given
        val bookId = 1L
        val chapters = listOf(
            createTestChapter(1L, read = true),
            createTestChapter(2L, read = true),
            createTestChapter(3L, read = false)
        )
        coEvery { chapterRepository.findChaptersByBookId(bookId) } returns chapters
        coEvery { chapterRepository.insertChapters(any()) } just Runs
        
        // When
        useCase.markAllAsUnread(bookId)
        
        // Then
        coVerify {
            chapterRepository.insertChapters(match { updatedChapters ->
                updatedChapters.size == 3 && updatedChapters.all { it.read == false }
            })
        }
    }
    
    @Test
    fun `markAllAsRead should handle empty chapter list`() = runTest {
        // Given
        val bookId = 1L
        coEvery { chapterRepository.findChaptersByBookId(bookId) } returns emptyList()
        coEvery { chapterRepository.insertChapters(any()) } just Runs
        
        // When
        useCase.markAllAsRead(bookId)
        
        // Then
        coVerify { chapterRepository.insertChapters(emptyList()) }
    }
    
    private fun createTestChapter(id: Long, read: Boolean): Chapter {
        return Chapter(
            id = id,
            bookId = 1L,
            key = "chapter-$id",
            name = "Chapter $id",
            dateUpload = 0L,
            number = id.toFloat(),
            sourceOrder = id.toInt(),
            read = read,
            bookmark = false,
            lastPageRead = 0L,
            dateFetch = 0L,
            translator = null,
            content = emptyList()
        )
    }
}
