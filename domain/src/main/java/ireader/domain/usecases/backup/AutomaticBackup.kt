package ireader.domain.usecases.backup

import android.content.Context
import android.os.Environment
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.createBinaryFile
import ireader.core.log.Log
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.utils.extensions.convertLongToTime
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.koin.core.annotation.Single
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@Single
class AutomaticBackup(
    val context: Context,
    val createBackup: CreateBackup,
    private val uiPreferences: UiPreferences,
    val simpleStorage: GetSimpleStorage
) {
    suspend fun initialize() {
        create(false)
    }

    private suspend fun create(force: Boolean = false) {
        val lastCheckPref = uiPreferences.lastBackUpTime()
        val maxFiles = uiPreferences.maxAutomaticBackupFiles().get()
        val automaticBackupPref = uiPreferences.automaticBackupTime().get()
        val backupEveryXTime = automaticBackupTime(automaticBackupPref) ?: return
        val lastCheck = Instant.fromEpochMilliseconds(lastCheckPref.get())
        val now = Clock.System.now()
        if (force || now - lastCheck > backupEveryXTime) {
            try {
                simpleStorage.checkPermission()
                val root = Environment.getExternalStorageDirectory()
                val dir = File(root, "IReader/Backups/Automatic")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val name =
                    "IReader_${convertLongToTime(Calendar.getInstance().timeInMillis)}.gz"
                val file = DocumentFileCompat.fromFile(
                    context,
                    dir,
                    requiresWriteAccess = true,
                    considerRawFile = true
                )
                val allFiles = file?.listFiles()
                if ((allFiles?.size ?: 0) > maxFiles) {
                    allFiles?.map { it.delete() }
                }
                val backupFile = file!!.createBinaryFile(context, name, mode = CreateMode.CREATE_NEW)
                createBackup.saveTo(backupFile!!.uri, context, onError = {}, onSuccess = {})
                lastCheckPref.set(now.toEpochMilliseconds())
            } catch (e: Exception) {
                Log.error(e, "AutomaticBackup")
                //uiPreferences.automaticBackupTime().set(PreferenceValues.AutomaticBackup.Off)
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