package ireader.domain.usecases.backup

import android.content.Context
import ireader.core.log.Log
import ireader.domain.models.common.Uri
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.utils.extensions.convertLongToTime
import ireader.domain.utils.extensions.withUIContext
import ireader.domain.utils.toast
import okio.FileSystem
import okio.Path.Companion.toPath
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime


class AutomaticBackup(
    val createBackup: CreateBackup,
    private val uiPreferences: UiPreferences,
    val simpleStorage: GetSimpleStorage,
    private val context: Context
) {
    private val fileSystem = FileSystem.SYSTEM
    
    suspend fun initialize() {
        create(false)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun create(force: Boolean = false) {
        val lastCheckPref = uiPreferences.lastBackUpTime()
        val maxFiles = uiPreferences.maxAutomaticBackupFiles().get()
        val automaticBackupPref = uiPreferences.automaticBackupTime().get()
        val backupEveryXTime = automaticBackupTime(automaticBackupPref) ?: return
        val lastCheck = kotlin.time.Instant.fromEpochMilliseconds(lastCheckPref.get())
        val now = kotlin.time.Clock.System.now()
        if (force || now - lastCheck > backupEveryXTime) {
            try {
                simpleStorage.checkPermission()
                val dir = simpleStorage.automaticBackupDirectory
                
                // Ensure directory exists using Okio
                fileSystem.createDirectories(dir)
                
                val name = "IReader_${convertLongToTime(Calendar.getInstance().timeInMillis)}.gz"
                
                // Use Okio to list files
                val allFiles = fileSystem.list(dir)
                
                // Clean up old backups if exceeding max
                if (allFiles.size > maxFiles) {
                    allFiles
                        .mapNotNull { path -> 
                            fileSystem.metadataOrNull(path)?.let { meta -> path to (meta.lastModifiedAtMillis ?: 0L) }
                        }
                        .sortedBy { it.second }
                        .take(allFiles.size - maxFiles)
                        .forEach { (path, _) -> fileSystem.delete(path) }
                }
                
                // Create new backup file path
                val backupPath = dir / name
                val backupUri = Uri(android.net.Uri.fromFile(backupPath.toFile()))
                
                createBackup.saveTo(backupUri, onError = {}, onSuccess = {}, currentEvent = {})
                lastCheckPref.set(now.toEpochMilliseconds())
            } catch (e: Exception) {
                Log.error(e, "AutomaticBackup")
                uiPreferences.automaticBackupTime().set(PreferenceValues.AutomaticBackup.Off)
                withUIContext {
                    context.toast("Permission not given")
                }
            }
        }
    }

    internal companion object {
        fun automaticBackupTime(time: PreferenceValues.AutomaticBackup): kotlin.time.Duration? {
            return when (time) {
                PreferenceValues.AutomaticBackup.Every6Hours -> 6.hours
                PreferenceValues.AutomaticBackup.Every12Hours -> 12.hours
                PreferenceValues.AutomaticBackup.Daily -> 1.days
                PreferenceValues.AutomaticBackup.Every2Days -> 2.days
                PreferenceValues.AutomaticBackup.Weekly -> 7.days
                PreferenceValues.AutomaticBackup.Off -> null
            }
        }
    }
}
