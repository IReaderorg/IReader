package ireader.domain.epub
import ireader.domain.utils.extensions.ioDispatcher

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import ireader.core.log.Log
import ireader.core.source.model.ImageBase64
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.Text
import ireader.core.util.IO
import ireader.core.util.randomUUID
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.epub.EpubChapter
import ireader.domain.models.epub.EmbeddedImage
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
    private val fileSystem: FileSystem
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
            
            // Process chapters (with optional translated content) - includes image download
            val epubChapters = processChapters(chapters, options, translationsMap)
            
            // Download cover image first to determine actual format
            var coverExtension = "jpg"
            var coverMediaType = "image/jpeg"
            if (options.includeCover && book.cover.isNotEmpty()) {
                val coverResult = downloadAndEmbedCover(tempDir, book.cover)
                if (coverResult != null) {
                    coverExtension = coverResult.first
                    coverMediaType = coverResult.second
                }
            }
            
            // Save embedded chapter images
            epubChapters.forEach { chapter ->
                chapter.images.forEach { image ->
                    val imageFile = tempDir / "OEBPS" / "Images" / image.fileName
                    fileSystem.sink(imageFile).buffer().use { it.write(image.data) }
                }
            }
            
            // Generate metadata
            val metadata = createMetadata(book)
            
            // Create EPUB files
            createContentOpf(tempDir, book, metadata, epubChapters, options, coverExtension, coverMediaType)
            createTocNcx(tempDir, metadata, epubChapters)
            createNavDocument(tempDir, epubChapters)
            createChapterFiles(tempDir, epubChapters, options)
            
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
    
    private suspend fun processChapters(
        chapters: List<Chapter>, 
        options: ExportOptions,
        translationsMap: Map<Long, ireader.domain.models.entities.TranslatedChapter> = emptyMap()
    ): List<EpubChapter> {
        val selectedChapters = if (options.selectedChapters.isEmpty()) {
            chapters
        } else {
            chapters.filter { it.id in options.selectedChapters }
        }
        
        val result = mutableListOf<EpubChapter>()
        
        for ((index, chapter) in selectedChapters.withIndex()) {
            // Use translated content if available and requested
            val contentPages = if (options.useTranslatedContent && translationsMap.containsKey(chapter.id)) {
                translationsMap[chapter.id]?.translatedContent ?: chapter.content
            } else {
                chapter.content
            }
            
            val images = mutableListOf<EmbeddedImage>()
            var imageCounter = 0
            
            val contentParts = mutableListOf<String>()
            for (page in contentPages) {
                when (page) {
                    is Text -> contentParts.add(page.text)
                    is ImageUrl -> {
                        if (options.includeImages) {
                            imageCounter++
                            val fileName = "chapter${index}_img${imageCounter}"
                            try {
                                val response = httpClient.get(page.url)
                                val imageBytes = response.readBytes()
                                val (extension, mediaType) = detectImageFormat(imageBytes)
                                val actualFileName = "$fileName.$extension"
                                images.add(EmbeddedImage(actualFileName, mediaType, imageBytes))
                                contentParts.add("""<img src="../Images/$actualFileName" alt="Image"/>""")
                            } catch (e: Exception) {
                                Log.warn { "Failed to download image from ${page.url}: ${e.message}" }
                            }
                        }
                    }
                    is ImageBase64 -> {
                        if (options.includeImages) {
                            imageCounter++
                            val fileName = "chapter${index}_img${imageCounter}"
                            try {
                                val imageBytes = java.util.Base64.getDecoder().decode(page.data)
                                val (extension, mediaType) = detectImageFormat(imageBytes)
                                val actualFileName = "$fileName.$extension"
                                images.add(EmbeddedImage(actualFileName, mediaType, imageBytes))
                                contentParts.add("""<img src="../Images/$actualFileName" alt="Image"/>""")
                            } catch (e: Exception) {
                                Log.warn { "Failed to decode base64 image: ${e.message}" }
                            }
                        }
                    }
                    else -> {}
                }
            }
            
            result.add(EpubChapter(
                id = "chapter${index}",
                title = chapter.name,
                content = contentParts.joinToString("\n\n"),
                order = index,
                fileName = "Text/chapter${index}.xhtml",
                images = images
            ))
        }
        
        return result
    }
    
    private fun createContentOpf(
        baseDir: Path,
        book: Book,
        metadata: EpubMetadata,
        chapters: List<EpubChapter>,
        options: ExportOptions,
        coverExtension: String = "jpg",
        coverMediaType: String = "image/jpeg"
    ) {
        val currentDate = currentTimeToLong().formatIsoDateTime()
        
        val contentOpf = buildString {
            appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
            appendLine("""<package version="3.0" xmlns="http://www.idpf.org/2007/opf" unique-identifier="BookId">""")
            
            // Metadata section - Enhanced for Google Books/Kindle compatibility
            appendLine("  <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:opf=\"http://www.idpf.org/2007/opf\">")
            
            // Required metadata
            appendLine("    <dc:title>${metadata.title.escapeXml()}</dc:title>")
            appendLine("    <dc:creator opf:role=\"aut\">${metadata.author.escapeXml()}</dc:creator>")
            appendLine("    <dc:language>${metadata.language}</dc:language>")
            appendLine("    <dc:identifier id=\"BookId\">${metadata.identifier}</dc:identifier>")
            
            // Optional but recommended metadata
            appendLine("    <dc:publisher>${metadata.publisher}</dc:publisher>")
            appendLine("    <dc:date>${metadata.date}</dc:date>")
            
            // Description (important for Google Books)
            if (metadata.description != null && metadata.description.isNotBlank()) {
                appendLine("    <dc:description>${metadata.description.escapeXml()}</dc:description>")
            } else {
                // Provide default description if none exists
                appendLine("    <dc:description>Exported from IReader</dc:description>")
            }
            
            // Generator (helps identify source)
            appendLine("    <meta property=\"dcterms:generator\">IReader EPUB Exporter</meta>")
            
            // Modified date (required for EPUB 3.0)
            appendLine("    <meta property=\"dcterms:modified\">$currentDate</meta>")
            
            // Cover meta tag for EPUB 2.0 compatibility (required by Google Books)
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
                appendLine("    <item id=\"cover-image\" href=\"Images/cover.$coverExtension\" media-type=\"$coverMediaType\" properties=\"cover-image\"/>")
            }
            // Embed chapter images in manifest
            val allImages = chapters.flatMap { it.images }
            allImages.forEach { image ->
                appendLine("    <item id=\"${image.fileName}\" href=\"Images/${image.fileName}\" media-type=\"${image.mediaType}\"/>")
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
        // Clean HTML content if needed, but preserve <img> tags
        val cleanedContent = if (HtmlContentCleaner.isHtml(content)) {
            HtmlContentCleaner.clean(content)
        } else {
            content
        }
        
        // Split into paragraphs and wrap in <p> tags, preserving <img> tags
        return cleanedContent
            .split("\n\n")
            .filter { it.isNotBlank() }
            .joinToString("\n") { paragraph ->
                val trimmed = paragraph.trim()
                if (trimmed.isNotBlank()) {
                    // Don't wrap <img> tags in <p> - they're block-level in EPUB
                    if (trimmed.startsWith("<img")) {
                        "    $trimmed"
                    } else {
                        "    <p>${trimmed}</p>"
                    }
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
    
    /**
     * Downloads and embeds the cover image.
     * @return Pair of (extension, mediaType) if successful, null otherwise
     */
    private suspend fun downloadAndEmbedCover(baseDir: Path, coverUrl: String): Pair<String, String>? {
        try {
            Log.info { "Downloading cover image from: $coverUrl" }
            
            // Validate URL
            if (coverUrl.isBlank() || !coverUrl.startsWith("http", ignoreCase = true)) {
                Log.warn { "Invalid cover URL: $coverUrl" }
                return null
            }
            
            // Download the cover image with timeout
            val response = httpClient.get(coverUrl)
            val imageBytes = response.readBytes()
            
            // Validate image size (max 10MB for compatibility)
            if (imageBytes.size > 10 * 1024 * 1024) {
                Log.warn { "Cover image too large: ${imageBytes.size} bytes, skipping" }
                return null
            }
            
            // Validate minimum size (at least 1KB)
            if (imageBytes.size < 1024) {
                Log.warn { "Cover image too small: ${imageBytes.size} bytes, might be invalid" }
            }
            
            // Determine file extension from actual image bytes (magic bytes)
            val (extension, mediaType) = detectImageFormat(imageBytes)
            
            // Save the cover image
            val coverFile = baseDir / "OEBPS" / "Images" / "cover.$extension"
            fileSystem.sink(coverFile).buffer().use { it.write(imageBytes) }
            
            Log.info { "Cover image downloaded successfully: ${imageBytes.size} bytes as $extension" }
            return extension to mediaType
        } catch (e: Exception) {
            Log.warn { "Failed to download cover image from $coverUrl: ${e.message}" }
            // Don't fail the entire export if cover download fails
            // This ensures export works even with broken cover URLs
            return null
        }
    }
    
    private fun cleanupTempDirectory(tempDir: Path) {
        fileSystem.deleteRecursively(tempDir)
    }
    
    /**
     * Detect image format from byte array magic bytes.
     * Returns (extension, mimeType) tuple.
     */
    private fun detectImageFormat(data: ByteArray): Pair<String, String> {
        if (data.size < 4) return "jpg" to "image/jpeg"
        
        // JPEG: FF D8 FF
        if (data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() && data[2] == 0xFF.toByte()) {
            return "jpg" to "image/jpeg"
        }
        
        // PNG: 89 50 4E 47
        if (data[0] == 0x89.toByte() && data[1] == 0x50.toByte() && 
            data[2] == 0x4E.toByte() && data[3] == 0x47.toByte()) {
            return "png" to "image/png"
        }
        
        // GIF: 47 49 46 38
        if (data[0] == 0x47.toByte() && data[1] == 0x49.toByte() && 
            data[2] == 0x46.toByte() && data[3] == 0x38.toByte()) {
            return "gif" to "image/gif"
        }
        
        // WebP: RIFF....WEBP
        if (data.size >= 12 && 
            data[0] == 0x52.toByte() && data[1] == 0x49.toByte() && 
            data[2] == 0x46.toByte() && data[3] == 0x46.toByte() &&
            data[8] == 0x57.toByte() && data[9] == 0x45.toByte() &&
            data[10] == 0x42.toByte() && data[11] == 0x50.toByte()) {
            return "webp" to "image/webp"
        }
        
        // Default to JPEG
        return "jpg" to "image/jpeg"
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
