package ireader.domain.usecases.backup

import android.content.Context
import android.net.Uri as AndroidUri
import androidx.documentfile.provider.DocumentFile
import ireader.domain.models.common.Uri
import ireader.domain.storage.SafStorageManager

/**
 * Storage for automatic backups.
 *
 * Uses the same SAF folder the app already needs for Extensions and js-plugins,
 * writing to `<selected folder>/Backups/Automatic`. This keeps backups in the
 * user's chosen IReader directory and needs no extra permissions.
 */
class AutoBackupStorage(
    private val context: Context,
    private val safStorageManager: SafStorageManager
) : BackupRetentionStorage {
    fun createBackupUri(fileName: String): Uri {
        val directory = safStorageManager.getAutomaticBackupsDirectory()
            ?: throw IllegalStateException("IReader storage folder is not available")
        val file = safStorageManager.createFile(directory, fileName, MIME_TYPE)
            ?: throw IllegalStateException("Could not create backup file Backups/Automatic/$fileName")
        return Uri(file.uri)
    }

    fun finishBackup(uri: Uri, success: Boolean) {
        if (success) return
        runCatching {
            DocumentFile.fromSingleUri(context, AndroidUri.parse(uri.toString()))?.delete()
        }
    }

    override suspend fun rotateAutomaticBackups(settings: BackupRetentionSettings): Int {
        val directory = safStorageManager.getAutomaticBackupsDirectory() ?: return 0
        return prune(directory, AutomaticBackupPolicy.fileNamePrefix(), settings)
    }

    override suspend fun automaticDirectorySize(): Long {
        val directory = safStorageManager.getAutomaticBackupsDirectory() ?: return 0L
        return directory.listFiles().sumOf { if (it.isFile) it.length() else 0L }
    }

    private fun prune(
        directory: DocumentFile,
        prefix: String,
        settings: BackupRetentionSettings
    ): Int {
        val newestFirst = directory.listFiles()
            .filter { it.isFile && it.name?.startsWith(prefix) == true }
            .map { file ->
                file to BackupFileInfo(
                    name = file.name.orEmpty(),
                    lastModifiedMillis = file.lastModified(),
                    sizeBytes = file.length()
                )
            }
            .sortedByDescending { it.second.lastModifiedMillis }

        return BackupRetentionPolicy.filesToDelete(
            newestFirst = newestFirst,
            settings = settings,
            infoOf = { it.second },
            nowMillis = System.currentTimeMillis()
        ).count { (file, _) -> runCatching { file.delete() }.getOrDefault(false) }
    }

    companion object {
        private const val MIME_TYPE = "application/gzip"
    }
}
