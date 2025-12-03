package ireader.domain.usecases.backup.lnreader

/**
 * Exception types for LNReader backup import errors
 */
sealed class LNReaderImportException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * The file is not a valid LNReader backup
     */
    class InvalidBackupException(
        message: String = "Not a valid LNReader backup file. Please select a .zip file exported from LNReader.",
        cause: Throwable? = null
    ) : LNReaderImportException(message, cause)
    
    /**
     * The backup file is corrupted or incomplete
     */
    class CorruptedBackupException(
        message: String = "The backup file appears to be corrupted or incomplete.",
        cause: Throwable? = null
    ) : LNReaderImportException(message, cause)
    
    /**
     * The backup contains no novels
     */
    class EmptyBackupException(
        message: String = "The backup file contains no novels to import.",
        cause: Throwable? = null
    ) : LNReaderImportException(message, cause)
    
    /**
     * Failed to read the backup file
     */
    class ReadFailedException(
        details: String,
        cause: Throwable? = null
    ) : LNReaderImportException("Failed to read backup file: $details", cause)
    
    /**
     * Failed to parse backup data
     */
    class ParseFailedException(
        details: String,
        cause: Throwable? = null
    ) : LNReaderImportException("Failed to parse backup data: $details", cause)
    
    /**
     * Failed to import a specific novel
     */
    class NovelImportFailedException(
        novelName: String,
        details: String,
        cause: Throwable? = null
    ) : LNReaderImportException("Failed to import novel \"$novelName\": $details", cause)
    
    /**
     * Failed to import a specific category
     */
    class CategoryImportFailedException(
        categoryName: String,
        details: String,
        cause: Throwable? = null
    ) : LNReaderImportException("Failed to import category \"$categoryName\": $details", cause)
    
    /**
     * Failed to import chapters
     */
    class ChapterImportFailedException(
        novelName: String,
        details: String,
        cause: Throwable? = null
    ) : LNReaderImportException("Failed to import chapters for \"$novelName\": $details", cause)
    
    /**
     * Source not found for plugin
     */
    class SourceNotFoundException(
        pluginId: String,
        cause: Throwable? = null
    ) : LNReaderImportException("Source not found for plugin: $pluginId. Novel will be imported as local.", cause)
    
    /**
     * Database error during import
     */
    class DatabaseException(
        details: String,
        cause: Throwable? = null
    ) : LNReaderImportException("Database error during import: $details", cause)
    
    /**
     * Permission denied
     */
    class PermissionDeniedException(
        message: String = "Storage permission denied. Please grant permission to access files.",
        cause: Throwable? = null
    ) : LNReaderImportException(message, cause)
    
    /**
     * File not found
     */
    class FileNotFoundException(
        message: String = "Backup file not found or inaccessible.",
        cause: Throwable? = null
    ) : LNReaderImportException(message, cause)
    
    /**
     * Out of memory
     */
    class OutOfMemoryException(
        message: String = "Not enough memory to process this backup. Try closing other apps.",
        cause: Throwable? = null
    ) : LNReaderImportException(message, cause)
    
    /**
     * Unknown error
     */
    class UnknownException(
        details: String,
        cause: Throwable? = null
    ) : LNReaderImportException("An unexpected error occurred: $details", cause)
    
    companion object {
        /**
         * Convert a generic exception to an LNReaderImportException
         */
        fun fromException(e: Throwable): LNReaderImportException {
            return when (e) {
                is LNReaderImportException -> e
                is okio.FileNotFoundException -> FileNotFoundException(cause = e)
                is OutOfMemoryError -> OutOfMemoryException(cause = e)
                is SecurityException -> PermissionDeniedException(cause = e)
                is kotlinx.serialization.SerializationException -> ParseFailedException(e.message ?: "JSON parsing error", e)
                else -> {
                    // Check exception message for zip-related errors
                    val message = e.message?.lowercase() ?: ""
                    if (message.contains("zip") || message.contains("corrupt")) {
                        CorruptedBackupException(cause = e)
                    } else if (message.contains("not found") || message.contains("no such file")) {
                        FileNotFoundException(cause = e)
                    } else {
                        UnknownException(e.message ?: "Unknown error", e)
                    }
                }
            }
        }
    }
}
