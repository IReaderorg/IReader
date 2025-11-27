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
                android.util.Log.d("EpubCreator", "Starting EPUB export for book: ${book.title}")
                android.util.Log.d("EpubCreator", "Output URI: ${uri.androidUri}")
                currentEvent("Loading chapters...")
                val chapters = chapterRepository.findChaptersByBookId(book.id)
                android.util.Log.d("EpubCreator", "Found ${chapters.size} chapters")
                
                if (chapters.isEmpty()) {
                    throw Exception("No chapters found for this book")
                }
                
                // Get cover file path if available
                android.util.Log.d("EpubCreator", "Getting cover file...")
                val coverFile = coverCache.getCoverFile(BookCover.Companion.from(book))
                val coverUrl = try {
                    if (coverFile != null && coverFile.exists()) {
                        // Verify the file actually exists on disk
                        val javaFile = File(coverFile.path)
                        if (javaFile.exists() && javaFile.canRead()) {
                            "file://${coverFile.path}"
                        } else {
                            book.cover
                        }
                    } else {
                        book.cover
                    }
                } catch (e: Exception) {
                    // If there's any error accessing the cover, use the original URL
                    book.cover
                }
                
                android.util.Log.d("EpubCreator", "Cover URL: $coverUrl")
                
                // Create temp file for EpubBuilder
                val tempFile = File.createTempFile("epub_export_", ".epub", appContext.cacheDir)
                android.util.Log.d("EpubCreator", "Temp file created: ${tempFile.absolutePath}")
                
                // Use EpubBuilder with export options
                val options = ExportOptions(
                    includeCover = coverUrl.isNotEmpty(),
                    selectedChapters = emptySet() // Export all chapters
                )
                
                android.util.Log.d("EpubCreator", "Calling epubBuilder.createEpub...")
                val result = epubBuilder.createEpub(
                    book = book.copy(cover = coverUrl),
                    chapters = chapters,
                    options = options,
                    outputUri = tempFile.absolutePath,
                    tempDirPath = appContext.cacheDir.absolutePath
                )
                
                result.onSuccess { tempPath ->
                    // Copy from temp file to Android URI
                    currentEvent("Saving EPUB file...")
                    try {
                        val sourceFile = File(tempPath)
                        if (!sourceFile.exists()) {
                            throw Exception("Generated EPUB file not found at: $tempPath")
                        }
                        
                        val contentResolver = appContext.contentResolver
                        val outputStream = contentResolver.openOutputStream(uri.androidUri)
                            ?: throw Exception("Failed to open output stream for URI: ${uri.androidUri}")
                        
                        outputStream.use { output ->
                            sourceFile.inputStream().use { input ->
                                input.copyTo(output)
                            }
                        }
                        
                        tempFile.delete()
                        currentEvent("EPUB created successfully!")
                    } catch (e: Exception) {
                        tempFile.delete()
                        throw Exception("Failed to save EPUB: ${e.message}", e)
                    }
                }.onFailure { error ->
                    tempFile.delete()
                    throw Exception("EPUB generation failed: ${error.message}", error)
                }
            } catch (e: Exception) {
                val errorMessage = "Error: ${e.message ?: "Unknown error"}"
                android.util.Log.e("EpubCreator", "EPUB creation failed", e)
                android.util.Log.e("EpubCreator", "Error details: ${e.javaClass.name}: ${e.message}")
                android.util.Log.e("EpubCreator", "Stack trace: ${e.stackTraceToString()}")
                currentEvent(errorMessage)
                // Log the full stack trace for debugging
                e.printStackTrace()
                throw Exception("EPUB creation failed: ${e.message}", e)
            }
        }
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[|\\\\?*<\":>+\\[\\]/']+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    @Composable
    fun onEpubCreateRequested(book: Book, onStart: @Composable ((Any) -> Unit)) {
        val mimeTypes = arrayOf("application/epub+zip")
        val filename = "${sanitizeFilename(book.title)}.epub"
        android.util.Log.d("EpubCreator", "Creating file picker intent for: $filename")
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("application/epub+zip")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            .putExtra(Intent.EXTRA_TITLE, filename)
        android.util.Log.d("EpubCreator", "Launching file picker intent")
        onStart(intent)
    }
}
