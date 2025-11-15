package ireader.domain.usecases.epub

import ireader.core.log.Log
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.epub.EpubBuilder
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.epub.ExportOptions
import ireader.domain.usecases.local.book_usecases.FindBookById

/**
 * Use case for exporting a book as an EPUB file with customizable options.
 * 
 * This use case:
 * - Fetches book details and chapters
 * - Applies export options (selected chapters, formatting, etc.)
 * - Creates a valid EPUB 3.0 file
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5
 */
class ExportBookAsEpubUseCase(
    private val findBookById: FindBookById,
    private val chapterRepository: ChapterRepository,
    private val epubBuilder: EpubBuilder
) {
    /**
     * Exports a book as an EPUB file
     * 
     * @param bookId The ID of the book to export
     * @param outputUri The destination file URI
     * @param options Export configuration options
     * @param onProgress Callback for progress updates
     * @return Result containing the file path or an error
     */
    suspend operator fun invoke(
        bookId: Long,
        outputUri: Uri,
        options: ExportOptions = ExportOptions(),
        onProgress: (String) -> Unit = {}
    ): Result<String> {
        return try {
            onProgress("Loading book details...")
            
            // Get book details
            val book = findBookById(bookId)
                ?: return Result.failure(Exception("Book not found"))
            
            onProgress("Loading chapters...")
            
            // Get all chapters
            val allChapters = chapterRepository.findChaptersByBookId(bookId)
            
            if (allChapters.isEmpty()) {
                return Result.failure(Exception("No chapters found for this book"))
            }
            
            // Filter selected chapters if specified
            val selectedChapters = if (options.selectedChapters.isEmpty()) {
                allChapters
            } else {
                allChapters.filter { it.id in options.selectedChapters }
            }
            
            if (selectedChapters.isEmpty()) {
                return Result.failure(Exception("No chapters selected for export"))
            }
            
            onProgress("Creating EPUB file...")
            
            // Create EPUB
            val result = epubBuilder.createEpub(
                book = book,
                chapters = selectedChapters,
                options = options,
                outputUri = outputUri.toString()
            )
            
            result.onSuccess {
                onProgress("EPUB created successfully!")
            }.onFailure { error ->
                onProgress("Export failed: ${error.message}")
                Log.error("EPUB export failed", error)
            }
            
            result
        } catch (e: Exception) {
            Log.error("EPUB export failed", e)
            Result.failure(e)
        }
    }
}
