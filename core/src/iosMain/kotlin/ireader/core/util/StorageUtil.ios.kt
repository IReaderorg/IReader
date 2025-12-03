package ireader.core.util

/**
 * iOS implementation of StorageUtil
 * TODO: Implement using NSFileManager
 */
actual object StorageUtil {
    actual fun getAvailableStorageSpace(): Long {
        // TODO: Implement using NSFileManager.defaultManager().attributesOfFileSystemForPath
        return Long.MAX_VALUE
    }
    
    actual fun checkStorageBeforeOperation(requiredBytes: Long, bufferBytes: Long): Boolean {
        val available = getAvailableStorageSpace()
        return available >= (requiredBytes + bufferBytes)
    }
    
    actual fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> {
                val gb = bytes / 1_000_000_000.0
                "${(gb * 100).toLong() / 100.0} GB"
            }
            bytes >= 1_000_000 -> {
                val mb = bytes / 1_000_000.0
                "${(mb * 100).toLong() / 100.0} MB"
            }
            bytes >= 1_000 -> {
                val kb = bytes / 1_000.0
                "${(kb * 100).toLong() / 100.0} KB"
            }
            else -> "$bytes B"
        }
    }
}
