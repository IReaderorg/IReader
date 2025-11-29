package ireader.domain.usecases.epub

import androidx.compose.runtime.Composable
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.epub.EpubBuilder
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.epub.ExportOptions
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Desktop implementation of EPUB creator using EpubBuilder
 */
actual class EpubCreator(
    private val chapterRepository: ChapterRepository,
    private val httpClient: HttpClient
) {
    private val epubBuilder = EpubBuilder(httpClient)
    
    actual suspend operator fun invoke(book: Book, uri: Uri, currentEvent: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                currentEvent("Loading chapters with content...")
                // Use findChaptersByBookIdWithContent to get chapters WITH their text content
                // The regular findChaptersByBookId uses a lightweight query without content
                val chapters = chapterRepository.findChaptersByBookIdWithContent(book.id)
                
                if (chapters.isEmpty()) {
                    throw Exception("No chapters found for this book")
                }
                
                // Use EpubBuilder with export options
                val options = ExportOptions(
                    includeCover = book.cover.isNotEmpty(),
                    selectedChapters = emptySet() // Export all chapters
                )
                
                val result = epubBuilder.createEpub(
                    book = book,
                    chapters = chapters,
                    options = options,
                    outputUri = uri.uriString
                )
                
                result.onSuccess {
                    currentEvent("EPUB created successfully!")
                }.onFailure { error ->
                    currentEvent("Error: ${error.message}")
                    throw error
                }
            } catch (e: Exception) {
                currentEvent("Error: ${e.message}")
                throw e
            }
        }
    }
}