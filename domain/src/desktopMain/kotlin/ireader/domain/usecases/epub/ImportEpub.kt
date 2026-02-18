package ireader.domain.usecases.epub

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.parser.Parser
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
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import java.io.File
import java.util.zip.ZipFile

/**
 * Desktop implementation of EPUB import using pure Kotlin with okio and ksoup
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
            }
        }
        
        if (errors.isNotEmpty()) {
            val errorMessage = errors.joinToString("\n") { (path, error) ->
                "${path.toPath().name}: $error"
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
            val opfDoc = Ksoup.parse(opfContent, Parser.xmlParser())
            
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
                lastUpdate = currentTimeToLong()
            ).let { bookRepository.upsert(it) }
            
            // Extract chapters
            val chapters = extractChapters(zip, opfDoc, bookId, key)
            if (chapters.isEmpty()) {
                throw Exception("No readable content found in EPUB")
            }
            
            chapterRepository.insertChapters(chapters)
        }
    }
    
    private fun findOpfFile(zip: ZipFile): java.util.zip.ZipEntry? {
        // Read container.xml to find OPF location
        val containerEntry = zip.getEntry("META-INF/container.xml")
        if (containerEntry != null) {
            val containerXml = zip.getInputStream(containerEntry).bufferedReader().readText()
            val doc = Ksoup.parse(containerXml, Parser.xmlParser())
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
        val coverFile = cacheDir / "$key.jpg"
        
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
                        FileSystem.SYSTEM.sink(coverFile).buffer().use { sink ->
                            sink.write(input.readBytes())
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Silently ignore cover extraction errors
        }
        
        return coverFile.toString()
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
                val doc = Ksoup.parse(html)
                
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
                            dateUpload = currentTimeToLong()
                        )
                    )
                }
            } catch (_: Exception) {
                // Silently ignore chapter extraction errors
            }
        }
        
        return chapters
    }
    
    private fun extractTextContent(doc: Document): List<Text> {
        val textList = mutableListOf<Text>()
        
        // Get the body element
        val body = doc.body() ?: return emptyList()
        
        // Traverse all child nodes (including text nodes)
        fun traverseNodes(node: com.fleeksoft.ksoup.nodes.Node) {
            when (node) {
                is com.fleeksoft.ksoup.nodes.TextNode -> {
                    // Extract text from text nodes
                    val text = node.text().trim()
                    if (text.isNotBlank()) {
                        textList.add(Text(text))
                    }
                }
                is com.fleeksoft.ksoup.nodes.Element -> {
                    // For block-level elements, process their children
                    if (node.tagName() in listOf("h1", "h2", "h3", "h4", "h5", "h6", "p", "div", "blockquote", "pre", "li", "body")) {
                        node.childNodes().forEach { traverseNodes(it) }
                    } else {
                        // For inline elements, get their text content
                        val text = node.text().trim()
                        if (text.isNotBlank()) {
                            textList.add(Text(text))
                        }
                    }
                }
            }
        }
        
        // Start traversal from body
        body.childNodes().forEach { traverseNodes(it) }
        
        return textList.ifEmpty {
            // Fallback: get all text from body
            val bodyText = body.text().trim()
            if (bodyText.isNotBlank()) {
                listOf(Text(bodyText))
            } else {
                emptyList()
            }
        }
    }
    
    private fun generateBookKey(title: String, author: String): String {
        val sanitized = "${title}_${author}".replace(Regex("[^a-zA-Z0-9]"), "_")
        val timestamp = currentTimeToLong()
        return "${sanitized}_$timestamp"
    }

    actual fun getCacheSize(): String {
        return cacheManager.getCacheSize()
    }

    actual fun removeCache() {
        return cacheManager.clearAllCache()
    }
}