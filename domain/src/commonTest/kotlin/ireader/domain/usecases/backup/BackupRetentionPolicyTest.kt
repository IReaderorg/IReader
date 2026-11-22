package ireader.domain.usecases.backup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackupRetentionPolicyTest {

    private val now = 1_000_000L
    private fun day(ageDays: Int) = now - ageDays * 86_400_000L

    private data class TestFile(
        val name: String,
        val lastModifiedMillis: Long,
        val sizeBytes: Long,
    )

    private fun infoOf(file: TestFile) = BackupFileInfo(
        name = file.name,
        lastModifiedMillis = file.lastModifiedMillis,
        sizeBytes = file.sizeBytes,
    )

    @Test
    fun `unlimited settings keep every file`() {
        val files = listOf(
            TestFile("newest", now, 10),
            TestFile("oldest", day(100), 20),
        )
        assertEquals(
            emptyList<TestFile>(),
            BackupRetentionPolicy.filesToDelete(
                newestFirst = files,
                settings = BackupRetentionSettings(0, 0, 0),
                infoOf = ::infoOf,
                nowMillis = now,
            )
        )
    }

    @Test
    fun `count limit deletes files beyond the newest N`() {
        val files = listOf(
            TestFile("a", now - 3, 10),
            TestFile("b", now - 2, 10),
            TestFile("c", now - 1, 10),
            TestFile("d", now, 10),
        )
        assertEquals(
            listOf("c", "d"),
            BackupRetentionPolicy.filesToDelete(
                newestFirst = files,
                settings = BackupRetentionSettings(maxFiles = 2),
                infoOf = ::infoOf,
                nowMillis = now,
            ).map { it.name }
        )
    }

    @Test
    fun `age limit deletes files older than the configured days`() {
        val files = listOf(
            TestFile("today", day(0), 10),
            TestFile("two-days", day(2), 10),
            TestFile("ten-days", day(10), 10),
        )
        assertEquals(
            listOf("ten-days"),
            BackupRetentionPolicy.filesToDelete(
                newestFirst = files,
                settings = BackupRetentionSettings(maxAgeDays = 7),
                infoOf = ::infoOf,
                nowMillis = now,
            ).map { it.name }
        )
    }

    @Test
    fun `size limit deletes oldest files until directory fits`() {
        val files = listOf(
            TestFile("newest-60", now - 3, 60),
            TestFile("mid-30", now - 2, 30),
            TestFile("old-20", now - 1, 20),
        )
        // Keep newest-60 (60). To get under 70 we must drop mid-30 and old-20.
        assertEquals(
            listOf("mid-30", "old-20"),
            BackupRetentionPolicy.filesToDelete(
                newestFirst = files,
                settings = BackupRetentionSettings(maxSizeBytes = 70),
                infoOf = ::infoOf,
                nowMillis = now,
            ).map { it.name }
        )
    }

    @Test
    fun `size limit keeps as many newest files as fit`() {
        val files = listOf(
            TestFile("newest-40", now - 3, 40),
            TestFile("mid-30", now - 2, 30),
            TestFile("old-20", now - 1, 20),
        )
        // 40 + 30 = 70 fits; adding old-20 would exceed 70.
        assertEquals(
            listOf("old-20"),
            BackupRetentionPolicy.filesToDelete(
                newestFirst = files,
                settings = BackupRetentionSettings(maxSizeBytes = 70),
                infoOf = ::infoOf,
                nowMillis = now,
            ).map { it.name }
        )
    }

    @Test
    fun `combined rules delete a file once even if it breaks several limits`() {
        val files = listOf(
            TestFile("newest", day(0), 50),
            TestFile("old-big", day(10), 500),
        )
        val toDelete = BackupRetentionPolicy.filesToDelete(
            newestFirst = files,
            settings = BackupRetentionSettings(maxFiles = 1, maxAgeDays = 7, maxSizeBytes = 60),
            infoOf = ::infoOf,
            nowMillis = now,
        )
        assertEquals(listOf("old-big"), toDelete.map { it.name })
        assertEquals(1, toDelete.size)
    }

    @Test
    fun `totalSize sums file sizes`() {
        assertEquals(
            150L,
            BackupRetentionPolicy.totalSize(
                listOf(
                    BackupFileInfo("a", now, 50),
                    BackupFileInfo("b", now, 100),
                )
            )
        )
    }

    @Test
    fun `empty directory returns nothing to delete`() {
        assertTrue(
            BackupRetentionPolicy.filesToDelete(
                newestFirst = emptyList(),
                settings = BackupRetentionSettings(maxFiles = 1, maxAgeDays = 1, maxSizeBytes = 1),
                infoOf = ::infoOf,
                nowMillis = now,
            ).isEmpty()
        )
    }
}
