package ireader.domain.services.download

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Desktop implementation of DownloadProvider.
 * Uses user home directory for downloads.
 */
class DesktopDownloadProvider : DownloadProvider {
    
    private val defaultDownloadsDir: File by lazy {
        val userHome = System.getProperty("user.home")
        File(userHome, ".ireader/downloads")
    }
    
    private val downloadsDir: File
        get() = defaultDownloadsDir.also { it.mkdirs() }
    
    override fun getDownloadsRoot(): String = downloadsDir.absolutePath
    
    override fun getSourceDirectory(sourceName: String): String {
        val sanitizedSource = sanitizeName(sourceName)
        return File(downloadsDir, sanitizedSource).absolutePath
    }
    
    override fun getBookDirectory(sourceName: String, book: Book): String {
        val sanitizedSource = sanitizeName(sourceName)
        val sanitizedBook = sanitizeName(book.title)
        return File(downloadsDir, "$sanitizedSource/$sanitizedBook").absolutePath
    }
    
    override fun getChapterDirectory(sourceName: String, book: Book, chapter: Chapter): String {
        val sanitizedSource = sanitizeName(sourceName)
        val sanitizedBook = sanitizeName(book.title)
        val sanitizedChapter = sanitizeName(chapter.name)
        return File(downloadsDir, "$sanitizedSource/$sanitizedBook/$sanitizedChapter").absolutePath
    }
    
    override fun getChapterContentFile(sourceName: String, book: Book, chapter: Chapter): String {
        return File(getChapterDirectory(sourceName, book, chapter), DownloadProvider.CONTENT_FILE_NAME).absolutePath
    }
    
    override fun sanitizeName(name: String): String = defaultSanitizeName(name)
    
    override fun getAvailableSpace(): Long {
        return try {
            val path = Paths.get(downloadsDir.absolutePath)
            val store = Files.getFileStore(path)
            store.usableSpace
        } catch (e: Exception) {
            // Fallback to root partition
            try {
                val store = Files.getFileStore(Paths.get("/"))
                store.usableSpace
            } catch (e2: Exception) {
                Long.MAX_VALUE // Assume unlimited if we can't determine
            }
        }
    }
    
    override fun chapterDirectoryExists(sourceName: String, book: Book, chapter: Chapter): Boolean {
        return File(getChapterDirectory(sourceName, book, chapter)).exists()
    }
    
    override fun chapterContentExists(sourceName: String, book: Book, chapter: Chapter): Boolean {
        return File(getChapterContentFile(sourceName, book, chapter)).exists()
    }
    
    override fun createChapterDirectory(sourceName: String, book: Book, chapter: Chapter): Boolean {
        return File(getChapterDirectory(sourceName, book, chapter)).mkdirs()
    }
    
    override fun deleteChapterDirectory(sourceName: String, book: Book, chapter: Chapter): Boolean {
        return File(getChapterDirectory(sourceName, book, chapter)).deleteRecursively()
    }
    
    override fun deleteBookDirectory(sourceName: String, book: Book): Boolean {
        return File(getBookDirectory(sourceName, book)).deleteRecursively()
    }
    
    override fun getDownloadedChapterIds(sourceName: String, book: Book): Set<Long> {
        val bookDir = File(getBookDirectory(sourceName, book))
        if (!bookDir.exists()) return emptySet()
        
        // This is a simplified implementation - in practice, you'd need to map
        // directory names back to chapter IDs, which requires additional metadata
        return emptySet()
    }
    
    override fun getDownloadedBooks(sourceName: String): List<String> {
        val sourceDir = File(getSourceDirectory(sourceName))
        if (!sourceDir.exists()) return emptyList()
        
        return sourceDir.listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?: emptyList()
    }
}
