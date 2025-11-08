package ireader.domain.usecases.backup

import android.content.Context
import android.os.Environment
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.createBinaryFile
import ireader.core.log.Log
import ireader.domain.models.common.Uri
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.utils.extensions.convertLongToTime
import ireader.domain.utils.extensions.withUIContext
import ireader.domain.utils.toast
import ireader.i18n.R
import kotlinx.datetime.Instant
import java.io.File
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
    suspend fun initialize() {
        create(false)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun create(force: Boolean = false) {
        val lastCheckPref = uiPreferences.lastBackUpTime()
        val maxFiles = uiPreferences.maxAutomaticBackupFiles().get()
        val automaticBackupPref = uiPreferences.automaticBackupTime().get()
        val backupEveryXTime = automaticBackupTime(automaticBackupPref) ?: return
        val lastCheck = Instant.fromEpochMilliseconds(lastCheckPref.get())
        val now = kotlin.time.Clock.System.now()
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
                createBackup.saveTo(Uri(backupFile!!.uri), onError = {}, onSuccess = {}, currentEvent = {})
                lastCheckPref.set(now.toEpochMilliseconds())
            } catch (e: Exception) {
                Log.error(e, "AutomaticBackup")
                uiPreferences.automaticBackupTime().set(PreferenceValues.AutomaticBackup.Off)
                withUIContext {
                    context.toast(R.string.permission_not_given)
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