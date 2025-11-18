package ireader.domain.usecases.epub

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.epub.EpubBuilder
import ireader.domain.image.CoverCache
import ireader.domain.models.BookCover
import ireader.domain.models.entities.Book
import ireader.domain.models.epub.ExportOptions
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class EpubCreator(
    private val coverCache: CoverCache,
    private val chapterRepository: ChapterRepository,
    context: Context,
    private val httpClient: HttpClient
) {
    private val appContext: Context = context.applicationContext
    private val epubBuilder = EpubBuilder(httpClient)

    actual suspend operator fun invoke(
        book: Book,
        uri: ireader.domain.models.common.Uri,
        currentEvent: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                currentEvent("Loading chapters...")
                val chapters = chapterRepository.findChaptersByBookId(book.id)
                
                if (chapters.isEmpty()) {
                    throw Exception("No chapters found for this book")
                }
                
                // Get cover file path if available
                val coverFile = coverCache.getCoverFile(BookCover.Companion.from(book))
                val coverUrl = if (coverFile?.exists() == true) {
                    "file://${coverFile.absolutePath}"
                } else {
                    book.cover
                }
                
                // Create temp file for EpubBuilder
                val tempFile = File.createTempFile("epub_export_", ".epub", appContext.cacheDir)
                
                // Use EpubBuilder with export options
                val options = ExportOptions(
                    includeCover = coverUrl.isNotEmpty(),
                    selectedChapters = emptySet() // Export all chapters
                )
                
                val result = epubBuilder.createEpub(
                    book = book.copy(cover = coverUrl),
                    chapters = chapters,
                    options = options,
                    outputUri = tempFile.absolutePath
                )
                
                result.onSuccess { tempPath ->
                    // Copy from temp file to Android URI
                    currentEvent("Saving EPUB file...")
                    val contentResolver = appContext.contentResolver
                    contentResolver.openOutputStream(uri.androidUri)?.use { outputStream ->
                        File(tempPath).inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    tempFile.delete()
                    currentEvent("EPUB created successfully!")
                }.onFailure { error ->
                    tempFile.delete()
                    throw error
                }
            } catch (e: Exception) {
                currentEvent("Error: ${e.message}")
                throw e
            }
        }
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[|\\\\?*<\":>+\\[\\]/']+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    @Composable
    actual fun onEpubCreateRequested(book: Book, onStart: @Composable ((Any) -> Unit)) {
        val mimeTypes = arrayOf("application/epub+zip")
        val filename = "${sanitizeFilename(book.title)}.epub"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("application/epub+zip")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            .putExtra(Intent.EXTRA_TITLE, filename)
        onStart(intent)
    }
}
