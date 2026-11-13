package ireader.domain.usecases.backup

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import ireader.core.db.Transactions
import ireader.core.source.model.Text
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.backup.backup.BackupProto
import ireader.domain.usecases.backup.backup.BookProto
import ireader.domain.usecases.backup.backup.ChapterProto
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalSerializationApi::class)
class RestoreBackupMergeTest {

    @Test
    fun `newer backup updates chapters in place instead of deleting them`() = runTest {
        val transactions = mockk<Transactions>()
        coEvery { transactions.run(any<suspend () -> Any>()) } coAnswers { firstArg<suspend () -> Any>()() }
        val bookRepository = mockk<ireader.domain.data.repository.BookRepository>(relaxed = true)
        val chapterRepository = mockk<ireader.domain.data.repository.ChapterRepository>(relaxed = true)
        val categoryRepository = mockk<ireader.domain.data.repository.CategoryRepository>(relaxed = true)

        val downloaded = listOf(Text("downloaded chapter text"))
        coEvery { bookRepository.find("book", 1L) } returns
            Book(id = 10L, sourceId = 1L, key = "book", title = "Book", initialized = true, favorite = true, lastUpdate = 1000L)
        coEvery { chapterRepository.findChaptersByBookId(10L) } returns listOf(
            Chapter(id = 100L, bookId = 10L, key = "ch1", name = "Chapter 1", content = downloaded)
        )
        coEvery { categoryRepository.findAll() } returns emptyList()

        val backup = BackupProto(
            library = listOf(
                BookProto(
                    sourceId = 1L, key = "book", title = "Book", initialized = true, lastUpdate = 2000L,
                    chapters = listOf(
                        ChapterProto(key = "ch1", name = "Chapter 1", read = true),
                        ChapterProto(key = "ch2", name = "Chapter 2")
                    )
                )
            )
        )

        val result = RestoreBackup(
            bookRepository = bookRepository,
            categoryRepository = categoryRepository,
            chapterRepository = chapterRepository,
            mangaCategoryRepository = mockk(relaxed = true),
            libraryPreferences = mockk(relaxed = true),
            transactions = transactions,
            fileSaver = mockk(relaxed = true)
        ).restoreFromBytes(ProtoBuf.encodeToByteArray(backup))

        assertTrue(result is RestoreBackup.Result.Success)
        // Deleting cascaded away history, downloads and translations of the book's chapters
        coVerify(exactly = 0) { chapterRepository.deleteChapters(any()) }
        val saved = slot<List<Chapter>>()
        coVerify { chapterRepository.insertChapters(capture(saved)) }
        val ch1 = saved.captured.single { it.key == "ch1" }
        assertEquals(100L, ch1.id)
        assertEquals(downloaded, ch1.content)
        assertTrue(ch1.read)
        assertEquals(0L, saved.captured.single { it.key == "ch2" }.id)
    }
}
