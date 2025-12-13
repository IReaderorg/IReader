package ireader.domain.usecases.epub

import ireader.core.log.Log
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.TranslatedChapterRepository
import ireader.domain.epub.EpubBuilder
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.TranslatedChapter
import ireader.domain.models.epub.ExportOptions
import ireader.domain.usecases.local.book_usecases.FindBookById

/**
 * Use case for exporting a book as an EPUB file with customizable options.
 * 
 * This use case:
 * - Fetches book details and chapters
 * - Applies export options (selected chapters, formatting, etc.)
 * - Optionally uses translated content instead of original content
 * - Creates a valid EPUB 3.0 file
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5
 */
class ExportBookAsEpubUseCase(
    private val findBookById: FindBookById,
    private val chapterRepository: ChapterRepository,
    private val translatedChapterRepository: TranslatedChapterRepository,
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
            
            onProgress("Loading chapters with content...")
            
            // Get all chapters WITH their text content
            // The regular findChaptersByBookId uses a lightweight query without content
            val allChapters = chapterRepository.findChaptersByBookIdWithContent(bookId)
            
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
            
            // Fetch translated chapters if requested
            val translationsMap: Map<Long, TranslatedChapter> = if (options.useTranslatedContent) {
                onProgress("Loading translated content...")
                translatedChapterRepository.getTranslatedChaptersByBookId(bookId)
                    .filter { it.targetLanguage == options.translationTargetLanguage }
                    .associateBy { it.chapterId }
            } else {
                emptyMap()
            }
            
            if (options.useTranslatedContent && translationsMap.isEmpty()) {
                Log.warn { "No translations found for target language: ${options.translationTargetLanguage}" }
                onProgress("Warning: No translations found, using original content")
            }
            
            onProgress("Creating EPUB file...")
            
            // For content URIs (Android), we need to create a temp file first
            // then copy it to the content URI
            val isContentUri = outputUri.toString().startsWith("content://")
            val tempFilePath = if (isContentUri) {
                // Create temp file path - platform-specific temp dir will be used
                createTempEpubPath()
            } else {
                outputUri.toString()
            }
            
            // Create EPUB
            val result = epubBuilder.createEpub(
                book = book,
                chapters = selectedChapters,
                options = options,
                outputUri = tempFilePath,
                translationsMap = translationsMap
            )
            
            // If we used a temp file for content URI, we need to copy it
            if (isContentUri && result.isSuccess) {
                result.onSuccess { tempPath ->
                    copyTempFileToContentUri(tempPath, outputUri)
                }
            }
            
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
    
    /**
     * Create a temporary file path for EPUB creation
     * Platform-specific implementation will provide appropriate temp directory
     */
    private fun createTempEpubPath(): String {
        return createPlatformTempEpubPath()
    }
    
    /**
     * Copy temp file to content URI (Android only)
     * Desktop implementation will be a no-op
     */
    private suspend fun copyTempFileToContentUri(tempPath: String, contentUri: Uri) {
        copyPlatformTempFileToContentUri(tempPath, contentUri)
    }
}

/**
 * Platform-specific temp file creation
 */
expect fun createPlatformTempEpubPath(): String

/**
 * Platform-specific content URI handling
 */
expect suspend fun copyPlatformTempFileToContentUri(tempPath: String, contentUri: Uri)
