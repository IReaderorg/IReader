package ireader.domain.usecases.files

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.StorageId
import org.koin.core.annotation.Single
import java.io.File

@Single
class GetSimpleStorage(
    private val context: Context,
) {

    lateinit var storage: SimpleStorage

    fun provideActivity(activity: Activity,savedState: Bundle?) {
        storage = SimpleStorage(activity,savedState)
    }

    fun ireaderDirectory(dirName: String): File =
        File(Environment.getExternalStorageDirectory(), "IReader/${dirName}")

    fun extensionDirectory(): File =
        File(Environment.getExternalStorageDirectory(), "IReader/Extensions")

    fun checkPermission() {
        if (!storage.isStorageAccessGranted(StorageId.PRIMARY)) {
            storage.requestFullStorageAccess()
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