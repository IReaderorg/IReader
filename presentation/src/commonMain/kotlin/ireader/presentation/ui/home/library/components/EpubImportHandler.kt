package ireader.presentation.ui.home.library.components

import ireader.domain.models.entities.BookItem

/**
 * Handler for EPUB file imports
 * Platform-specific implementations will handle file picking and parsing
 */
interface EpubImportHandler {
    /**
     * Import EPUB files from the given URIs
     * @param uris List of file URIs to import
     * @return Result containing list of imported books or error
     */
    suspend fun importEpubFiles(uris: List<String>): Result<List<BookItem>>
    
    /**
     * Show platform-specific file picker for EPUB files
     * @return List of selected file URIs
     */
    suspend fun pickEpubFiles(): List<String>
}

/**
 * Result of EPUB import operation
 */
sealed class ImportResult {
    data class Success(val books: List<BookItem>) : ImportResult()
    data class PartialSuccess(val books: List<BookItem>, val errors: List<String>) : ImportResult()
    data class Failure(val error: String) : ImportResult()
}
