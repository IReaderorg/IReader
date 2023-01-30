package ireader.domain.image.cache

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import java.io.File

object DiskUtil {

    fun hashKeyForDisk(key: String): String {
        return Hash.md5(key)
    }

    fun getDirectorySize(f: File): Long {
        var size: Long = 0
        if (f.isDirectory) {
            for (file in f.listFiles().orEmpty()) {
                size += getDirectorySize(file)
            }
        } else {
            size = f.length()
        }
        return size
    }

    /**
     * Returns the root folders of all the available external storages.
     */
    fun getExternalStorages(context: Context): Collection<File> {
        val directories = mutableSetOf<File>()
        directories += ContextCompat.getExternalFilesDirs(context, null)
            .filterNotNull()
            .mapNotNull {
                val file = File(it.absolutePath.substringBefore("/Android/"))
                val state = Environment.getExternalStorageState(file)
                if (state == Environment.MEDIA_MOUNTED || state == Environment.MEDIA_MOUNTED_READ_ONLY) {
                    file
                } else {
                    null
                }
            }

        return directories
    }

    /**
     * Scans the given file so that it can be shown in gallery apps, for example.
     */
    fun scanMedia(context: Context, file: File) {
        scanMedia(context, file.toUri())
    }

    /**
     * Scans the given file so that it can be shown in gallery apps, for example.
     */
    fun scanMedia(context: Context, uri: Uri) {
        val action = Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
        val mediaScanIntent = Intent(action)
        mediaScanIntent.data = uri
        context.sendBroadcast(mediaScanIntent)
    }

    /**
     * Mutate the given filename to make it valid for a FAT filesystem,
     * replacing any invalid characters with "_". This method doesn't allow hidden files (starting
     * with a dot), but you can manually add it later.
     */
    fun buildValidFilename(origName: String): String {
        val name = origName.trim('.', ' ')
        if (name.isEmpty()) {
            return "(invalid)"
        }
        val sb = StringBuilder(name.length)
        name.forEach { c ->
            if (isValidFatFilenameChar(c)) {
                sb.append(c)
            } else {
                sb.append('_')
            }
        }
        // Even though vfat allows 255 UCS-2 chars, we might eventually write to
        // ext4 through a FUSE layer, so use that limit minus 15 reserved characters.
        return sb.toString().take(240)
    }

    /**
     * Returns true if the given character is a valid filename character, false otherwise.
     */
    private fun isValidFatFilenameChar(c: Char): Boolean {
        if (0x00.toChar() <= c && c <= 0x1f.toChar()) {
            return false
        }
        return when (c) {
            '"', '*', '/', ':', '<', '>', '?', '\\', '|', 0x7f.toChar() -> false
            else -> true
        }
    }
}
