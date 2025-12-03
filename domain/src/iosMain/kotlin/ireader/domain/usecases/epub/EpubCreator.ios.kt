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
 */
actual class EpubCreator {
    
    private val fileSystem = FileSystem.SYSTEM
    private var httpClient: HttpClient? = null
    private var chapterRepository: ChapterRepository? = null
    
    fun setDependencies(httpClient: HttpClient, chapterRepository: ChapterRepository) {
        this.httpClient = httpClient
        this.chapterRepository = chapterRepository
    }
    
    actual suspend operator fun invoke(book: Book, uri: Uri, currentEvent: (String) -> Unit) {
        val client = httpClient
        if (client == null) {
            currentEvent("Error: HttpClient not initialized")
            return
        }
        
        val chapRepo = chapterRepository
        if (chapRepo == null) {
            currentEvent("Error: ChapterRepository not initialized")
            return
        }
        
        try {
            currentEvent("Loading chapters...")
            val chapters = withContext(Dispatchers.Default) {
                chapRepo.findChaptersByBookId(book.id)
            }
            
            if (chapters.isEmpty()) {
                currentEvent("Error: No chapters found for this book")
                return
            }
            
            currentEvent("Found ${chapters.size} chapters")

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
                chapters = chapters,
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

private suspend fun ChapterRepository.findChaptersByBookId(bookId: Long): List<Chapter> = emptyList()
