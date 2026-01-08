package ireader.domain.services.download

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Interface for managing download directories and file paths.
 * Based on Mihon's DownloadProvider for centralized directory management.
 */
interface DownloadProvider {
    
    /**
     * Returns the root directory for all downloads.
     */
    fun getDownloadsRoot(): String
    
    /**
     * Returns the directory for a specific source.
     * Pattern: {downloads_root}/{source_name}
     */
    fun getSourceDirectory(sourceName: String): String
    
    /**
     * Returns the directory for a specific book.
     * Pattern: {downloads_root}/{source_name}/{book_title}
     */
    fun getBookDirectory(sourceName: String, book: Book): String
    
    /**
     * Returns the directory for a specific chapter.
     * Pattern: {downloads_root}/{source_name}/{book_title}/{chapter_name}
     */
    fun getChapterDirectory(sourceName: String, book: Book, chapter: Chapter): String
    
    /**
     * Returns the content file path for a specific chapter.
     * Pattern: {chapter_directory}/content.txt
     */
    fun getChapterContentFile(sourceName: String, book: Book, chapter: Chapter): String
    
    /**
     * Sanitizes a name for use as a file/folder name.
     * Removes invalid filesystem characters and handles edge cases.
     */
    fun sanitizeName(name: String): String
    
    /**
     * Returns the available disk space in bytes.
     */
    fun getAvailableSpace(): Long
    
    /**
     * Returns true if the chapter directory exists.
     */
    fun chapterDirectoryExists(sourceName: String, book: Book, chapter: Chapter): Boolean
    
    /**
     * Returns true if the chapter content file exists.
     */
    fun chapterContentExists(sourceName: String, book: Book, chapter: Chapter): Boolean
    
    /**
     * Creates the chapter directory if it doesn't exist.
     * Returns true if successful.
     */
    fun createChapterDirectory(sourceName: String, book: Book, chapter: Chapter): Boolean
    
    /**
     * Deletes the chapter directory and all its contents.
     * Returns true if successful.
     */
    fun deleteChapterDirectory(sourceName: String, book: Book, chapter: Chapter): Boolean
    
    /**
     * Deletes the book directory and all its contents.
     * Returns true if successful.
     */
    fun deleteBookDirectory(sourceName: String, book: Book): Boolean
    
    /**
     * Returns all downloaded chapter IDs for a book.
     */
    fun getDownloadedChapterIds(sourceName: String, book: Book): Set<Long>
    
    /**
     * Returns all downloaded book directories for a source.
     */
    fun getDownloadedBooks(sourceName: String): List<String>
    
    companion object {
        /**
         * Invalid filesystem characters to remove from names.
         */
        val INVALID_CHARS = charArrayOf('\\', '/', ':', '*', '?', '"', '<', '>', '|', '\u0000')
        
        /**
         * Maximum length for file/folder names.
         */
        const val MAX_NAME_LENGTH = 255
        
        /**
         * Default content file name.
         */
        const val CONTENT_FILE_NAME = "content.txt"
        
        /**
         * Minimum disk space required for downloads (200 MB).
         */
        const val MIN_DISK_SPACE = 200L * 1024 * 1024
    }
}

/**
 * Default implementation of sanitizeName that can be used by platform implementations.
 */
fun defaultSanitizeName(name: String): String {
    var sanitized = name.trim()
    
    // Remove invalid characters
    for (char in DownloadProvider.INVALID_CHARS) {
        sanitized = sanitized.replace(char.toString(), "")
    }
    
    // Replace multiple spaces with single space
    sanitized = sanitized.replace(Regex("\\s+"), " ")
    
    // Remove leading/trailing dots and spaces
    sanitized = sanitized.trim('.', ' ')
    
    // Handle empty result
    if (sanitized.isEmpty()) {
        sanitized = "unnamed"
    }
    
    // Truncate if too long
    if (sanitized.length > DownloadProvider.MAX_NAME_LENGTH) {
        sanitized = sanitized.take(DownloadProvider.MAX_NAME_LENGTH)
    }
    
    return sanitized
}
