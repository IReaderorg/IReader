package ireader.domain.usecases.files

import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.documentfile.provider.DocumentFile
import coil.imageLoader
import com.anggrayudi.storage.SimpleStorage
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.DocumentFileCompat
import java.io.File


class AndroidGetSimpleStorage(
    private val context: Context,
) : GetSimpleStorage {

    lateinit var storage: SimpleStorage
    lateinit var simpleStorageHelper: SimpleStorageHelper

    fun provideActivity(activity: ComponentActivity, savedState: Bundle?) {
        storage = SimpleStorage(activity, savedState)
        simpleStorageHelper = SimpleStorageHelper(activity, savedState)
    }

    override val mainIReaderDir: File = File(Environment.getExternalStorageDirectory(), "IReader/")



    override fun ireaderDirectory(dirName: String): File =
        File(Environment.getExternalStorageDirectory(), "IReader/${dirName}/")

    override fun extensionDirectory(): File =
        File(Environment.getExternalStorageDirectory(), "IReader/Extensions/")

    override fun cacheExtensionDir() = File(context.cacheDir,"IReader/Extensions/")
    override fun ireaderCacheDir() = File(context.cacheDir,"IReader/cache/")

    override val backupDirectory: File =
        File(Environment.getExternalStorageDirectory(), "IReader/Backups/")
    override val booksDirectory: File =
        File(Environment.getExternalStorageDirectory(), "IReader/Books/")
    override  val automaticBackupDirectory: File =
        File(Environment.getExternalStorageDirectory(), "IReader/Backups/Automatic/")

    init {

    }

    override fun checkPermission(): Boolean {
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

    override fun createIReaderDir() {
        kotlin.runCatching {
            if (!mainIReaderDir.exists()) {
                DocumentFile.fromFile(Environment.getExternalStorageDirectory())
                    .createDirectory("IReader")
            }
        }
    }

    override fun createNoMediaFile() {

        kotlin.runCatching {
            val noMediaFile = File(mainIReaderDir, ".nomedia")
            if (!noMediaFile.exists()) {
                DocumentFile.fromFile(mainIReaderDir).createFile("", ".nomedia")
            }
        }
    }

    override fun clearImageCache() {
        context.imageLoader.memoryCache?.clear()
    }

    override fun clearCache() {
        context.cacheDir.deleteRecursively()
    }

    override fun getCacheSize(): String {
        return ireader.domain.utils.getCacheSize(context = context)
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