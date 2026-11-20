package ireader.data

import io.mockk.mockk
import ireader.data.core.JvmDatabaseHandler
import ireader.data.core.createDatabase
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.AppPreferences
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChapterBatchInsertAtomicityTest {

    @Test
    fun `a failed batch does not leave earlier chapters committed`() = runTest {
        val driver = createTestDriver()
        ir.kazemcodes.infinityreader.Database.Schema.create(driver)
        val database = createDatabase(driver)
        val handler = JvmDatabaseHandler(
            db = database,
            driver = driver,
            preferencesHelper = mockk<AppPreferences>(relaxed = true),
        )

        database.bookQueries.upsert(
            id = null,
            source = 1L,
            url = "book",
            artist = "",
            author = "",
            description = "",
            genre = emptyList(),
            title = "Book",
            status = 0L,
            thumbnailUrl = "",
            customCover = "",
            favorite = false,
            lastUpdate = 0L,
            nextUpdate = 0L,
            initialized = true,
            viewerFlags = 0L,
            chapterFlags = 0L,
            coverLastModified = 0L,
            dateAdded = 0L,
            isPinned = false,
            pinnedOrder = 0L,
            isArchived = false,
        )

        val chapters = (1..201).map { index ->
            Chapter(
                bookId = 1L,
                key = "ch-$index",
                name = "Chapter $index",
                number = if (index == 201) Float.NaN else index.toFloat(),
            )
        }

        assertFailsWith<Exception> {
            ireader.data.chapter.ChapterRepositoryImpl(handler).insertChapters(chapters)
        }

        val savedChapters = database.chapterQueries
            .getChaptersByMangaIdLight(1L, ireader.data.chapter.chapterMapperLight)
            .executeAsList()
        assertEquals(0, savedChapters.size)
    }

    @Test
    fun `inserting a chapter with an id returns that id after an update`() = runTest {
        val driver = createTestDriver()
        ir.kazemcodes.infinityreader.Database.Schema.create(driver)
        val database = createDatabase(driver)
        val handler = JvmDatabaseHandler(
            db = database,
            driver = driver,
            preferencesHelper = mockk<AppPreferences>(relaxed = true),
        )

        val repository = ireader.data.chapter.ChapterRepositoryImpl(handler)
        repository.insertChapter(
            Chapter(id = 42L, bookId = 1L, key = "ch-42", name = "Chapter 42", number = 42f)
        )
        repository.insertChapter(
            Chapter(bookId = 1L, key = "ch-43", name = "Chapter 43", number = 43f)
        )

        val result = repository.insertChapter(
            Chapter(id = 42L, bookId = 1L, key = "ch-42", name = "Updated", number = 42f)
        )

        assertEquals(42L, result)
    }
}
