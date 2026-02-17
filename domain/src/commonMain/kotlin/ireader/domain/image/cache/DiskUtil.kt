package ireader.domain.image.cache

import okio.FileSystem
import okio.Path

/**
 * Disk utilities using Okio for KMP compatibility.
 */
object DiskUtil {

    fun hashKeyForDisk(key: String): String {
        return Hash.md5(key)
    }

    /**
     * Calculate directory size recursively using Okio FileSystem.
     */
    fun getDirectorySize(path: Path, fileSystem: FileSystem): Long {
        var size: Long = 0
        if (fileSystem.exists(path)) {
            val metadata = fileSystem.metadata(path)
            if (metadata.isDirectory) {
                fileSystem.list(path).forEach { child ->
                    size += getDirectorySize(child, fileSystem)
                }
            } else {
                size = metadata.size ?: 0L
            }
        }
        return size
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
