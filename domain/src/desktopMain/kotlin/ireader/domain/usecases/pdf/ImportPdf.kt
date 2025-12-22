package ireader.domain.usecases.pdf

import ireader.core.source.LocalSource
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.Text
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.storage.CacheManager
import ireader.domain.storage.StorageManager
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

/**
 * Desktop PDF import implementation using Apache PDFBox
 * 
 * Extracts text from PDF files and creates Book/Chapter entities
 * that can be read and used with TTS.
 */
actual class ImportPdf(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val cacheManager: CacheManager,
    private val storageManager: StorageManager
) {
    actual suspend fun parse(uris: List<Uri>) = withContext(Dispatchers.IO) {
        val errors = mutableListOf<Pair<String, String>>()
        
        uris.forEach { uri ->
            try {
                importPdf(uri)
            } catch (e: Exception) {
                val filePath = uri.path
                errors.add(filePath to (e.message ?: "Unknown error"))
                println("Failed to import PDF $filePath: ${e.message}")
                e.printStackTrace()
            }
        }
        
        if (errors.isNotEmpty()) {
            val errorMessage = errors.joinToString("\n") { (path, error) ->
                "${File(path).name}: $error"
            }
            throw Exception("Failed to import ${errors.size} PDF file(s):\n$errorMessage")
        }
    }
    
    private suspend fun importPdf(uri: Uri) {
        val pdfFile = File(uri.path)
        
        if (!pdfFile.exists()) {
            throw Exception("PDF file not found: ${uri.path}")
        }
        
        // Open PDF with PDFBox
        val document: PDDocument = Loader.loadPDF(pdfFile)
        
        try {
            val pageCount = document.numberOfPages
            if (pageCount == 0) {
                throw Exception("PDF has no pages")
            }
            
            // Extract title from filename
            val title = pdfFile.nameWithoutExtension
            
            // Try to get metadata
            val info = document.documentInformation
            val author = info?.author?.takeIf { it.isNotBlank() } ?: "PDF Import"
            val subject = info?.subject?.takeIf { it.isNotBlank() } ?: ""
            
            // Generate unique key
            val key = generateBookKey(title)
            bookRepository.delete(key)
            
            // Create book
            val bookId = Book(
                title = title,
                key = key,
                favorite = true,
                sourceId = LocalSource.SOURCE_ID,
                cover = "",
                author = author,
                status = MangaInfo.UNKNOWN,
                description = if (subject.isNotBlank()) subject else "Imported from PDF ($pageCount pages)",
                lastUpdate = currentTimeToLong()
            ).let { bookRepository.upsert(it) }
            
            // Extract chapters
            val chapters = extractChapters(document, bookId, key, pageCount)
            
            if (chapters.isEmpty()) {
                throw Exception("No readable content found in PDF")
            }
            
            chapterRepository.insertChapters(chapters)
            println("Successfully imported PDF: $title (${chapters.size} chapters from $pageCount pages)")
            
        } finally {
            document.close()
        }
    }
    
    /**
     * Extract chapters from PDF pages using PDFBox text extraction
     */
    private fun extractChapters(
        document: PDDocument,
        bookId: Long,
        key: String,
        pageCount: Int
    ): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val pagesPerChapter = calculatePagesPerChapter(pageCount)
        
        val textStripper = PDFTextStripper()
        
        var chapterIndex = 0
        var currentPageStart = 1 // PDFBox uses 1-based page numbers
        
        while (currentPageStart <= pageCount) {
            val pageEnd = minOf(currentPageStart + pagesPerChapter - 1, pageCount)
            
            try {
                // Set page range for extraction
                textStripper.startPage = currentPageStart
                textStripper.endPage = pageEnd
                
                val pageText = textStripper.getText(document)
                
                if (pageText.isNotBlank()) {
                    val chapterTitle = if (pageCount <= pagesPerChapter) {
                        "Full Document"
                    } else {
                        "Pages $currentPageStart-$pageEnd"
                    }
                    
                    // Split text into paragraphs for better TTS
                    val paragraphs = splitIntoParagraphs(pageText.trim())
                    val content = paragraphs.map { Text(it) }
                    
                    chapters.add(
                        Chapter(
                            name = chapterTitle,
                            key = "${key}_chapter_$chapterIndex",
                            bookId = bookId,
                            content = content,
                            number = chapterIndex.toFloat(),
                            dateUpload = currentTimeToLong()
                        )
                    )
                    chapterIndex++
                }
            } catch (e: Exception) {
                println("Failed to extract text from pages $currentPageStart-$pageEnd: ${e.message}")
            }
            
            currentPageStart = pageEnd + 1
        }
        
        return chapters
    }
    
    /**
     * Split text into paragraphs for better reading and TTS experience
     */
    private fun splitIntoParagraphs(text: String): List<String> {
        return text
            .replace("\r\n", "\n")
            .split(Regex("\n{2,}|(?<=\\.)\\s{2,}"))
            .map { paragraph ->
                paragraph
                    .replace(Regex("\\s+"), " ")
                    .trim()
            }
            .filter { it.isNotBlank() && it.length > 1 }
    }
    
    /**
     * Calculate optimal pages per chapter based on total page count
     */
    private fun calculatePagesPerChapter(pageCount: Int): Int {
        return when {
            pageCount <= 10 -> pageCount
            pageCount <= 50 -> 5
            pageCount <= 100 -> 10
            pageCount <= 500 -> 20
            else -> 50
        }
    }
    
    private fun generateBookKey(title: String): String {
        val sanitized = title.replace(Regex("[^a-zA-Z0-9]"), "_")
        val timestamp = currentTimeToLong()
        return "pdf_${sanitized}_$timestamp"
    }
    
    actual fun getCacheSize(): String {
        return cacheManager.getCacheSize()
    }
    
    actual fun removeCache() {
        cacheManager.clearAllCache()
    }
}
