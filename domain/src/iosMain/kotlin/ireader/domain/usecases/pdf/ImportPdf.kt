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
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.stringWithContentsOfFile
import platform.PDFKit.PDFDocument
import platform.PDFKit.PDFPage
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * iOS PDF import implementation using PDFKit
 * 
 * Extracts text from PDF files and creates Book/Chapter entities
 * that can be read and used with TTS.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)
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
                println("[ImportPdf] Failed to import PDF $filePath: ${e.message}")
            }
        }
        
        if (errors.isNotEmpty()) {
            val errorMessage = errors.joinToString("\n") { (path, error) ->
                "${path.substringAfterLast("/")}: $error"
            }
            throw Exception("Failed to import ${errors.size} PDF file(s):\n$errorMessage")
        }
    }
    
    private suspend fun importPdf(uri: Uri) {
        val pdfUrl = NSURL.fileURLWithPath(uri.path)
        val document = PDFDocument(pdfUrl) ?: throw Exception("Failed to open PDF file")
        
        val pageCount = document.pageCount.toInt()
        if (pageCount == 0) {
            throw Exception("PDF has no pages")
        }
        
        // Extract title from filename
        val title = uri.path.substringAfterLast("/").removeSuffix(".pdf").removeSuffix(".PDF")
        
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
            author = "PDF Import",
            status = MangaInfo.UNKNOWN,
            description = "Imported from PDF ($pageCount pages)",
            lastUpdate = currentTimeToLong()
        ).let { bookRepository.upsert(it) }
        
        // Extract chapters
        val chapters = extractChapters(document, bookId, key, pageCount)
        
        if (chapters.isEmpty()) {
            throw Exception("No readable content found in PDF")
        }
        
        chapterRepository.insertChapters(chapters)
        println("[ImportPdf] Successfully imported PDF: $title (${chapters.size} chapters from $pageCount pages)")
    }
    
    /**
     * Extract chapters from PDF pages using PDFKit text extraction
     */
    private fun extractChapters(
        document: PDFDocument,
        bookId: Long,
        key: String,
        pageCount: Int
    ): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val pagesPerChapter = calculatePagesPerChapter(pageCount)
        
        var chapterIndex = 0
        var currentPageStart = 0 // PDFKit uses 0-based page numbers
        
        while (currentPageStart < pageCount) {
            val pageEnd = minOf(currentPageStart + pagesPerChapter, pageCount)
            val pageTexts = mutableListOf<String>()
            
            // Extract text from pages in this chapter
            for (pageNum in currentPageStart until pageEnd) {
                val page: PDFPage? = document.pageAtIndex(pageNum.toLong())
                val pageText = page?.string
                
                if (!pageText.isNullOrBlank()) {
                    pageTexts.add(pageText.trim())
                }
            }
            
            if (pageTexts.isNotEmpty()) {
                val chapterTitle = if (pageCount <= pagesPerChapter) {
                    "Full Document"
                } else {
                    "Pages ${currentPageStart + 1}-$pageEnd"
                }
                
                // Split text into paragraphs for better TTS
                val paragraphs = pageTexts.flatMap { text ->
                    splitIntoParagraphs(text)
                }
                
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
            
            currentPageStart = pageEnd
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
    
    private fun currentTimeToLong(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
    
    actual fun getCacheSize(): String {
        return cacheManager.getCacheSize()
    }
    
    actual fun removeCache() {
        cacheManager.clearAllCache()
    }
}
