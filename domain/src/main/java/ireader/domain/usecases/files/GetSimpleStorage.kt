package ireader.domain.usecases.files

import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.FileFullPath
import com.anggrayudi.storage.file.StorageType
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

    fun mainIReaderDir(): File {
        return File(Environment.getExternalStorageDirectory(), "IReader/")
    }

    fun ireaderDirectory(dirName: String): File =
        File(Environment.getExternalStorageDirectory(), "IReader/${dirName}")

    fun extensionDirectory(): File =
        File(Environment.getExternalStorageDirectory(), "IReader/Extensions")

    // No need for this right now.
    fun checkPermission(): Boolean {
        createIReaderDir()
        createNoMediaFile()
        val isGranted = SimpleStorage.hasStorageAccess(
            context,
            mainIReaderDir().absolutePath,
            requiresWriteAccess = true
        )
        return if (!isGranted) {
            simpleStorageHelper.requestStorageAccess(
                200, expectedStorageType = StorageType.EXTERNAL, initialPath = FileFullPath(
                    context,
                    mainIReaderDir()
                )
            )
            return false
        } else true
    }

    fun createIReaderDir() {
        kotlin.runCatching {
            if (!mainIReaderDir().exists()) {
                DocumentFile.fromFile(Environment.getExternalStorageDirectory()).createDirectory("IReader")
            }
        }
    }

    fun createNoMediaFile() {

        kotlin.runCatching {
            val noMediaFile = File(mainIReaderDir(), ".nomedia")
            if (!noMediaFile.exists()) {
                DocumentFile.fromFile(mainIReaderDir()).createFile("",".nomedia")
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