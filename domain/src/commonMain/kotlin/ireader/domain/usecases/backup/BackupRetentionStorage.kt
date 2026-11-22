package ireader.domain.usecases.backup

/**
 * Platform storage operations needed to apply [BackupRetentionPolicy] and to
 * report how much space the automatic backup directory currently uses.
 */
interface BackupRetentionStorage {
    /** Prunes automatic backups and returns the number of files deleted. */
    suspend fun rotateAutomaticBackups(settings: BackupRetentionSettings): Int

    /** Current total size, in bytes, of the automatic backup directory. */
    suspend fun automaticDirectorySize(): Long
}
