package ireader.domain.usecases.backup

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSFileSize
import platform.Foundation.NSFileType
import platform.Foundation.NSFileTypeDirectory
import platform.Foundation.NSNumber
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970

/** iOS retention storage backed by NSFileManager. */
@OptIn(ExperimentalForeignApi::class)
class IosBackupRetentionStorage : BackupRetentionStorage {

    override suspend fun rotateAutomaticBackups(settings: BackupRetentionSettings): Int {
        val directory = automaticBackupDirectory() ?: return 0
        return prune(directory, settings)
    }

    override suspend fun automaticDirectorySize(): Long =
        filesIn(automaticBackupDirectory() ?: return 0L).sumOf { it.second.sizeBytes }

    private fun automaticBackupDirectory(): String? {
        val documents = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String ?: return null
        return "$documents/backups"
    }

    private fun filesIn(directory: String): List<Pair<String, BackupFileInfo>> {
        val fileManager = NSFileManager.defaultManager
        val contents = fileManager.contentsOfDirectoryAtPath(directory, error = null)
            ?: return emptyList()
        return contents.mapNotNull { item ->
            val name = item as? String ?: return@mapNotNull null
            if (!name.startsWith(AutomaticBackupPolicy.fileNamePrefix()) && !name.endsWith(".ireader")) {
                return@mapNotNull null
            }
            val path = "$directory/$name"
            val attributes = fileManager.attributesOfItemAtPath(path, error = null)
            if (attributes?.get(NSFileType) as? String == NSFileTypeDirectory) {
                return@mapNotNull null
            }
            val size = (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
            val modified = (attributes?.get(NSFileModificationDate) as? NSDate)
                ?.timeIntervalSince1970?.toLong()?.times(1000) ?: 0L
            path to BackupFileInfo(
                name = name,
                lastModifiedMillis = modified,
                sizeBytes = size
            )
        }
    }

    private fun prune(directory: String, settings: BackupRetentionSettings): Int {
        val newestFirst = filesIn(directory)
            .sortedByDescending { it.second.lastModifiedMillis }
        return BackupRetentionPolicy.filesToDelete(
            newestFirst = newestFirst,
            settings = settings,
            infoOf = { it.second },
            nowMillis = NSDate().timeIntervalSince1970.toLong() * 1000
        ).count { (path, _) ->
            runCatching { NSFileManager.defaultManager.removeItemAtPath(path, error = null) }.isSuccess
        }
    }
}
