package ireader.domain.usecases.backup.v2

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import ireader.core.db.Transactions
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.History
import ireader.domain.usecases.file.FileSaver
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalSerializationApi::class)
class BackupHistoryRestoreTest {

    private val serializer = BackupSerializer()

    @Test
    fun `streaming restore applies history after chapter content`() = runTest {
        var contentRestored = false
        val payload = BackupPayload(
            books = listOf(
                BookSnapshot(
                    sourceId = 1L,
                    key = "book-1",
                    title = "Book",
                    chapters = listOf(ChapterSnapshot(key = "ch-1", name = "Chapter 1")),
                )
            ),
            histories = listOf(
                HistorySnapshot(
                    bookKey = "book-1",
                    bookSourceId = 1L,
                    chapterKey = "ch-1",
                    lastRead = 1000L,
                    timeRead = 10L,
                    progress = 0.5f,
                )
            ),
        )
        val content = BookContentSnapshot(
            bookKey = "book-1",
            bookSourceId = 1L,
            chapters = listOf(ChapterSnapshot(key = "ch-1", name = "Chapter 1", content = "text")),
        )

        val historyRepository = mockk<HistoryRepository>(relaxed = true)
        coEvery { historyRepository.insertHistory(any()) } answers {
            assertTrue(
                contentRestored,
                "History must be restored after chapter content (chapter IDs change on re-insert)",
            )
            Unit
        }

        val chapterRepository = mockk<ChapterRepository>(relaxed = true)
        coEvery { chapterRepository.findChaptersByBookId(any()) } returns listOf(
            Chapter(id = 99L, bookId = 42L, key = "ch-1", name = "Chapter 1")
        )
        coEvery { chapterRepository.insertChapters(any()) } returns listOf(1L)

        val bookRepository = mockk<BookRepository>(relaxed = true)
        coEvery { bookRepository.find("book-1", 1L) } returns null
        coEvery { bookRepository.upsert(any()) } returns 42L

        val streamer = mockk<BackupArchiveStreamer>(relaxed = true)
        coEvery { streamer.isZipArchive(any()) } returns true
        coEvery { streamer.extractArchive(any(), any(), any()) } coAnswers {
            val onMetadata = secondArg<suspend (ByteArray) -> Unit>()
            val onBookContent = thirdArg<suspend (String, ByteArray) -> Unit>()
            onMetadata(serializer.serialize(payload))
            onBookContent(
                "chapters/1_0.pb",
                ProtoBuf.encodeToByteArray(BookContentSnapshot.serializer(), content),
            )
            contentRestored = true
        }

        val transactions = mockk<Transactions>()
        coEvery { transactions.run(any<suspend () -> Any>()) } coAnswers { firstArg<suspend () -> Any>()() }

        val orchestrator = BackupOrchestrator(
            serializer = serializer,
            legacyMigrator = mockk(relaxed = true),
            fileSaver = mockk(relaxed = true),
            libraryRepository = mockk(relaxed = true),
            bookRepository = bookRepository,
            chapterRepository = chapterRepository,
            categoryRepository = mockk(relaxed = true),
            bookCategoryRepository = mockk(relaxed = true),
            historyRepository = historyRepository,
            trackingRepository = mockk(relaxed = true),
            readerThemeRepository = mockk(relaxed = true),
            settingsBackup = mockk(relaxed = true),
            transactions = transactions,
            archiveStreamer = streamer,
        )

        val result = orchestrator.restoreBackup(mockk<Uri>())

        assertTrue(result.isSuccess, "restore failed: ${result.exceptionOrNull()}")
        assertEquals(1, result.getOrThrow().historyRestored)
    }

    @Test
    fun `monolithic restore finds history book when books are not restored`() = runTest {
        val payload = BackupPayload(
            books = listOf(
                BookSnapshot(sourceId = 1L, key = "book-1", title = "Book")
            ),
            histories = listOf(
                HistorySnapshot(
                    bookKey = "book-1",
                    bookSourceId = 1L,
                    chapterKey = "ch-2",
                    lastRead = 1000L,
                    timeRead = 10L,
                    progress = 0.25f,
                )
            ),
        )
        val inserted = slot<History>()

        val historyRepository = mockk<HistoryRepository>(relaxed = true)
        coEvery { historyRepository.insertHistory(capture(inserted)) } returns Unit

        val chapterRepository = mockk<ChapterRepository>(relaxed = true)
        coEvery { chapterRepository.findChaptersByBookId(42L) } returns listOf(
            Chapter(id = 99L, bookId = 42L, key = "ch-2", name = "Chapter 2")
        )

        val bookRepository = mockk<BookRepository>(relaxed = true)
        coEvery { bookRepository.find("book-1", 1L) } returns Book(
            id = 42L,
            sourceId = 1L,
            key = "book-1",
            title = "Book",
        )

        val fileSaver = mockk<FileSaver>()
        every { fileSaver.read(any()) } returns serializer.serialize(payload)

        val streamer = mockk<BackupArchiveStreamer>(relaxed = true)
        coEvery { streamer.isZipArchive(any()) } returns false

        val transactions = mockk<Transactions>()
        coEvery { transactions.run(any<suspend () -> Any>()) } coAnswers { firstArg<suspend () -> Any>()() }

        val orchestrator = BackupOrchestrator(
            serializer = serializer,
            legacyMigrator = mockk(relaxed = true),
            fileSaver = fileSaver,
            libraryRepository = mockk(relaxed = true),
            bookRepository = bookRepository,
            chapterRepository = chapterRepository,
            categoryRepository = mockk(relaxed = true),
            bookCategoryRepository = mockk(relaxed = true),
            historyRepository = historyRepository,
            trackingRepository = mockk(relaxed = true),
            readerThemeRepository = mockk(relaxed = true),
            settingsBackup = mockk(relaxed = true),
            transactions = transactions,
            archiveStreamer = streamer,
        )

        val result = orchestrator.restoreBackup(
            mockk<Uri>(),
            RestoreOptions(restoreBooks = false),
        )

        assertTrue(result.isSuccess, "restore failed: ${result.exceptionOrNull()}")
        assertEquals(1, result.getOrThrow().historyRestored)
        assertEquals(99L, inserted.captured.chapterId)
        assertEquals(1000L, inserted.captured.readAt)
    }
}
