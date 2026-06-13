package ireader.data.epub

import ireader.core.log.Log
import ireader.core.source.model.ImageUrl
import ireader.core.util.randomUUID
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.services.epub.EpubExportOptions
import ireader.domain.services.epub.EpubExportService
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.ExperimentalTime

/**
 * Implementation of EpubExportService that generates EPUB files with beautified formatting.
 * 
 * This implementation creates valid EPUB 2.0 files with:
 * - Proper container structure (mimetype, META-INF, OEBPS)
 * - Metadata in OPF format
 * - Table of contents in NCX format
 * - Styled XHTML chapter files
 * - Professional CSS formatting
 */
class EpubExportServiceImpl(
    private val chapterRepository: ChapterRepository,
    private val fileProvider: EpubFileProvider
) : EpubExportService {
    
    override suspend fun exportBook(
        book: Book,
        chapters: List<Chapter>,
        options: EpubExportOptions,
        onProgress: (Float, String) -> Unit
    ): Result<Uri> = withContext(ioDispatcher) {
        try {
            onProgress(0.1f, "Preparing export...")
            
            // Validate chapters
            if (chapters.isEmpty()) {
                return@withContext Result.failure(Exception("No chapters to export"))
            }
            
            // Fetch chapter content
            onProgress(0.2f, "Loading chapter content...")
            val chaptersWithContent = fetchChapterContent(chapters, onProgress)
            
            if (chaptersWithContent.isEmpty()) {
                return@withContext Result.failure(Exception("No chapter content available"))
            }
            
            onProgress(0.4f, "Generating EPUB structure...")
            
            // Generate EPUB content
            val epubContent = generateEpubContent(book, chaptersWithContent, options, onProgress)
            
            onProgress(0.8f, "Writing EPUB file...")
            
            // Write to file
            val fileName = sanitizeFileName("${book.title} - ${book.author}.epub")
            val uri = fileProvider.createEpubFile(fileName, epubContent)
            
            onProgress(1.0f, "Export complete!")
            
            Result.success(uri)
        } catch (e: Exception) {
            Log.error("EPUB export failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Fetch content for all chapters, downloading if necessary
     */
    private suspend fun fetchChapterContent(
        chapters: List<Chapter>,
        onProgress: (Float, String) -> Unit
    ): List<Chapter> {
        val chaptersWithContent = mutableListOf<Chapter>()
        
        chapters.forEachIndexed { index, chapter ->
            val progress = 0.2f + (0.2f * (index.toFloat() / chapters.size))
            onProgress(progress, "Loading chapter ${index + 1}/${chapters.size}...")
            
            try {
                // Check if chapter has content
                val content = chapter.content.joinToString("")
                if (content.isNotBlank()) {
                    chaptersWithContent.add(chapter)
                } else {
                    // Try to fetch from repository
                    val fetchedChapter = chapterRepository.findChapterById(chapter.id)
                    if (fetchedChapter != null && fetchedChapter.content.joinToString("").isNotBlank()) {
                        chaptersWithContent.add(fetchedChapter)
                    } else {
                        Log.warn { "Chapter ${chapter.name} has no content, skipping" }
                    }
                }
            } catch (e: Exception) {
                Log.error("Error fetching chapter ${chapter.name}", e)
            }
        }
        
        return chaptersWithContent
    }
    
    /**
     * Generate complete EPUB content as byte array.
     * Uses platform-specific ZIP writer for KMP compatibility.
     */
    private suspend fun generateEpubContent(
        book: Book,
        chapters: List<Chapter>,
        options: EpubExportOptions,
        onProgress: (Float, String) -> Unit
    ): ByteArray {
        val entries = mutableListOf<EpubZipEntry>()
        
        // 1. Mimetype (must be first, uncompressed)
        entries.add(EpubZipEntry("mimetype", "application/epub+zip".encodeToByteArray(), compressed = false))
        
        // 2. META-INF/container.xml
        entries.add(EpubZipEntry("META-INF/container.xml", generateContainerXml().encodeToByteArray()))
        
        // 3. Cover image if available and requested (must be before OPF for hasCover flag)
        var hasCover = false
        if (options.includeCover && book.cover.isNotBlank()) {
            try {
                val coverData = fileProvider.downloadCoverImage(book.cover)
                if (coverData != null) {
                    val (extension, mimeType) = detectImageFormat(coverData)
                    entries.add(EpubZipEntry("OEBPS/images/cover.$extension", coverData))
                    hasCover = true
                    Log.info { "Cover image added to EPUB (format: $mimeType)" }
                }
            } catch (e: Exception) {
                Log.warn { "Failed to include cover image: ${e.message}" }
            }
        }
        
        // 4. Chapter images - download and embed images from chapter content
        val imageCounter = AtomicInteger(1)
        chapters.forEachIndexed { chapterIndex, chapter ->
            chapter.content.forEach { page ->
                if (page is ireader.core.source.model.ImageUrl) {
                    try {
                        val imageData = fileProvider.downloadCoverImage(page.url)
                        if (imageData != null) {
                            val (extension, _) = detectImageFormat(imageData)
                            val imageName = "chapter${chapterIndex + 1}_image${imageCounter.getAndIncrement()}.$extension"
                            entries.add(EpubZipEntry("OEBPS/images/$imageName", imageData))
                        }
                    } catch (e: Exception) {
                        Log.warn { "Failed to download chapter image: ${e.message}" }
                    }
                }
            }
        }
        
        // 5. OEBPS/content.opf (metadata and manifest) - after images so hasCover is known
        val coverFormat = if (hasCover) {
            // Find the cover image entry to get its actual format
            entries.firstOrNull { it.path.contains("cover.") }?.let { coverEntry ->
                val ext = coverEntry.path.substringAfterLast(".")
                val mime = when (ext) {
                    "png" -> "image/png"
                    "gif" -> "image/gif"
                    "webp" -> "image/webp"
                    else -> "image/jpeg"
                }
                ext to mime
            }
        } else null
        entries.add(EpubZipEntry("OEBPS/content.opf", generateContentOpf(book, chapters, options, hasCover, coverFormat).encodeToByteArray()))
        
        // 6. OEBPS/toc.ncx (table of contents)
        entries.add(EpubZipEntry("OEBPS/toc.ncx", generateTocNcx(book, chapters).encodeToByteArray()))
        
        // 7. OEBPS/stylesheet.css
        entries.add(EpubZipEntry("OEBPS/stylesheet.css", generateStylesheet(options).encodeToByteArray()))
        
        // 8. Chapter XHTML files
        chapters.forEachIndexed { index, chapter ->
            val progress = 0.4f + (0.4f * (index.toFloat() / chapters.size))
            onProgress(progress, "Writing chapter ${index + 1}/${chapters.size}...")
            
            entries.add(EpubZipEntry(
                "OEBPS/chapter${index + 1}.xhtml",
                generateChapterXhtml(chapter, index + 1, options).encodeToByteArray()
            ))
        }
        
        // Use platform-specific ZIP creation
        return createEpubZip(entries)
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
     * Generate META-INF/container.xml
     */
    private fun generateContainerXml(): String = """
        <?xml version="1.0" encoding="UTF-8"?>
        <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
            <rootfiles>
                <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
            </rootfiles>
        </container>
    """.trimIndent()
    
    /**
     * Generate OEBPS/content.opf (metadata and manifest)
     */
    @OptIn(ExperimentalTime::class)
    private fun generateContentOpf(book: Book, chapters: List<Chapter>, options: EpubExportOptions, hasCover: Boolean = false, coverFormat: Pair<String, String>? = null): String {
        val uuid = randomUUID()
        val chapterManifest = chapters.indices.joinToString("\n") { index ->
            """        <item id="chapter${index + 1}" href="chapter${index + 1}.xhtml" media-type="application/xhtml+xml"/>"""
        }
        val chapterSpine = chapters.indices.joinToString("\n") { index ->
            """        <itemref idref="chapter${index + 1}"/>"""
        }
        val coverItem = if (hasCover && coverFormat != null) {
            val (ext, mime) = coverFormat
            """        <item id="cover-image" href="images/cover.$ext" media-type="$mime"/>"""
        } else if (hasCover) {
            // Fallback for backward compatibility
            """        <item id="cover-image" href="images/cover.jpg" media-type="image/jpeg"/>"""
        } else ""
        val coverMeta = if (hasCover) {
            """                    <meta name="cover" content="cover-image"/>"""
        } else ""
        
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <package xmlns="http://www.idpf.org/2007/opf" unique-identifier="BookId" version="2.0">
                <metadata xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:opf="http://www.idpf.org/2007/opf">
                    <dc:title>${escapeXml(book.title)}</dc:title>
                    <dc:creator opf:role="aut">${escapeXml(book.author)}</dc:creator>
                    <dc:language>en</dc:language>
                    <dc:identifier id="BookId">urn:uuid:$uuid</dc:identifier>
                    <dc:description>${escapeXml(book.description)}</dc:description>
                    <dc:date>${kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.UTC).date}</dc:date>
                    <meta name="generator" content="iReader"/>
$coverMeta
                </metadata>
                <manifest>
                    <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
                    <item id="stylesheet" href="stylesheet.css" media-type="text/css"/>
$chapterManifest
$coverItem
                </manifest>
                <spine toc="ncx">
$chapterSpine
                </spine>
            </package>
        """.trimIndent()
    }
    
    /**
     * Generate OEBPS/toc.ncx (table of contents)
     */
    private fun generateTocNcx(book: Book, chapters: List<Chapter>): String {
        val navPoints = chapters.mapIndexed { index, chapter ->
            """
                <navPoint id="chapter${index + 1}" playOrder="${index + 1}">
                    <navLabel>
                        <text>${escapeXml(chapter.name)}</text>
                    </navLabel>
                    <content src="chapter${index + 1}.xhtml"/>
                </navPoint>
            """.trimIndent()
        }.joinToString("\n")
        
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
                <head>
                    <meta name="dtb:uid" content="urn:uuid:${randomUUID()}"/>
                    <meta name="dtb:depth" content="1"/>
                    <meta name="dtb:totalPageCount" content="0"/>
                    <meta name="dtb:maxPageNumber" content="0"/>
                </head>
                <docTitle>
                    <text>${escapeXml(book.title)}</text>
                </docTitle>
                <navMap>
        $navPoints
                </navMap>
            </ncx>
        """.trimIndent()
    }
    
    /**
     * Generate stylesheet with beautified formatting
     */
    private fun generateStylesheet(options: EpubExportOptions): String = """
        body {
            font-family: ${options.typography.fontFamily};
            line-height: 1.5;
            margin: 5%;
            text-align: justify;
        }
        
        h1 {
            font-size: ${options.chapterHeadingSize}em;
            font-weight: bold;
            margin-top: 2em;
            margin-bottom: 1em;
            text-align: center;
            page-break-before: always;
        }
        
        p {
            margin-bottom: ${options.paragraphSpacing}em;
            text-indent: 1.5em;
        }
        
        p:first-of-type {
            text-indent: 0;
        }
        
        h1 + p {
            text-indent: 0;
        }
        
        ${if (options.addDropCaps) """
        p:first-of-type::first-letter {
            font-size: 3em;
            font-weight: bold;
            float: left;
            line-height: 0.9;
            margin: 0.1em 0.1em 0 0;
        }
        """ else ""}
        
        .chapter-content {
            margin: 0 auto;
            max-width: 40em;
        }
        
        img {
            max-width: 100%;
            height: auto;
            display: block;
            margin: 1em auto;
        }
    """.trimIndent()
    
    /**
     * Generate XHTML for a single chapter
     */
    private fun generateChapterXhtml(chapter: Chapter, chapterNumber: Int, options: EpubExportOptions): String {
        val content = chapter.content.joinToString("")
        val cleanedContent = content // TODO: Implement HTML cleaning if needed
        
        // Convert to paragraphs if not already formatted
        val formattedContent = formatContent(cleanedContent)
        
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <title>${escapeXml(chapter.name)}</title>
                <link rel="stylesheet" type="text/css" href="stylesheet.css"/>
            </head>
            <body>
                <div class="chapter-content">
                    <h1>${escapeXml(chapter.name)}</h1>
                    $formattedContent
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Format content into proper paragraphs
     */
    private fun formatContent(content: String): String {
        // Remove script tags and other unwanted elements
        var cleaned = content
            .replace(Regex("<script[^>]*>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<style[^>]*>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
        
        // If content doesn't have paragraph tags, add them
        if (!cleaned.contains("<p>") && !cleaned.contains("<p ")) {
            // Split by double newlines and wrap in paragraphs
            cleaned = cleaned.split(Regex("\n\n+"))
                .filter { it.trim().isNotBlank() }
                .joinToString("\n") { "<p>${it.trim()}</p>" }
        }
        
        // Fix quotation marks
        cleaned = cleaned
            .replace("\"", """)
            .replace("\"", """)
            .replace("'", "'")
            .replace("'", "'")
        
        // Fix em dashes
        cleaned = cleaned.replace(" - ", "—")
        
        // Remove extra whitespace
        cleaned = cleaned.replace(Regex("\\s+"), " ")
        
        return cleaned
    }
    
    /**
     * Escape XML special characters
     */
    private fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
    
    /**
     * Sanitize filename for file system
     */
    private fun sanitizeFileName(fileName: String): String = fileName
        .replace(Regex("[^a-zA-Z0-9._\\- ]"), "")
        .take(200)
    
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
}

/**
 * Platform-specific file provider interface
 */
interface EpubFileProvider {
    /**
     * Create an EPUB file with the given content
     */
    suspend fun createEpubFile(fileName: String, content: ByteArray): Uri
    
    /**
     * Download cover image and return as byte array
     */
    suspend fun downloadCoverImage(url: String): ByteArray?
}


/**
 * Platform-specific ZIP creation for EPUB files.
 * Implementations should handle proper ZIP format with mimetype uncompressed first.
 */
expect fun createEpubZip(entries: List<EpubExportServiceImpl.EpubZipEntry>): ByteArray
