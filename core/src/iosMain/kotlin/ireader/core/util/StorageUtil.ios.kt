package ireader.core.util

/**
 * iOS implementation of StorageUtil
 * TODO: Implement using NSFileManager
 */
actual object StorageUtil {
    actual fun getAvailableSpace(): Long {
        // TODO: Implement using NSFileManager.defaultManager().attributesOfFileSystemForPath
        return Long.MAX_VALUE
    }
    
    actual fun hasEnoughSpace(requiredBytes: Long): Boolean {
        return getAvailableSpace() >= requiredBytes
    }
}
