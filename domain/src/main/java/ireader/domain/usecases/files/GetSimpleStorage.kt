package ireader.domain.usecases.files

import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.StorageId
import com.anggrayudi.storage.file.tryCreateNewFile
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

    fun ireaderDirectory(dirName: String): File =
        File(Environment.getExternalStorageDirectory(), "IReader/${dirName}")

    fun extensionDirectory(): File =
        File(Environment.getExternalStorageDirectory(), "IReader/Extensions")

    fun checkPermission(): Boolean {
        val useCacheLocation = uiPreferences.savedLocalCatalogLocation().get()
        if (useCacheLocation) {
            return true
        }
        return if (!storage.isStorageAccessGranted(StorageId.PRIMARY)) {
            storage.requestFullStorageAccess()

            return false
        } else true
    }

    fun createNoMediaFile() {
        val ireaderDir = File(Environment.getExternalStorageDirectory(), "IReader")
        val temp = File(context.cacheDir, ".nomedia")
        if (!temp.exists()) {
            temp.tryCreateNewFile()
        }
        val noMediaFile = File(ireaderDir, ".nomedia")
        if (!noMediaFile.exists()) {
            temp.copyRecursively(ireaderDir, false)
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