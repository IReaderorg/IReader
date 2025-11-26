package ireader.domain.usecases.epub

import ireader.core.source.LocalSource
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.Text
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.storage.CacheManager
import ireader.domain.storage.StorageManager
import ireader.domain.usecases.files.GetSimpleStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.buffer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipFile

/**
 * Desktop implementation of EPUB import using pure Kotlin with okio and jsoup
 */
actual class ImportEpub(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val getSimpleStorage: GetSimpleStorage,
    private val cacheManager: CacheManager,
    private val storageManager: StorageManager
) {
    
    actual suspend fun parse(uris: List<ireader.domain.models.common.Uri>) = withContext(Dispatchers.IO) {
        val errors = mutableListOf<Pair<String, String>>()
        
        uris.forEach { uri ->
            try {
                importEpub(uri)
            } catch (e: Exception) {
                errors.add(uri.uriString to (e.message ?: "Unknown error"))
                println("Failed to import ${uri.uriString}: ${e.message}")
            }
        }
        
        if (errors.isNotEmpty()) {
            val errorMessage = errors.joinToString("\n") { (path, error) ->
                "${File(path).name}: $error"
            }
            throw Exception("Failed to import ${errors.size} file(s):\n$errorMessage")
        }
    }
    
    private suspend fun importEpub(uri: ireader.domain.models.common.Uri) {
        val file = File(uri.uriString)
        if (!file.exists()) throw Exception("File not found: ${uri.uriString}")
        
        ZipFile(file).use { zip ->
            // Parse OPF file to get metadata and spine
            val opfEntry = findOpfFile(zip) ?: throw Exception("No OPF file found in EPUB")
            val opfContent = zip.getInputStream(opfEntry).bufferedReader().readText()
            val opfDoc = Jsoup.parse(opfContent, "", org.jsoup.parser.Parser.xmlParser())
            
            // Extract metadata
            val metadata = extractMetadata(opfDoc)
            val title = metadata["title"] ?: file.nameWithoutExtension
            val author = metadata["author"] ?: "Unknown"
            val description = metadata["description"] ?: ""
            
            // Generate unique key
            val key = generateBookKey(title, author)
            bookRepository.delete(key)
            
            // Extract cover
            val coverPath = extractCover(zip, opfDoc, key)
            
            // Create book
            val bookId = Book(
                title = title,
                key = key,
                favorite = true,
                sourceId = LocalSource.SOURCE_ID,
                cover = coverPath,
                author = author,
                status = MangaInfo.PUBLISHING_FINISHED,
                description = description,
                lastUpdate = System.currentTimeMillis()
            ).let { bookRepository.upsert(it) }
            
            // Extract chapters
            val chapters = extractChapters(zip, opfDoc, bookId, key)
            if (chapters.isEmpty()) {
                throw Exception("No readable content found in EPUB")
            }
            
            chapterRepository.insertChapters(chapters)
            println("Successfully imported: $title (${chapters.size} chapters)")
        }
    }
    
    private fun findOpfFile(zip: ZipFile): java.util.zip.ZipEntry? {
        // Read container.xml to find OPF location
        val containerEntry = zip.getEntry("META-INF/container.xml")
        if (containerEntry != null) {
            val containerXml = zip.getInputStream(containerEntry).bufferedReader().readText()
            val doc = Jsoup.parse(containerXml, "", org.jsoup.parser.Parser.xmlParser())
            val rootfile = doc.selectFirst("rootfile[full-path]")
            val opfPath = rootfile?.attr("full-path")
            if (opfPath != null) {
                return zip.getEntry(opfPath)
            }
        }
        
        // Fallback: search for .opf files
        return zip.entries().asSequence().find { it.name.endsWith(".opf") }
    }
    
    private fun extractMetadata(opfDoc: Document): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        
        opfDoc.selectFirst("metadata")?.let { meta ->
            meta.selectFirst("dc|title, title")?.text()?.let { metadata["title"] = it }
            meta.selectFirst("dc|creator, creator")?.text()?.let { metadata["author"] = it }
            meta.selectFirst("dc|description, description")?.text()?.let { metadata["description"] = it }
        }
        
        return metadata
    }
    
    private fun extractCover(zip: ZipFile, opfDoc: Document, key: String): String {
        val cacheDir = cacheManager.getCacheSubDirectory("library_covers")
        val coverFile = File(cacheDir, "$key.jpg")
        
        try {
            // Try to find cover image reference
            val coverItem = opfDoc.select("item[properties*=cover-image], item[id=cover], item[id=cover-image]").firstOrNull()
            val coverHref = coverItem?.attr("href")
            
            if (coverHref != null) {
                val opfPath = findOpfFile(zip)?.name ?: ""
                val basePath = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") + "/" else ""
                val fullPath = basePath + coverHref
                
                zip.getEntry(fullPath)?.let { entry ->
                    zip.getInputStream(entry).use { input ->
                        coverFile.writeBytes(input.readBytes())
                    }
                }
            }
        } catch (e: Exception) {
            println("Failed to extract cover: ${e.message}")
        }
        
        return coverFile.absolutePath
    }
    
    private fun extractChapters(zip: ZipFile, opfDoc: Document, bookId: Long, key: String): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val opfPath = findOpfFile(zip)?.name ?: ""
        val basePath = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") + "/" else ""
        
        // Get spine items
        val spineItems = opfDoc.select("spine itemref")
        val manifestItems = opfDoc.select("manifest item").associateBy { it.attr("id") }
        
        spineItems.forEachIndexed { index, itemref ->
            val idref = itemref.attr("idref")
            val manifestItem = manifestItems[idref]
            val href = manifestItem?.attr("href") ?: return@forEachIndexed
            
            val fullPath = basePath + href
            val entry = zip.getEntry(fullPath) ?: return@forEachIndexed
            
            try {
                val html = zip.getInputStream(entry).bufferedReader().readText()
                val doc = Jsoup.parse(html)
                
                // Extract title
                val title = doc.selectFirst("h1, h2, h3, title")?.text()?.trim()
                    ?: "Chapter ${index + 1}"
                
                // Extract content
                val content = extractTextContent(doc)
                
                if (content.isNotEmpty()) {
                    chapters.add(
                        Chapter(
                            name = title,
                            key = "${key}_chapter_$index",
                            bookId = bookId,
                            content = content,
                            number = index.toFloat(),
                            dateUpload = System.currentTimeMillis()
                        )
                    )
                }
            } catch (e: Exception) {
                println("Failed to extract chapter $index: ${e.message}")
            }
        }
        
        return chapters
    }
    
    private fun extractTextContent(doc: Document): List<Text> {
        val textList = mutableListOf<Text>()
        
        doc.select("h1, h2, h3, h4, h5, h6, p, blockquote, pre, li").forEach { element ->
            val text = element.text().trim()
            if (text.isNotBlank()) {
                textList.add(Text(text))
            }
        }
        
        return textList.ifEmpty {
            listOf(Text(doc.body().text()))
        }
    }
    
    private fun generateBookKey(title: String, author: String): String {
        val sanitized = "${title}_${author}".replace(Regex("[^a-zA-Z0-9]"), "_")
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
        return "${sanitized}_$timestamp"
    }

    actual fun getCacheSize(): String {
        return cacheManager.getCacheSize()
    }

    actual fun removeCache() {
        return cacheManager.clearAllCache()
    }
}