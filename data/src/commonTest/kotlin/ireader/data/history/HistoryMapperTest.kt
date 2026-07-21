package ireader.data.history

import ireader.domain.models.entities.History
import ireader.domain.models.entities.HistoryWithRelations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HistoryMapperTest {

    @Test
    fun historyMapperShouldMapAllFields() {
        // historyMapper: (Long, Long, Long?, Long, Double?) -> History
        val history = historyMapper(
            1L,      // id
            100L,    // chapterId
            1234567890L, // readAt
            300L,    // readDuration
            0.75     // progress
        )

        assertEquals(1L, history.id)
        assertEquals(100L, history.chapterId)
        assertEquals(1234567890L, history.readAt)
        assertEquals(300L, history.readDuration)
        assertEquals(0.75f, history.progress)
    }

    @Test
    fun historyMapperShouldHandleNullReadAt() {
        val history = historyMapper(
            1L, 100L, null, 300L, 0.5
        )

        assertEquals(1L, history.id)
        assertEquals(100L, history.chapterId)
        assertNull(history.readAt)
    }

    @Test
    fun historyMapperShouldHandleNullProgress() {
        val history = historyMapper(
            1L, 100L, 1234567890L, 300L, null
        )

        assertEquals(1L, history.id)
        assertEquals(100L, history.chapterId)
        assertEquals(1234567890L, history.readAt)
        assertEquals(300L, history.readDuration)
        assertEquals(0.0f, history.progress) // null progress defaults to 0.0
    }

    @Test
    fun historyMapperShouldHandleAllNulls() {
        val history = historyMapper(
            1L, 100L, null, 0L, null
        )

        assertEquals(1L, history.id)
        assertEquals(100L, history.chapterId)
        assertNull(history.readAt)
        assertEquals(0L, history.readDuration)
        assertEquals(0.0f, history.progress)
    }

    @Test
    fun historyWithRelationsMapperShouldMapAllFields() {
        // historyWithRelationsMapper: (Long, Long, Long, String, String?, String, Long, Boolean, Long, Float, Long?, Long, Double?, String) -> HistoryWithRelations
        val relations = historyWithRelationsMapper(
            1L,         // historyId
            100L,       // bookId
            200L,       // chapterId
            "Test Book", // title
            "https://example.com/thumb.jpg", // thumbnailUrl
            "",         // customCover
            1000L,      // source
            true,       // favorite
            500L,       // cover_last_modified
            1.5f,       // chapterNumber
            1234567890L, // readAt
            300L,       // readDuration
            0.75,       // progress
            "Chapter 1" // chapterName
        )

        assertEquals(1L, relations.id)
        assertEquals(200L, relations.chapterId)
        assertEquals(100L, relations.bookId)
        assertEquals("Test Book", relations.title)
        assertEquals(1.5f, relations.chapterNumber)
        assertEquals(1234567890L, relations.readAt)
        assertEquals(300L, relations.readDuration)
        assertEquals("Chapter 1", relations.chapterName)

        // BookCover assertions
        assertEquals(100L, relations.coverData.bookId)
        assertEquals(1000L, relations.coverData.sourceId)
        assertEquals(true, relations.coverData.favorite)
        assertEquals("https://example.com/thumb.jpg", relations.coverData.cover)
        assertEquals(500L, relations.coverData.lastModified)
        assertEquals(false, relations.coverData.hasCustomCover)
    }

    @Test
    fun historyWithRelationsMapperShouldUseCustomCoverWhenProvided() {
        val relations = historyWithRelationsMapper(
            1L, 100L, 200L, "Book", "https://example.com/thumb.jpg",
            "https://example.com/custom.jpg", // customCover
            1000L, true, 500L, 1f, 1234567890L, 300L, 0.5, "Ch 1"
        )

        assertEquals("https://example.com/custom.jpg", relations.coverData.cover)
        assertEquals(true, relations.coverData.hasCustomCover)
    }

    @Test
    fun historyWithRelationsMapperShouldFallbackToThumbnailWhenCustomCoverSameAsThumbnail() {
        val relations = historyWithRelationsMapper(
            1L, 100L, 200L, "Book", "https://example.com/thumb.jpg",
            "https://example.com/thumb.jpg", // customCover same as thumbnail
            1000L, true, 500L, 1f, 1234567890L, 300L, 0.5, "Ch 1"
        )

        assertEquals("https://example.com/thumb.jpg", relations.coverData.cover)
        assertEquals(false, relations.coverData.hasCustomCover)
    }

    @Test
    fun historyWithRelationsMapperShouldHandleNullReadAt() {
        val relations = historyWithRelationsMapper(
            1L, 100L, 200L, "Book", "thumb.jpg", "", 1000L, false, 500L, 1f,
            null, // readAt is null
            300L, 0.5, "Ch 1"
        )

        assertEquals(0L, relations.readAt) // null readAt defaults to 0
    }
}
