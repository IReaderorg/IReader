package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReadingProgressDataTest {

    @Test
    fun `ReadingProgressData should be created with valid data`() {
        // Arrange
        val bookId = 123L
        val chapterId = 456L
        val chapterIndex = 5
        val offset = 1024
        val progress = 0.75f
        val lastReadAt = System.currentTimeMillis()

        // Act
        val progressData = ReadingProgressData(
            bookId = bookId,
            chapterId = chapterId,
            chapterIndex = chapterIndex,
            offset = offset,
            progress = progress,
            lastReadAt = lastReadAt
        )

        // Assert
        assertEquals(bookId, progressData.bookId)
        assertEquals(chapterId, progressData.chapterId)
        assertEquals(chapterIndex, progressData.chapterIndex)
        assertEquals(offset, progressData.offset)
        assertEquals(progress, progressData.progress)
        assertEquals(lastReadAt, progressData.lastReadAt)
    }

    @Test
    fun `ReadingProgressData should reject negative bookId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ReadingProgressData(
                bookId = -1L,
                chapterId = 456L,
                chapterIndex = 5,
                offset = 1024,
                progress = 0.75f,
                lastReadAt = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `ReadingProgressData should reject negative chapterId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ReadingProgressData(
                bookId = 123L,
                chapterId = -1L,
                chapterIndex = 5,
                offset = 1024,
                progress = 0.75f,
                lastReadAt = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `ReadingProgressData should reject negative chapterIndex`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ReadingProgressData(
                bookId = 123L,
                chapterId = 456L,
                chapterIndex = -1,
                offset = 1024,
                progress = 0.75f,
                lastReadAt = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `ReadingProgressData should reject negative offset`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ReadingProgressData(
                bookId = 123L,
                chapterId = 456L,
                chapterIndex = 5,
                offset = -1,
                progress = 0.75f,
                lastReadAt = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `ReadingProgressData should reject progress below 0`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ReadingProgressData(
                bookId = 123L,
                chapterId = 456L,
                chapterIndex = 5,
                offset = 1024,
                progress = -0.1f,
                lastReadAt = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `ReadingProgressData should reject progress above 1`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ReadingProgressData(
                bookId = 123L,
                chapterId = 456L,
                chapterIndex = 5,
                offset = 1024,
                progress = 1.1f,
                lastReadAt = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `ReadingProgressData should accept progress of 0`() {
        // Arrange & Act
        val progressData = ReadingProgressData(
            bookId = 123L,
            chapterId = 456L,
            chapterIndex = 5,
            offset = 0,
            progress = 0.0f,
            lastReadAt = System.currentTimeMillis()
        )

        // Assert
        assertEquals(0.0f, progressData.progress)
    }

    @Test
    fun `ReadingProgressData should accept progress of 1`() {
        // Arrange & Act
        val progressData = ReadingProgressData(
            bookId = 123L,
            chapterId = 456L,
            chapterIndex = 5,
            offset = 1024,
            progress = 1.0f,
            lastReadAt = System.currentTimeMillis()
        )

        // Assert
        assertEquals(1.0f, progressData.progress)
    }

    @Test
    fun `ReadingProgressData should reject negative lastReadAt timestamp`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            ReadingProgressData(
                bookId = 123L,
                chapterId = 456L,
                chapterIndex = 5,
                offset = 1024,
                progress = 0.75f,
                lastReadAt = -1L
            )
        }
    }
}
