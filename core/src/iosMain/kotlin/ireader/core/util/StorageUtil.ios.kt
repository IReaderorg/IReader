package ireader.core.util

import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value

/**
 * iOS implementation of StorageUtil using NSFileManager
 */
@OptIn(ExperimentalForeignApi::class)
actual object StorageUtil {
    actual fun getAvailableStorageSpace(): Long {
        return try {
            val fileManager = NSFileManager.defaultManager
            val documentDirectory = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            ).firstOrNull() as? String ?: return Long.MAX_VALUE
            
            val attributes = fileManager.attributesOfFileSystemForPath(documentDirectory, null)
            val freeSpace = attributes?.get(NSFileSystemFreeSize) as? NSNumber
            freeSpace?.longLongValue ?: Long.MAX_VALUE
        } catch (e: Exception) {
            Long.MAX_VALUE
        }
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
