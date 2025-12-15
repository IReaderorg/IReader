package ireader.presentation.ui.book.helpers

/**
 * Platform-specific helper functions for book operations.
 * These functions have different implementations on Android and Desktop.
 */
expect class PlatformHelper {
    /**
     * Share text content using platform-specific sharing mechanism.
     * @param text The text content to share
     * @param title Optional title for the share dialog
     */
    fun shareText(text: String, title: String = "Share")
    
    /**
     * Create a URI for EPUB export file.
     * @param bookTitle The title of the book being exported
     * @param author The author of the book
     * @return URI string where the EPUB should be saved, or null if unavailable
     */
    fun createEpubExportUri(bookTitle: String, author: String): String?

    fun copyToClipboard(label: String, content: String)
    
    /**
     * Copy an image from a URI to the app's custom cover directory.
     * @param sourceUri The URI of the source image (content:// or file://)
     * @param bookId The ID of the book to save the cover for
     * @return The file path of the saved cover, or null if failed
     */
    suspend fun copyImageToCustomCover(sourceUri: String, bookId: Long): String?
}
