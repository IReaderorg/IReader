package ireader.domain.usecases.backup.v2

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import ireader.core.db.Transactions
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.common.Uri
import ireader.domain.usecases.file.FileSaver
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
class BackupSettingsRestoreTest {

    private val serializer = BackupSerializer()

    @Test
    fun `restore delegates settings and reports the actual restored count`() = runTest {
        val settingsBackup = mockk<SettingsBackup>(relaxed = true) {
            every { restore(any()) } returns 4
        }
        val payload = BackupPayload(
            settings = listOf(
                SettingSnapshot("theme_mode", "Dark"),
                SettingSnapshot("font_size", "20"),
                SettingSnapshot("language", "ja"),
                SettingSnapshot("unknown_key", "ignored"),
            )
        )
        val uri = mockk<Uri>()
        val fileSaver = mockk<FileSaver>(relaxed = true) {
            every { read(uri) } returns serializer.serialize(payload)
        }

        val orchestrator = createOrchestrator(settingsBackup, fileSaver)
        val result = orchestrator.restoreBackup(
            uri = uri,
            options = RestoreOptions(
                restoreBooks = false,
                restoreCategories = false,
                restoreHistory = false,
                restoreTracks = false,
                restoreThemes = false,
                restoreSettings = true,
            ),
        )

        assertTrue(result.isSuccess, "restore failed: ${result.exceptionOrNull()}")
        assertEquals(4, result.getOrThrow().settingsRestored)
        verify { settingsBackup.restore(payload.settings) }
    }

    @Test
    fun `create backup writes the settings collected by the registry`() = runTest {
        val settings = listOf(
            SettingSnapshot("theme_mode", "Dark"),
            SettingSnapshot("auto_backup", "Weekly"),
            SettingSnapshot("language", "ja"),
        )
        val settingsBackup = mockk<SettingsBackup>(relaxed = true) {
            every { collect() } returns settings
        }
        val uri = mockk<Uri>()
        val written = slot<ByteArray>()
        val fileSaver = mockk<FileSaver>(relaxed = true) {
            every { save(uri, capture(written)) } returns Unit
            every { validate(uri) } returns true
        }

        val orchestrator = createOrchestrator(settingsBackup, fileSaver)
        val result = orchestrator.createBackup(uri)

        assertTrue(result.isSuccess, "backup failed: ${result.exceptionOrNull()}")
        assertEquals(settings, serializer.deserialize(written.captured).settings)
    }

    private fun createOrchestrator(
        settingsBackup: SettingsBackup,
        fileSaver: FileSaver,
    ): BackupOrchestrator {
        val transactions = mockk<Transactions>()
        coEvery { transactions.run(any<suspend () -> Any>()) } coAnswers { firstArg<suspend () -> Any>()() }
        val libraryRepository = mockk<LibraryRepository>(relaxed = true)
        coEvery { libraryRepository.findFavorites() } returns emptyList()

        return BackupOrchestrator(
            serializer = serializer,
            legacyMigrator = mockk(relaxed = true),
            fileSaver = fileSaver,
            libraryRepository = libraryRepository,
            bookRepository = mockk(relaxed = true),
            chapterRepository = mockk(relaxed = true),
            categoryRepository = mockk(relaxed = true),
            bookCategoryRepository = mockk(relaxed = true),
            historyRepository = mockk(relaxed = true),
            trackingRepository = mockk(relaxed = true),
            readerThemeRepository = mockk(relaxed = true),
            settingsBackup = settingsBackup,
            transactions = transactions,
            archiveStreamer = null,
        )
    }
}
