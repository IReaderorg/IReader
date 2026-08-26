package ireader.domain.models.entities

import ireader.core.source.model.RecommendationInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RecommendationTest {

    @Test
    fun `default recommendation values`() {
        val rec = Recommendation(
            key = "/novel/123",
            title = "Test Novel"
        )

        assertEquals("/novel/123", rec.key)
        assertEquals("Test Novel", rec.title)
        assertEquals("", rec.cover)
        assertTrue(rec.genres.isEmpty())
        assertEquals(0L, rec.sourceId)
        assertEquals("", rec.sourceName)
        assertTrue(rec.isValid())
    }

    @Test
    fun `isValid returns false for blank key or title`() {
        val blankKey = Recommendation(key = "", title = "Valid Title")
        val blankTitle = Recommendation(key = "valid-key", title = "   ")

        assertFalse(blankKey.isValid())
        assertFalse(blankTitle.isValid())
    }

    @Test
    fun `fromSourceModel maps all fields correctly`() {
        val info = RecommendationInfo(
            key = "/novel/abc",
            title = "Awesome Novel",
            cover = "https://example.com/cover.png",
            genres = listOf("Action", "Fantasy"),
            sourceId = 42L,
            sourceName = "NovelSource"
        )

        val rec = Recommendation.fromSourceModel(info)

        assertEquals(info.key, rec.key)
        assertEquals(info.title, rec.title)
        assertEquals(info.cover, rec.cover)
        assertEquals(info.genres, rec.genres)
        assertEquals(info.sourceId, rec.sourceId)
        assertEquals(info.sourceName, rec.sourceName)
        assertTrue(rec.isValid())
    }
}
