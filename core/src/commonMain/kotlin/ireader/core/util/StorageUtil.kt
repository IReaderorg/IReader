package ireader.core.util

/**
 * Utility for checking storage space availability
 */
expect object StorageUtil {
    /**
     * Get available storage space in bytes
     * @return Available storage space in bytes, or -1 if unable to determine
     */
    fun getAvailableStorageSpace(): Long
    
    /**
     * Check if there's enough storage space for an operation
     * @param requiredBytes Required space in bytes
     * @param bufferBytes Additional buffer space (default 100MB)
     * @return true if enough space is available
     */
    fun checkStorageBeforeOperation(requiredBytes: Long, bufferBytes: Long = 100 * 1024 * 1024): Boolean
    
    /**
     * Format bytes to human-readable string
     * @param bytes Size in bytes
     * @return Formatted string (e.g., "1.5 GB")
     */
    fun formatBytes(bytes: Long): String
}
