package ireader.domain.usecases.backup.v2

import ireader.core.source.model.encode
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.History
import ireader.domain.models.entities.Track
import ireader.domain.models.entities.TrackStatus
import ireader.domain.models.theme.ReaderTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackupPayloadTest {

    private val serializer = BackupSerializer()

    @Test
    fun testFullBackupPayloadSerializationAndDeserialization() {
        val books = listOf(
            BookSnapshot(
                sourceId = 1L,
                key = "book-1",
                title = "Solo Leveling",
                author = "Chugong",
                description = "Hunter story",
                chapters = listOf(
                    ChapterSnapshot(
                        key = "ch-1",
                        name = "Chapter 1",
                        read = true,
                        bookmark = true,
                        lastPageRead = 15L,
                        content = listOf(ireader.core.source.model.Text("Once upon a time in a dungeon...")).encode()
                    )
                ),
                categoryOrders = listOf(1L)
            )
        )

        val categories = listOf(
            CategorySnapshot(name = "Action", order = 1L, flags = 0L)
        )

        val histories = listOf(
            HistorySnapshot(
                bookKey = "book-1",
                bookSourceId = 1L,
                chapterKey = "ch-1",
                lastRead = 123456789L,
                timeRead = 5000L,
                progress = 0.85f
            )
        )

        val tracks = listOf(
            TrackSnapshot(
                bookKey = "book-1",
                bookSourceId = 1L,
                siteId = 1,
                entryId = 100L,
                mediaId = 200L,
                title = "Solo Leveling",
                lastRead = 1f,
                totalChapters = 200,
                score = 9.5f,
                status = 1
            )
        )

        val themes = listOf(
            ReaderThemeSnapshot(
                id = 1L,
                backgroundColor = 0x000000,
                onTextColor = 0xFFFFFF
            )
        )

        val settings = listOf(
            SettingSnapshot(key = "font_size", value = "18"),
            SettingSnapshot(key = "reading_mode", value = "Continuous")
        )

        val metadata = BackupMetadata(
            appVersion = "1.0.0",
            deviceName = "Test Device",
            createdAt = 1234567890L,
            bookCount = 1,
            chapterCount = 1,
            historyCount = 1,
            categoryCount = 1,
            trackCount = 1,
            themeCount = 1
        )

        val payload = BackupPayload(
            version = BackupPayload.CURRENT_VERSION,
            books = books,
            categories = categories,
            histories = histories,
            tracks = tracks,
            themes = themes,
            settings = settings,
            metadata = metadata
        )

        // Serialize + Compress
        val bytes = serializer.serialize(payload)
        assertTrue(bytes.isNotEmpty())

        // Decompress + Deserialize
        val deserialized = serializer.deserialize(bytes)

        assertEquals(BackupPayload.CURRENT_VERSION, deserialized.version)
        assertEquals(1, deserialized.books.size)
        assertEquals("Solo Leveling", deserialized.books[0].title)
        assertEquals(1, deserialized.books[0].chapters.size)
        assertEquals("Chapter 1", deserialized.books[0].chapters[0].name)
        assertTrue(deserialized.books[0].chapters[0].read)
        assertTrue(deserialized.books[0].chapters[0].bookmark)
        val restoredChapter = deserialized.books[0].chapters[0].toChapter(1L)
        assertEquals(1, restoredChapter.content.size)
        assertEquals("Once upon a time in a dungeon...", (restoredChapter.content[0] as ireader.core.source.model.Text).text)

        assertEquals(1, deserialized.categories.size)
        assertEquals("Action", deserialized.categories[0].name)

        assertEquals(1, deserialized.histories.size)
        assertEquals("book-1", deserialized.histories[0].bookKey)
        assertEquals("ch-1", deserialized.histories[0].chapterKey)
        assertEquals(123456789L, deserialized.histories[0].lastRead)

        assertEquals(1, deserialized.tracks.size)
        assertEquals("Solo Leveling", deserialized.tracks[0].title)
        assertEquals(9.5f, deserialized.tracks[0].score)

        assertEquals(1, deserialized.themes.size)
        assertEquals(0x000000, deserialized.themes[0].backgroundColor)
        assertEquals(0xFFFFFF, deserialized.themes[0].onTextColor)

        assertEquals(2, deserialized.settings.size)
        assertEquals("18", deserialized.settings.find { it.key == "font_size" }?.value)
    }

    @Test
    fun testDefaultBackupOptionsAreAllEnabled() {
        val options = BackupOptions()
        assertTrue(options.includeBooks)
        assertTrue(options.includeChapters)
        assertTrue(options.includeChapterContent)
        assertTrue(options.includeCategories)
        assertTrue(options.includeHistory)
        assertTrue(options.includeTracks)
        assertTrue(options.includeThemes)
        assertTrue(options.includeSettings)
    }

    @Test
    fun testDefaultRestoreOptionsAreAllEnabled() {
        val options = RestoreOptions()
        assertTrue(options.restoreBooks)
        assertTrue(options.restoreChapters)
        assertTrue(options.restoreCategories)
        assertTrue(options.restoreHistory)
        assertTrue(options.restoreTracks)
        assertTrue(options.restoreThemes)
        assertTrue(options.restoreSettings)
    }
}
