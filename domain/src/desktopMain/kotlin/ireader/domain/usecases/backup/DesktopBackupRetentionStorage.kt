package ireader.domain.usecases.backup

import ireader.domain.usecases.files.GetSimpleStorage
import okio.FileSystem
import okio.Path

/** Desktop retention storage backed by Okio's filesystem. */
class DesktopBackupRetentionStorage(
    private val simpleStorage: GetSimpleStorage,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
) : BackupRetentionStorage {

    override suspend fun rotateAutomaticBackups(settings: BackupRetentionSettings): Int =
        prune(simpleStorage.automaticBackupDirectory, AutomaticBackupPolicy.fileNamePrefix(), settings)

    override suspend fun automaticDirectorySize(): Long = directorySize(
        simpleStorage.automaticBackupDirectory,
        AutomaticBackupPolicy.fileNamePrefix()
    )

    private fun prune(directory: Path, prefix: String, settings: BackupRetentionSettings): Int {
        if (!fileSystem.exists(directory)) return 0
        val newestFirst = fileSystem.list(directory)
            .filter { it.name.startsWith(prefix) }
            .mapNotNull { path ->
                fileSystem.metadataOrNull(path)?.let { meta ->
                    path to BackupFileInfo(
                        name = path.name,
                        lastModifiedMillis = meta.lastModifiedAtMillis ?: 0L,
                        sizeBytes = meta.size ?: 0L
                    )
                }
            }
            .sortedByDescending { it.second.lastModifiedMillis }

        return BackupRetentionPolicy.filesToDelete(
            newestFirst = newestFirst,
            settings = settings,
            infoOf = { it.second },
            nowMillis = System.currentTimeMillis()
        ).count { (path, _) -> runCatching { fileSystem.delete(path) }.isSuccess }
    }

    private fun directorySize(directory: Path, prefix: String): Long {
        if (!fileSystem.exists(directory)) return 0L
        return fileSystem.list(directory)
            .filter { it.name.startsWith(prefix) }
            .sumOf { fileSystem.metadataOrNull(it)?.size ?: 0L }
    }
}
