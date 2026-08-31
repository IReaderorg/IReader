package ireader.domain.usecases.remote

import ireader.core.source.Source
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.Page
import ireader.core.source.model.RecommendationInfo
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogBundled
import ireader.domain.models.entities.Recommendation
import ireader.i18n.UiText
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GetSourceRecommendationsTest {

    private class FakeRecommendationSource(
        override val id: Long = 100L,
        override val name: String = "FakeSource",
        override val lang: String = "en",
        private val recommendationsToReturn: List<RecommendationInfo> = emptyList(),
        private val throwError: Boolean = false
    ) : Source {
        override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo = manga
        override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> = emptyList()
        override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> = emptyList()

        override suspend fun getRecommendations(manga: MangaInfo, commands: List<Command<*>>): List<RecommendationInfo> {
            if (throwError) {
                throw RuntimeException("Network error fetching recommendations")
            }
            return recommendationsToReturn
        }
    }

    @Test
    fun `returns empty list when catalog is null`() = runTest {
        val useCase = GetSourceRecommendations()
        val book = Book(id = 1L, title = "Test Book", key = "test-key", sourceId = 100L)

        var errorReceived: UiText? = null
        var recommendationsReceived: List<Recommendation>? = null

        useCase(
            book = book,
            catalog = null,
            onError = { errorReceived = it },
            onSuccess = { recommendationsReceived = it }
        )

        assertNotNull(errorReceived)
    }

    @Test
    fun `returns mapped recommendations on success`() = runTest {
        val expectedRecs = listOf(
            RecommendationInfo(
                key = "rec-1",
                title = "Rec 1",
                cover = "https://example.com/1.jpg",
                genres = listOf("Action"),
                sourceId = 100L,
                sourceName = "FakeSource"
            ),
            RecommendationInfo(
                key = "rec-2",
                title = "Rec 2",
                sourceId = 100L,
                sourceName = "FakeSource"
            )
        )
        val source = FakeRecommendationSource(recommendationsToReturn = expectedRecs)
        val catalog = CatalogBundled(source = source)
        val book = Book(id = 1L, title = "Test Book", key = "test-key", sourceId = 100L)

        val useCase = GetSourceRecommendations()
        var result: List<Recommendation>? = null

        useCase(
            book = book,
            catalog = catalog,
            onError = {},
            onSuccess = { result = it }
        )

        val nonNullResult = assertNotNull(result)
        assertEquals(2, nonNullResult.size)
        assertEquals("rec-1", nonNullResult[0].key)
        assertEquals("Rec 1", nonNullResult[0].title)
        assertEquals("rec-2", nonNullResult[1].key)
    }


    @Test
    fun `calls onError on source exception`() = runTest {
        val source = FakeRecommendationSource(throwError = true)
        val catalog = CatalogBundled(source = source)
        val book = Book(id = 1L, title = "Test Book", key = "test-key", sourceId = 100L)

        val useCase = GetSourceRecommendations()
        var errorReceived: UiText? = null

        useCase(
            book = book,
            catalog = catalog,
            onError = { errorReceived = it },
            onSuccess = {}
        )

        assertNotNull(errorReceived)
    }
}
