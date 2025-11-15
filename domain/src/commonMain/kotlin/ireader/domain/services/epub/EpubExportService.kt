package ireader.domain.services.epub

import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Service interface for exporting books as EPUB files with beautified formatting.
 * 
 * This service handles the complete EPUB generation process including:
 * - Creating EPUB container structure
 * - Generating metadata (OPF)
 * - Creating table of contents (NCX)
 * - Formatting chapter content with CSS
 * - Packaging as ZIP with .epub extension
 */
interface EpubExportService {
    
    /**
     * Export a book as an EPUB file with the specified options.
     * 
     * @param book The book to export
     * @param chapters The chapters to include in the export
     * @param options Export configuration options
     * @param onProgress Callback for progress updates (0.0 to 1.0)
     * @return Result containing the URI of the exported file or an error
     */
    suspend fun exportBook(
        book: Book,
        chapters: List<Chapter>,
        options: EpubExportOptions,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Result<Uri>
}

/**
 * Configuration options for EPUB export
 */
data class EpubExportOptions(
    val includeCover: Boolean = true,
    val paragraphSpacing: Float = 1.0f,
    val chapterHeadingSize: Float = 2.0f,
    val typography: EpubTypography = EpubTypography.DEFAULT,
    val cleanHtml: Boolean = true,
    val addDropCaps: Boolean = false
)

/**
 * Typography options for EPUB formatting
 */
enum class EpubTypography(val fontFamily: String) {
    DEFAULT("Georgia, serif"),
    SERIF("'Times New Roman', Times, serif"),
    SANS_SERIF("Arial, Helvetica, sans-serif")
}
