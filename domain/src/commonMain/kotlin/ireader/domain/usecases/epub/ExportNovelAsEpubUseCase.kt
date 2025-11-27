package ireader.domain.usecases.epub

import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book

/**
 * Use case for exporting a novel as an ePub file.
 * This is a wrapper around EpubCreator that provides a cleaner API
 * for the End of Life Management feature.
 * 
 * Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7, 15.8, 15.9, 15.10
 */
class ExportNovelAsEpubUseCase(
    private val epubCreator: EpubCreator
) {
    /**
     * Exports a novel as an ePub file with progress reporting.
     * 
     * @param book The book to export
     * @param uri The destination URI for the ePub file
     * @param onProgress Callback for progress updates (e.g., "Loading chapters...", "Writing chapter 1/100")
     * @throws Exception if export fails (no chapters, file write error, etc.)
     */
    suspend operator fun invoke(
        book: Book,
        uri: Uri,
        onProgress: (String) -> Unit = {}
    ) {
        epubCreator.invoke(book, uri, onProgress)
    }
    
    /**
     * Generates a suggested filename for the ePub export.
     * 
     * @param book The book to export
     * @return Sanitized filename with .epub extension
     */
    fun getSuggestedFilename(book: Book): String {
        return sanitizeFilename(book.title) + ".epub"
    }
    
    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[|\\\\?*<\":>+\\[\\]/']+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
