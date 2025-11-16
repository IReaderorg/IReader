package ireader.core.util

import java.io.File

actual object StorageUtil {
    actual fun getAvailableStorageSpace(): Long {
        return try {
            val userHome = System.getProperty("user.home")
            val file = File(userHome)
            file.usableSpace
        } catch (e: Exception) {
            -1L
        }
    }
    
    actual fun checkStorageBeforeOperation(requiredBytes: Long, bufferBytes: Long): Boolean {
        val available = getAvailableStorageSpace()
        return available >= (requiredBytes + bufferBytes)
    }
    
    actual fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "Unknown"
        
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return String.format("%.2f %s", size, units[unitIndex])
    }
}
