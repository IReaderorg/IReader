package ireader.domain.usecases.epub

import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.epub.EpubBuilder
import ireader.domain.models.epub.ExportOptions
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

/**
 * iOS implementation of EpubCreator
 * 
 * Creates EPUB files from books using the EpubBuilder.
 * 
 * ## Usage
 * ```kotlin
 * val creator = EpubCreator()
 * creator.setDependencies(httpClient, chapterRepository)
 * creator(book, uri) { event -> println(event) }
 * ```
 * 
 * ## Dependencies
 * - HttpClient: For downloading cover images
 * - ChapterRepository: For fetching chapter content
 * 
 * ## Note
 * Uses `findChaptersByBookIdWithContent()` to get chapters WITH their text content.
 * This is required for EPUB export but can be memory-intensive for large books.
 */
actual class EpubCreator {
    
    private val fileSystem = FileSystem.SYSTEM
    private var httpClient: HttpClient? = null
    private var chapterRepository: ChapterRepository? = null
    
    /**
     * Set required dependencies for EPUB creation
     * 
     * @param httpClient HTTP client for downloading cover images
     * @param chapterRepository Repository for fetching chapter content
     */
    fun setDependencies(httpClient: HttpClient, chapterRepository: ChapterRepository) {
        this.httpClient = httpClient
        this.chapterRepository = chapterRepository
    }
    
    actual suspend operator fun invoke(book: Book, uri: Uri, currentEvent: (String) -> Unit) {
        val client = httpClient
        if (client == null) {
            currentEvent("Error: HttpClient not initialized. Call setDependencies() first.")
            return
        }
        
        val chapRepo = chapterRepository
        if (chapRepo == null) {
            currentEvent("Error: ChapterRepository not initialized. Call setDependencies() first.")
            return
        }
        
        try {
            currentEvent("Loading chapters with content...")
            
            // Use findChaptersByBookIdWithContent to get chapters WITH their text content
            // This is essential for EPUB export - findChaptersByBookId only returns metadata
            val chapters = withContext(Dispatchers.Default) {
                chapRepo.findChaptersByBookIdWithContent(book.id)
            }
            
            if (chapters.isEmpty()) {
                currentEvent("Error: No chapters found for this book")
                return
            }
            
            // Filter out chapters without content using Chapter.isEmpty() method
            // Chapter.content is List<Page>, and isEmpty() checks if content.joinToString().isBlank()
            val chaptersWithContent = chapters.filter { !it.isEmpty() }
            if (chaptersWithContent.isEmpty()) {
                currentEvent("Error: No chapters with content found. Download chapters first.")
                return
            }
            
            currentEvent("Found ${chaptersWithContent.size} chapters with content (${chapters.size} total)")

            val options = ExportOptions(
                includeCover = true,
                selectedChapters = emptySet(),
                fontFamily = "Georgia, serif",
                fontSize = 16,
                paragraphSpacing = 1.0f,
                chapterHeadingSize = 1.5f
            )
            
            currentEvent("Creating EPUB structure...")
            val epubBuilder = EpubBuilder(client, fileSystem)
            val outputPath = resolveOutputPath(uri, book)
            
            currentEvent("Building EPUB file...")
            val result = epubBuilder.createEpub(
                book = book,
                chapters = chaptersWithContent,
                options = options,
                outputUri = outputPath
            )
            
            result.fold(
                onSuccess = { path -> currentEvent("EPUB created successfully: $path") },
                onFailure = { error -> currentEvent("Error creating EPUB: ${error.message}") }
            )
        } catch (e: Exception) {
            currentEvent("Error: ${e.message}")
        }
    }
    
    private fun resolveOutputPath(uri: Uri, book: Book): String {
        val uriString = uri.toString()
        return when {
            uriString.startsWith("file://") -> uriString.removePrefix("file://")
            uriString.startsWith("/") -> uriString
            else -> {
                val documentsDir = getDocumentsDirectory()
                val fileName = sanitizeFileName(book.title) + ".epub"
                "$documentsDir/$fileName"
            }
        }
    }
    
    private fun getDocumentsDirectory(): String {
        val paths = platform.Foundation.NSSearchPathForDirectoriesInDomains(
            platform.Foundation.NSDocumentDirectory,
            platform.Foundation.NSUserDomainMask,
            true
        )
        return (paths.firstOrNull() as? String) ?: FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toString()
    }
    
    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_").replace(Regex("\\s+"), "_").take(100)
    }
}
