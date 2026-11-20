package ireader.data

import ireader.data.chapter.chapterMapperLight
import ireader.data.core.createDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

class ChapterUpsertByIdTest {

    @Test
    fun `updating an existing chapter key by id does not violate the primary key`() {
        val driver = createTestDriver()
        ir.kazemcodes.infinityreader.Database.Schema.create(driver)
        val database = createDatabase(driver)

        database.chapterQueries.upsert(
            id = 10L,
            bookId = 1L,
            key = "old-key",
            name = "Chapter 1",
            translator = "",
            read = false,
            bookmark = false,
            last_page_read = 0L,
            chapter_number = 1f,
            source_order = 0L,
            date_fetch = 0L,
            date_upload = 0L,
            content = emptyList(),
            type = 1L,
        )

        database.chapterQueries.upsertById(
            id = 10L,
            bookId = 1L,
            key = "new-key",
            name = "Chapter 1",
            translator = "",
            read = true,
            bookmark = false,
            last_page_read = 0L,
            chapter_number = 1f,
            source_order = 4L,
            date_fetch = 0L,
            date_upload = 0L,
            content = emptyList(),
            type = 1L,
        )

        val chapters = database.chapterQueries.getChaptersByMangaIdLight(1L, chapterMapperLight).executeAsList()
        assertEquals(1, chapters.size)
        assertEquals("new-key", chapters.single().key)
        assertEquals(4L, chapters.single().sourceOrder)
        assertEquals(true, chapters.single().read)
    }

    @Test
    fun `renaming a chapter to a url held by another chapter does not violate the unique index`() {
        val driver = createTestDriver()
        ir.kazemcodes.infinityreader.Database.Schema.create(driver)
        val database = createDatabase(driver)

        fun insert(id: Long, key: String) {
            database.chapterQueries.upsert(
                id = id,
                bookId = 1L,
                key = key,
                name = "Chapter $id",
                translator = "",
                read = false,
                bookmark = false,
                last_page_read = 0L,
                chapter_number = id.toFloat(),
                source_order = id,
                date_fetch = 0L,
                date_upload = 0L,
                content = emptyList(),
                type = 1L,
            )
        }

        insert(10L, "first-key")
        insert(11L, "second-key")

        database.chapterQueries.removeConflictingChapterUrls(1L, "second-key", 10L)
        database.chapterQueries.upsertById(
            id = 10L,
            bookId = 1L,
            key = "second-key",
            name = "Chapter 10",
            translator = "",
            read = false,
            bookmark = false,
            last_page_read = 0L,
            chapter_number = 10f,
            source_order = 5L,
            date_fetch = 0L,
            date_upload = 0L,
            content = emptyList(),
            type = 1L,
        )

        val chapters = database.chapterQueries.getChaptersByMangaIdLight(1L, chapterMapperLight).executeAsList()
        assertEquals(1, chapters.size)
        assertEquals(10L, chapters.single().id)
        assertEquals("second-key", chapters.single().key)
    }
}
