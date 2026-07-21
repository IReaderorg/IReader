package ireader.core.util

/**
 * Platform-specific file picker for selecting files
 */
expect object PlatformFilePicker {
    /**
     * Pick files from the file system
     * @param fileTypes List of file extensions to filter (e.g., ["ttf", "otf"])
     * @param multiSelect Whether to allow multiple file selection
     * @return List of file paths, or null if cancelled
     */
    suspend fun pickFiles(fileTypes: List<String>, multiSelect: Boolean): List<String>?
}
