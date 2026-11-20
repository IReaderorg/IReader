package ireader.domain.usecases.backup

import ireader.domain.preferences.prefs.UiPreferences

/**
 * User-configured limits for pruning old backups. A value of `0` (or less)
 * disables that rule — it never means "delete everything".
 */
data class BackupRetentionSettings(
    val maxFiles: Int = 0,
    val maxAgeDays: Int = 0,
    val maxSizeBytes: Long = 0L,
)

/** Minimal description of a backup file needed to apply retention rules. */
data class BackupFileInfo(
    val name: String,
    val lastModifiedMillis: Long,
    val sizeBytes: Long,
)

/**
 * Pure retention rules shared by automatic and manual backups on every platform.
 *
 * Callers must pass files sorted newest first. The function only decides *which*
 * files to delete; the platform layer performs the actual I/O.
 */
object BackupRetentionPolicy {

    private const val MILLIS_PER_DAY = 86_400_000L

    /**
     * Returns the files that violate at least one configured limit. Each file is
     * returned at most once, in the same (newest-first) order as [newestFirst].
     */
    fun <T> filesToDelete(
        newestFirst: List<T>,
        settings: BackupRetentionSettings,
        infoOf: (T) -> BackupFileInfo,
        nowMillis: Long,
    ): List<T> {
        if (newestFirst.isEmpty()) return emptyList()

        val doomed = LinkedHashSet<Int>()

        // 1. Age: delete files older than the configured number of days.
        if (settings.maxAgeDays > 0) {
            val cutoff = nowMillis - settings.maxAgeDays * MILLIS_PER_DAY
            newestFirst.forEachIndexed { index, file ->
                if (infoOf(file).lastModifiedMillis < cutoff) doomed += index
            }
        }

        // 2. Count: keep only the newest N files.
        if (settings.maxFiles > 0) {
            var kept = 0
            newestFirst.forEachIndexed { index, _ ->
                if (index in doomed) return@forEachIndexed
                if (kept >= settings.maxFiles) doomed += index else kept++
            }
        }

        // 3. Size: while the directory is over the limit, drop the oldest survivor.
        if (settings.maxSizeBytes > 0) {
            var total = newestFirst.indices
                .filterNot(doomed::contains)
                .sumOf { infoOf(newestFirst[it]).sizeBytes }
            if (total > settings.maxSizeBytes) {
                for (index in newestFirst.indices.reversed()) {
                    if (index in doomed) continue
                    doomed += index
                    total -= infoOf(newestFirst[index]).sizeBytes
                    if (total <= settings.maxSizeBytes) break
                }
            }
        }

        return newestFirst.indices.filter(doomed::contains).map(newestFirst::get)
    }

    fun totalSize(files: List<BackupFileInfo>): Long = files.sumOf { it.sizeBytes }

    fun fromPreferences(preferences: UiPreferences): BackupRetentionSettings = BackupRetentionSettings(
        maxFiles = preferences.maxAutomaticBackupFiles().get(),
        maxAgeDays = preferences.maxAutomaticBackupAgeDays().get(),
        maxSizeBytes = preferences.maxAutomaticBackupSizeMb().get().toLong() * 1024L * 1024L,
    )
}
