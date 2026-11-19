package ireader.domain.usecases.remote

import ireader.core.source.Source
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Command
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.Page
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogBundled
import ireader.domain.models.entities.Chapter
import ireader.i18n.UiText
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetRemoteChaptersOrderingTest {

    private class FakeSource(
        private val fetched: List<ChapterInfo>,
    ) : Source {
        override val id: Long = 100L
        override val name: String = "FakeSource"
        override val lang: String = "en"

        override suspend fun getMangaDetails(
            manga: MangaInfo,
            commands: List<Command<*>>,
        ): MangaInfo = manga

        override suspend fun getChapterList(
            manga: MangaInfo,
            commands: List<Command<*>>,
        ): List<ChapterInfo> = fetched

        override suspend fun getPageList(
            chapter: ChapterInfo,
            commands: List<Command<*>>,
        ): List<Page> = emptyList()
    }

    private val book = Book(id = 7L, key = "book", sourceId = 100L, title = "Book")

    private suspend fun fetch(
        fetched: List<ChapterInfo>,
        oldChapters: List<Chapter>,
    ): List<Chapter> {
        var saved: List<Chapter> = emptyList()
        var error: UiText? = null
        GetRemoteChapters().invoke(
            book = book,
            catalog = CatalogBundled(FakeSource(fetched)),
            oldChapters = oldChapters,
            onSuccess = { saved = it },
            onError = { error = it },
        )
        assertTrue(error == null, "Fetch failed: $error")
        return saved
    }

    @Test
    fun `fetched chapters carry their source position as source order`() = runTest {
        val saved = fetch(
            fetched = listOf(
                ChapterInfo(key = "c1", name = "Chapter 1", number = 1f),
                ChapterInfo(key = "c2", name = "Chapter 2", number = 2f),
                ChapterInfo(key = "c3", name = "Chapter 3", number = 3f),
            ),
            oldChapters = emptyList(),
        )

        assertEquals(listOf(0L, 1L, 2L), saved.map { it.sourceOrder })
    }

    @Test
    fun `chapter with a changed key updates existing chapter instead of duplicating it`() = runTest {
        val existing = Chapter(
            id = 12L,
            bookId = 7L,
            key = "old-c2",
            name = "Chapter 2",
            number = 2f,
            sourceOrder = 1L,
            read = true,
        )

        val saved = fetch(
            fetched = listOf(
                ChapterInfo(key = "c1", name = "Chapter 1", number = 1f),
                ChapterInfo(key = "new-c2", name = "Chapter 2", number = 2f),
                ChapterInfo(key = "c3", name = "Chapter 3", number = 3f),
            ),
            oldChapters = listOf(existing),
        )

        assertEquals(3, saved.size, "Changed key must not create a duplicate chapter")
        val updated = saved.single { it.number == 2f }
        assertEquals(12L, updated.id, "Existing chapter row must be reused")
        assertEquals("new-c2", updated.key)
        assertTrue(updated.read, "Reading progress must survive the update")
        assertEquals(1L, updated.sourceOrder)
    }

    @Test
    fun `duplicate chapters returned by a source are collapsed`() = runTest {
        val saved = fetch(
            fetched = listOf(
                ChapterInfo(key = "c1", name = "Chapter 1", number = 1f),
                ChapterInfo(key = "c2", name = "Chapter 2", number = 2f),
                ChapterInfo(key = "c2-alt", name = "Chapter 2", number = 2f),
                ChapterInfo(key = "c3", name = "Chapter 3", number = 3f),
            ),
            oldChapters = emptyList(),
        )

        assertEquals(3, saved.size)
        assertEquals(listOf("c1", "c2", "c3"), saved.map { it.key })
    }

    @Test
    fun `downloaded chapter no longer returned by source still gets a unique source order`() = runTest {
        val downloaded = Chapter(
            id = 99L,
            bookId = 7L,
            key = "old-c2",
            name = "Chapter 2",
            number = 2f,
            sourceOrder = 0L,
            read = true,
        )

        val saved = fetch(
            fetched = listOf(
                ChapterInfo(key = "c1", name = "Chapter 1", number = 1f),
                ChapterInfo(key = "c3", name = "Chapter 3", number = 3f),
            ),
            oldChapters = listOf(downloaded),
        )

        assertEquals(3, saved.size)
        assertEquals(listOf(0L, 1L, 2L), saved.map { it.sourceOrder })
        assertEquals(99L, saved.single { it.key == "old-c2" }.id)
        assertTrue(saved.single { it.key == "old-c2" }.read)
    }
}
