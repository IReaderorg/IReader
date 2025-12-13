package ireader.domain.epub
import ireader.domain.utils.extensions.ioDispatcher

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import ireader.core.log.Log
import ireader.core.source.model.Text
import ireader.core.util.IO
import ireader.core.util.randomUUID
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.epub.EpubChapter
import ireader.domain.models.epub.EpubMetadata
import ireader.domain.models.epub.ExportOptions
import ireader.domain.usecases.epub.HtmlContentCleaner
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.domain.utils.extensions.formatIsoDate
import ireader.domain.utils.extensions.formatIsoDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.buffer
import okio.use

/**
 * EPUB 3.0 Builder that creates valid EPUB files with proper structure,
 * metadata, and formatting options.
 * Uses Okio for KMP-compatible file operations.
 */
class EpubBuilder(
    private val httpClient: HttpClient,
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    
    /**
     * Creates an EPUB file from a book and its chapters
     * 
     * @param book The book to export
     * @param chapters The chapters to include
     * @param options Export configuration options
     * @param outputUri The destination file path
     * @param tempDirPath Optional custom temp directory path (for Android compatibility)
     * @param translationsMap Optional map of chapter ID to translated chapter for translated export
     * @return Result containing the file path or an error
     */
    suspend fun createEpub(
        book: Book,
        chapters: List<Chapter>,
        options: ExportOptions,
        outputUri: String,
        tempDirPath: String? = null,
        translationsMap: Map<Long, ireader.domain.models.entities.TranslatedChapter> = emptyMap()
    ): Result<String> = withContext(ioDispatcher) {
        try {
            // Create temporary directory for EPUB structure
            val tempDir = createTempDirectory(tempDirPath)
            
            // Build EPUB structure
            createEpubStructure(tempDir)
            createMimetypeFile(tempDir)
            createContainerXml(tempDir)
            
            // Process chapters (with optional translated content)
            val epubChapters = processChapters(chapters, options, translationsMap)
            
            // Generate metadata
            val metadata = createMetadata(book)
            
            // Create EPUB files
            createContentOpf(tempDir, book, metadata, epubChapters, options)
            createTocNcx(tempDir, metadata, epubChapters)
            createNavDocument(tempDir, epubChapters)
            createChapterFiles(tempDir, epubChapters, options)
            
            // Download and embed cover image if requested
            if (options.includeCover && book.cover.isNotEmpty()) {
                downloadAndEmbedCover(tempDir, book.cover)
            }
            
            // Package as ZIP
            val epubFile = packageAsEpub(tempDir, outputUri)
            
            // Cleanup temp directory
            cleanupTempDirectory(tempDir)
            
            Result.success(epubFile)
        } catch (e: Exception) {
            Log.error("EPUB creation failed", e)
            Result.failure(e)
        }
    }
    
    private fun createTempDirectory(customPath: String? = null): Path {
        val basePath = customPath ?: FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toString()
        val tempDir = "$basePath/epub_${randomUUID()}".toPath()
        if (!fileSystem.exists(tempDir)) {
            fileSystem.createDirectories(tempDir)
        }
        return tempDir
    }
    
    private fun createEpubStructure(baseDir: Path) {
        fileSystem.createDirectories(baseDir / "META-INF")
        fileSystem.createDirectories(baseDir / "OEBPS")
        fileSystem.createDirectories(baseDir / "OEBPS" / "Text")
        fileSystem.createDirectories(baseDir / "OEBPS" / "Styles")
        fileSystem.createDirectories(baseDir / "OEBPS" / "Images")
    }
    
    private fun createMimetypeFile(baseDir: Path) {
        fileSystem.sink(baseDir / "mimetype").buffer().use { it.writeUtf8("application/epub+zip") }
    }
    
    private fun createContainerXml(baseDir: Path) {
        val containerXml = """<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
    <rootfiles>
        <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
    </rootfiles>
</container>"""
        fileSystem.sink(baseDir / "META-INF" / "container.xml").buffer().use { it.writeUtf8(containerXml) }
    }
    
    private fun createMetadata(book: Book): EpubMetadata {
        val currentDate = currentTimeToLong().formatIsoDate()
        return EpubMetadata(
            title = book.title,
            author = book.author.ifEmpty { "Unknown" },
            identifier = "urn:uuid:${randomUUID()}",
            date = currentDate,
            description = book.description.takeIf { it.isNotBlank() }
        )
    }
    
    private fun processChapters(
        chapters: List<Chapter>, 
        options: ExportOptions,
        translationsMap: Map<Long, ireader.domain.models.entities.TranslatedChapter> = emptyMap()
    ): List<EpubChapter> {
        val selectedChapters = if (options.selectedChapters.isEmpty()) {
            chapters
        } else {
            chapters.filter { it.id in options.selectedChapters }
        }
        
        return selectedChapters.mapIndexed { index, chapter ->
            // Use translated content if available and requested
            val contentPages = if (options.useTranslatedContent && translationsMap.containsKey(chapter.id)) {
                translationsMap[chapter.id]?.translatedContent ?: chapter.content
            } else {
                chapter.content
            }
            
            val content = contentPages.mapNotNull {
                when (it) {
                    is Text -> it.text
                    else -> null
                }
            }.joinToString("\n\n")
            
            EpubChapter(
                id = "chapter${index}",
                title = chapter.name,
                content = content,
                order = index,
                fileName = "Text/chapter${index}.xhtml"
            )
        }
    }
    
    private fun createContentOpf(
        baseDir: Path,
        book: Book,
        metadata: EpubMetadata,
        chapters: List<EpubChapter>,
        options: ExportOptions
    ) {
        val currentDate = currentTimeToLong().formatIsoDateTime()
        
        val contentOpf = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<package version="3.0" xmlns="http://www.idpf.org/2007/opf" unique-identifier="BookId">""")
            
            // Metadata section
            appendLine("  <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\">")
            appendLine("    <dc:title>${metadata.title.escapeXml()}</dc:title>")
            appendLine("    <dc:creator>${metadata.author.escapeXml()}</dc:creator>")
            appendLine("    <dc:language>${metadata.language}</dc:language>")
            appendLine("    <dc:identifier id=\"BookId\">${metadata.identifier}</dc:identifier>")
            appendLine("    <dc:publisher>${metadata.publisher}</dc:publisher>")
            appendLine("    <dc:date>${metadata.date}</dc:date>")
            if (metadata.description != null) {
                appendLine("    <dc:description>${metadata.description.escapeXml()}</dc:description>")
            }
            appendLine("    <meta property=\"dcterms:modified\">$currentDate</meta>")
            // Add cover meta tag for EPUB 2.0 compatibility (required by Google Books)
            if (options.includeCover && book.cover.isNotEmpty()) {
                appendLine("    <meta name=\"cover\" content=\"cover-image\"/>")
            }
            appendLine("  </metadata>")
            
            // Manifest section
            appendLine("  <manifest>")
            appendLine("    <item id=\"nav\" href=\"nav.xhtml\" media-type=\"application/xhtml+xml\" properties=\"nav\"/>")
            appendLine("    <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>")
            appendLine("    <item id=\"style\" href=\"Styles/style.css\" media-type=\"text/css\"/>")
            chapters.forEach { chapter ->
                appendLine("    <item id=\"${chapter.id}\" href=\"${chapter.fileName}\" media-type=\"application/xhtml+xml\"/>")
            }
            if (options.includeCover && book.cover.isNotEmpty()) {
                // Determine media type from cover URL
                val (extension, mediaType) = when {
                    book.cover.contains(".png", ignoreCase = true) -> "png" to "image/png"
                    book.cover.contains(".jpeg", ignoreCase = true) -> "jpeg" to "image/jpeg"
                    book.cover.contains(".jpg", ignoreCase = true) -> "jpg" to "image/jpeg"
                    book.cover.contains(".gif", ignoreCase = true) -> "gif" to "image/gif"
                    book.cover.contains(".webp", ignoreCase = true) -> "webp" to "image/webp"
                    else -> "jpg" to "image/jpeg"
                }
                appendLine("    <item id=\"cover-image\" href=\"Images/cover.$extension\" media-type=\"$mediaType\" properties=\"cover-image\"/>")
            }
            appendLine("  </manifest>")
            
            // Spine section
            appendLine("  <spine toc=\"ncx\">")
            appendLine("    <itemref idref=\"nav\"/>")
            chapters.forEach { chapter ->
                appendLine("    <itemref idref=\"${chapter.id}\"/>")
            }
            appendLine("  </spine>")
            
            appendLine("</package>")
        }
        
        fileSystem.sink(baseDir / "OEBPS" / "content.opf").buffer().use { it.writeUtf8(contentOpf) }
    }
    
    private fun createTocNcx(baseDir: Path, metadata: EpubMetadata, chapters: List<EpubChapter>) {
        val tocNcx = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<!DOCTYPE ncx PUBLIC "-//NISO//DTD ncx 2005-1//EN" "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd">""")
            appendLine("""<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">""")
            appendLine("  <head>")
            appendLine("    <meta name=\"dtb:uid\" content=\"${metadata.identifier}\"/>")
            appendLine("    <meta name=\"dtb:depth\" content=\"1\"/>")
            appendLine("    <meta name=\"dtb:totalPageCount\" content=\"0\"/>")
            appendLine("    <meta name=\"dtb:maxPageNumber\" content=\"0\"/>")
            appendLine("  </head>")
            appendLine("  <docTitle>")
            appendLine("    <text>${metadata.title.escapeXml()}</text>")
            appendLine("  </docTitle>")
            appendLine("  <navMap>")
            
            chapters.forEachIndexed { index, chapter ->
                appendLine("    <navPoint id=\"navPoint-${index + 1}\" playOrder=\"${index + 1}\">")
                appendLine("      <navLabel>")
                appendLine("        <text>${chapter.title.escapeXml()}</text>")
                appendLine("      </navLabel>")
                appendLine("      <content src=\"${chapter.fileName}\"/>")
                appendLine("    </navPoint>")
            }
            
            appendLine("  </navMap>")
            appendLine("</ncx>")
        }
        
        fileSystem.sink(baseDir / "OEBPS" / "toc.ncx").buffer().use { it.writeUtf8(tocNcx) }
    }
    
    private fun createNavDocument(baseDir: Path, chapters: List<EpubChapter>) {
        val navXhtml = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<!DOCTYPE html>""")
            appendLine("""<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops">""")
            appendLine("<head>")
            appendLine("  <meta charset=\"UTF-8\"/>")
            appendLine("  <title>Table of Contents</title>")
            appendLine("  <link rel=\"stylesheet\" type=\"text/css\" href=\"Styles/style.css\"/>")
            appendLine("</head>")
            appendLine("<body>")
            appendLine("  <nav epub:type=\"toc\" id=\"toc\">")
            appendLine("    <h1>Table of Contents</h1>")
            appendLine("    <ol>")
            
            chapters.forEach { chapter ->
                appendLine("      <li><a href=\"${chapter.fileName}\">${chapter.title.escapeXml()}</a></li>")
            }
            
            appendLine("    </ol>")
            appendLine("  </nav>")
            appendLine("</body>")
            appendLine("</html>")
        }
        
        fileSystem.sink(baseDir / "OEBPS" / "nav.xhtml").buffer().use { it.writeUtf8(navXhtml) }
    }
    
    private fun createChapterFiles(
        baseDir: Path,
        chapters: List<EpubChapter>,
        options: ExportOptions
    ) {
        // First create CSS file
        createStylesheet(baseDir, options)
        
        chapters.forEach { chapter ->
            val xhtml = buildString {
                appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
                appendLine("""<!DOCTYPE html>""")
                appendLine("""<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops">""")
                appendLine("<head>")
                appendLine("  <meta charset=\"UTF-8\"/>")
                appendLine("  <title>${chapter.title.escapeXml()}</title>")
                appendLine("  <link rel=\"stylesheet\" type=\"text/css\" href=\"../Styles/style.css\"/>")
                appendLine("</head>")
                appendLine("<body>")
                appendLine("  <section id=\"${chapter.id}\" epub:type=\"chapter\">")
                appendLine("    <h1>${chapter.title.escapeXml()}</h1>")
                
                // Process chapter content
                val processedContent = processChapterContent(chapter.content, options)
                appendLine(processedContent)
                
                appendLine("  </section>")
                appendLine("</body>")
                appendLine("</html>")
            }
            
            fileSystem.sink(baseDir / "OEBPS" / chapter.fileName).buffer().use { it.writeUtf8(xhtml) }
        }
    }
    
    private fun createStylesheet(baseDir: Path, options: ExportOptions) {
        val css = """
body {
    font-family: ${options.fontFamily};
    font-size: ${options.fontSize}px;
    line-height: 1.6;
    margin: 1em;
    text-align: justify;
}

h1 {
    font-size: ${options.fontSize * options.chapterHeadingSize}px;
    font-weight: bold;
    margin-top: 1em;
    margin-bottom: 0.5em;
    text-align: center;
}

p {
    margin-bottom: ${options.paragraphSpacing}em;
    text-indent: 1.5em;
}

p:first-of-type {
    text-indent: 0;
}

section {
    page-break-after: always;
}

img {
    max-width: 100%;
    height: auto;
}
""".trimIndent()
        
        fileSystem.sink(baseDir / "OEBPS" / "Styles" / "style.css").buffer().use { it.writeUtf8(css) }
    }
    
    private fun processChapterContent(content: String, options: ExportOptions): String {
        // Clean HTML content if needed
        val cleanedContent = if (HtmlContentCleaner.isHtml(content)) {
            HtmlContentCleaner.extractPlainText(content)
        } else {
            content
        }
        
        // Split into paragraphs and wrap in <p> tags
        return cleanedContent
            .split("\n\n")
            .filter { it.isNotBlank() }
            .joinToString("\n") { paragraph ->
                val trimmed = paragraph.trim()
                if (trimmed.isNotBlank()) {
                    "    <p>${trimmed.escapeXml()}</p>"
                } else {
                    ""
                }
            }
    }
    
    private fun packageAsEpub(sourceDir: Path, outputUri: String): String {
        Log.info { "packageAsEpub called with outputUri: $outputUri" }
        
        // Handle both file:// URIs and absolute paths
        val outputPath = if (outputUri.startsWith("file://")) {
            outputUri.removePrefix("file://")
                .replace("%20", " ") // Decode URL-encoded spaces
        } else if (outputUri.startsWith("content://")) {
            // Content URIs cannot be used as file paths
            throw IllegalArgumentException("Content URIs must be handled by the caller. Use a temp file path instead. Got: $outputUri")
        } else {
            outputUri
        }
        
        Log.info { "Resolved output path: $outputPath" }
        val outputFile = outputPath.toPath()
        
        // Ensure parent directory exists
        outputFile.parent?.let { parent ->
            if (!fileSystem.exists(parent)) {
                fileSystem.createDirectories(parent)
            }
        }
        
        // Use platform-specific ZIP creation
        val entries = collectZipEntries(sourceDir)
        val zipBytes = createEpubZipPlatform(entries)
        fileSystem.sink(outputFile).buffer().use { it.write(zipBytes) }
        
        return outputPath
    }
    
    private fun collectZipEntries(sourceDir: Path): List<EpubZipEntry> {
        val entries = mutableListOf<EpubZipEntry>()
        
        // Add mimetype first (uncompressed)
        val mimetypeContent = fileSystem.source(sourceDir / "mimetype").buffer().use { it.readByteArray() }
        entries.add(EpubZipEntry("mimetype", mimetypeContent, compressed = false))
        
        // Add META-INF directory
        collectDirectoryEntries(sourceDir / "META-INF", "META-INF", entries)
        
        // Add OEBPS directory
        collectDirectoryEntries(sourceDir / "OEBPS", "OEBPS", entries)
        
        return entries
    }
    
    private fun collectDirectoryEntries(dir: Path, basePath: String, entries: MutableList<EpubZipEntry>) {
        if (!fileSystem.exists(dir)) return
        
        fileSystem.list(dir).forEach { file ->
            val entryName = "$basePath/${file.name}"
            val metadata = fileSystem.metadata(file)
            if (metadata.isDirectory) {
                collectDirectoryEntries(file, entryName, entries)
            } else {
                val content = fileSystem.source(file).buffer().use { it.readByteArray() }
                entries.add(EpubZipEntry(entryName, content))
            }
        }
    }
    
    /**
     * Data class for ZIP entries
     */
    data class EpubZipEntry(
        val path: String,
        val content: ByteArray,
        val compressed: Boolean = true
    )
    
    private suspend fun downloadAndEmbedCover(baseDir: Path, coverUrl: String) {
        try {
            Log.info { "Downloading cover image from: $coverUrl" }
            
            // Download the cover image
            val response = httpClient.get(coverUrl)
            val imageBytes = response.readBytes()
            
            // Determine file extension from URL or default to jpg
            val extension = when {
                coverUrl.contains(".png", ignoreCase = true) -> "png"
                coverUrl.contains(".jpeg", ignoreCase = true) -> "jpeg"
                coverUrl.contains(".jpg", ignoreCase = true) -> "jpg"
                coverUrl.contains(".gif", ignoreCase = true) -> "gif"
                coverUrl.contains(".webp", ignoreCase = true) -> "webp"
                else -> "jpg"
            }
            
            // Save the cover image
            val coverFile = baseDir / "OEBPS" / "Images" / "cover.$extension"
            fileSystem.sink(coverFile).buffer().use { it.write(imageBytes) }
            
            Log.info { "Cover image downloaded successfully: ${imageBytes.size} bytes" }
        } catch (e: Exception) {
            Log.warn { "Failed to download cover image: ${e.message}" }
            // Don't fail the entire export if cover download fails
        }
    }
    
    private fun cleanupTempDirectory(tempDir: Path) {
        fileSystem.deleteRecursively(tempDir)
    }
    
    private fun String.escapeXml(): String {
        return this
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}


/**
 * Platform-specific ZIP creation for EPUB files.
 * Implementations should handle proper ZIP format with mimetype uncompressed first.
 */
expect fun createEpubZipPlatform(entries: List<EpubBuilder.EpubZipEntry>): ByteArray
