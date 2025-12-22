package ireader.domain.usecases.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import ireader.core.source.LocalSource
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.Text
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.storage.CacheManager
import ireader.domain.storage.StorageManager
import ireader.domain.usecases.file.FileSaver
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.buffer
import java.io.File

/**
 * Android PDF import implementation
 * 
 * Uses Android's PdfRenderer to open PDF files and extract basic information.
 * For text extraction, this implementation provides page-based chapters.
 * 
 * Note: Android's PdfRenderer doesn't support direct text extraction.
 * For full text extraction, consider using:
 * - ML Kit for OCR on rendered bitmaps
 * - Apache PDFBox Android port (com.tom-roush:pdfbox-android)
 * - iText for Android
 * 
 * This basic implementation creates chapters per page group with placeholder text
 * that can be enhanced with OCR or a PDF text extraction library.
 */
actual class ImportPdf(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val fileSaver: FileSaver,
    private val cacheManager: CacheManager,
    private val storageManager: StorageManager,
    context: Context
) {
    private val appContext: Context = context.applicationContext
    
    actual suspend fun parse(uris: List<ireader.domain.models.common.Uri>) = withContext(Dispatchers.IO) {
        val errors = mutableListOf<Pair<String, String>>()
        
        uris.forEach { uri ->
            try {
                importPdf(uri)
            } catch (e: Exception) {
                val filePath = uri.androidUri.path ?: uri.toString()
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
    
    private suspend fun importPdf(uri: ireader.domain.models.common.Uri) {
        val tempFile = File(appContext.cacheDir, "temp_pdf_${currentTimeToLong()}.pdf")
        
        try {
            // Copy content to temp file
            fileSaver.readSource(uri).buffer().use { source ->
                FileSystem.SYSTEM.sink(tempFile.toOkioPath()).buffer().use { sink ->
                    sink.writeAll(source)
                }
            }
            
            if (!tempFile.exists() || tempFile.length() == 0L) {
                throw Exception("Failed to read PDF file")
            }
            
            // Open PDF with PdfRenderer
            val fileDescriptor = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)
            
            try {
                val pageCount = pdfRenderer.pageCount
                if (pageCount == 0) {
                    throw Exception("PDF has no pages")
                }
                
                // Extract title from filename
                val fileName = uri.androidUri.lastPathSegment?.substringAfterLast("/")
                    ?: tempFile.nameWithoutExtension
                val title = fileName.removeSuffix(".pdf").removeSuffix(".PDF")
                
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
                    description = "Imported from PDF ($pageCount pages)\n\nNote: PDF text extraction requires additional setup. " +
                            "This import creates page-based chapters for navigation.",
                    lastUpdate = currentTimeToLong()
                ).let { bookRepository.upsert(it) }
                
                // Extract chapters (group pages for better reading experience)
                val chapters = extractChapters(pdfRenderer, bookId, key, pageCount)
                
                if (chapters.isEmpty()) {
                    throw Exception("No content could be extracted from PDF")
                }
                
                chapterRepository.insertChapters(chapters)
                println("Successfully imported PDF: $title (${chapters.size} chapters from $pageCount pages)")
                
            } finally {
                pdfRenderer.close()
                fileDescriptor.close()
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }
    
    /**
     * Extract chapters from PDF pages
     * 
     * Groups pages into chapters for better reading experience.
     * Each chapter contains information about the pages it covers.
     */
    private fun extractChapters(
        pdfRenderer: PdfRenderer,
        bookId: Long,
        key: String,
        pageCount: Int
    ): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val pagesPerChapter = calculatePagesPerChapter(pageCount)
        
        var chapterIndex = 0
        var currentPageStart = 0
        
        while (currentPageStart < pageCount) {
            val pageEnd = minOf(currentPageStart + pagesPerChapter, pageCount)
            val pageInfos = mutableListOf<String>()
            
            // Get page information
            for (pageNum in currentPageStart until pageEnd) {
                val page = pdfRenderer.openPage(pageNum)
                try {
                    val width = page.width
                    val height = page.height
                    pageInfos.add("Page ${pageNum + 1} (${width}x${height})")
                } finally {
                    page.close()
                }
            }
            
            val chapterTitle = if (pageCount <= pagesPerChapter) {
                "Full Document"
            } else {
                "Pages ${currentPageStart + 1}-$pageEnd"
            }
            
            // Create content with page information
            // In a full implementation, this would contain extracted text
            val content = listOf(
                Text("$chapterTitle\n"),
                Text("This PDF chapter covers pages ${currentPageStart + 1} to $pageEnd.\n"),
                Text("Page details:\n${pageInfos.joinToString("\n")}\n"),
                Text("\n[PDF text extraction requires additional library integration]\n"),
                Text("To enable full text extraction and TTS, consider:\n"),
                Text("• Using ML Kit for OCR\n"),
                Text("• Adding PDFBox Android library\n"),
                Text("• Using iText PDF library\n")
            )
            
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
            
            currentPageStart = pageEnd
        }
        
        return chapters
    }
    
    /**
     * Calculate optimal pages per chapter based on total page count
     */
    private fun calculatePagesPerChapter(pageCount: Int): Int {
        return when {
            pageCount <= 10 -> pageCount // Single chapter for small PDFs
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
