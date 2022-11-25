package ireader.domain.usecases.files

import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.DocumentFileCompat
import ireader.domain.preferences.prefs.UiPreferences
import org.koin.core.annotation.Single
import java.io.File

@Single
class GetSimpleStorage(
    private val context: Context,
    private val uiPreferences: UiPreferences
) {

    lateinit var storage: SimpleStorage
    lateinit var simpleStorageHelper: SimpleStorageHelper

    fun provideActivity(activity: ComponentActivity, savedState: Bundle?) {
        storage = SimpleStorage(activity, savedState)
        simpleStorageHelper = SimpleStorageHelper(activity, savedState)
    }

    val mainIReaderDir: File = File(Environment.getExternalStorageDirectory(), "IReader/")


    fun ireaderDirectory(dirName: String): File =
        File(Environment.getExternalStorageDirectory(), "IReader/${dirName}/")

    fun extensionDirectory(): File =
        File(Environment.getExternalStorageDirectory(), "IReader/Extensions/")

    val backupDirectory: File =
        File(Environment.getExternalStorageDirectory(), "IReader/Backups/")
    val booksDirectory: File =
        File(Environment.getExternalStorageDirectory(), "IReader/Books/")
    val automaticBackupDirectory: File =
        File(Environment.getExternalStorageDirectory(), "IReader/Backups/Automatic/")

    init {

    }

    fun checkPermission(): Boolean {
        if (!mainIReaderDir.isDirectory) {
            mainIReaderDir.deleteRecursively()
        }
        if (!backupDirectory.isDirectory) {
            backupDirectory.deleteRecursively()
        }
        if (!automaticBackupDirectory.isDirectory) {
            automaticBackupDirectory.deleteRecursively()
        }
        if (!booksDirectory.isDirectory) {
            booksDirectory.deleteRecursively()
        }

        createIReaderDir()
        createNoMediaFile()
//        val isGranted = SimpleStorage.hasStorageAccess(
//            context,
//            mainIReaderDir.absolutePath,
//            requiresWriteAccess = true
//        )
//        return if (!isGranted) {
//            simpleStorageHelper.requestStorageAccess(
//                200, expectedStorageType = StorageType.EXTERNAL, initialPath = FileFullPath(
//                    context,
//                    mainIReaderDir
//                )
//            )
//            return false
//        } else true
        return true
    }

    fun createIReaderDir() {
        kotlin.runCatching {
            if (!mainIReaderDir.exists()) {
                DocumentFile.fromFile(Environment.getExternalStorageDirectory())
                    .createDirectory("IReader")
            }
        }
    }

    fun createNoMediaFile() {

        kotlin.runCatching {
            val noMediaFile = File(mainIReaderDir, ".nomedia")
            if (!noMediaFile.exists()) {
                DocumentFile.fromFile(mainIReaderDir).createFile("", ".nomedia")
            }
        }
    }

    fun get(dirName: String): DocumentFile {
        val dir = ireaderDirectory(dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return DocumentFileCompat.fromFile(
            context,
            dir,
            requiresWriteAccess = true,
            considerRawFile = true
        )!!
    }
}