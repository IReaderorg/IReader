package ireader.domain.usecases.backup

import ireader.domain.models.prefs.PreferenceValues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AutomaticBackupPolicyTest {
    private val dayMillis = 24L * 60L * 60L * 1000L

    @Test
    fun `off has no interval and is never due`() {
        assertNull(AutomaticBackupPolicy.interval(PreferenceValues.AutomaticBackup.Off))
        assertFalse(
            AutomaticBackupPolicy.isDue(
                frequency = PreferenceValues.AutomaticBackup.Off,
                lastBackupTimeMillis = 0L,
                nowMillis = Long.MAX_VALUE
            )
        )
    }

    @Test
    fun `daily interval is one day`() {
        assertEquals(
            dayMillis,
            AutomaticBackupPolicy.interval(PreferenceValues.AutomaticBackup.Daily)?.inWholeMilliseconds
        )
    }

    @Test
    fun `backup is due when it has never run`() {
        assertTrue(
            AutomaticBackupPolicy.isDue(
                frequency = PreferenceValues.AutomaticBackup.Daily,
                lastBackupTimeMillis = 0L,
                nowMillis = dayMillis * 10
            )
        )
    }

    @Test
    fun `backup is not due before the interval elapses`() {
        assertFalse(
            AutomaticBackupPolicy.isDue(
                frequency = PreferenceValues.AutomaticBackup.Daily,
                lastBackupTimeMillis = dayMillis * 10,
                nowMillis = dayMillis * 10 + dayMillis - 1
            )
        )
    }

    @Test
    fun `backup is due exactly at the interval`() {
        assertTrue(
            AutomaticBackupPolicy.isDue(
                frequency = PreferenceValues.AutomaticBackup.Daily,
                lastBackupTimeMillis = dayMillis * 10,
                nowMillis = dayMillis * 11
            )
        )
    }

    @Test
    fun `automatic backup uses the full backup options including chapter content`() {
        val options = AutomaticBackupPolicy.backupOptions()
        assertTrue(options.includeBooks)
        assertTrue(options.includeChapters)
        assertTrue(options.includeChapterContent)
        assertTrue(options.includeCategories)
        assertTrue(options.includeHistory)
        assertTrue(options.includeThemes)
        assertTrue(options.includeSettings)
    }

    @Test
    fun `backup file name is stable and includes the timestamp`() {
        assertEquals(
            "IReader_auto_backup_1234.gz",
            AutomaticBackupPolicy.fileName(1234L)
        )
    }
}
