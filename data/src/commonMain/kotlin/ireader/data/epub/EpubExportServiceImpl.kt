package ireader.data.epub

import ireader.core.log.Log
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.services.epub.EpubExportOptions
import ireader.domain.services.epub.EpubExportService
import ireader.domain.usecases.epub.HtmlContentCleaner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
    ): Result<Uri> = withContext(Dispatchers.IO) {
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
     * Generate complete EPUB content as byte array
     */
    private suspend fun generateEpubContent(
        book: Book,
        chapters: List<Chapter>,
        options: EpubExportOptions,
        onProgress: (Float, String) -> Unit
    ): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val zipOutputStream = ZipOutputStream(outputStream)
        
        try {
            // 1. Write mimetype (must be first, uncompressed)
            zipOutputStream.setLevel(0)
            zipOutputStream.putNextEntry(ZipEntry("mimetype"))
            zipOutputStream.write("application/epub+zip".toByteArray())
            zipOutputStream.closeEntry()
            
            // Reset compression for other files
            zipOutputStream.setLevel(9)
            
            // 2. Write META-INF/container.xml
            zipOutputStream.putNextEntry(ZipEntry("META-INF/container.xml"))
            zipOutputStream.write(generateContainerXml().toByteArray())
            zipOutputStream.closeEntry()
            
            // 3. Write OEBPS/content.opf (metadata and manifest)
            zipOutputStream.putNextEntry(ZipEntry("OEBPS/content.opf"))
            zipOutputStream.write(generateContentOpf(book, chapters, options).toByteArray())
            zipOutputStream.closeEntry()
            
            // 4. Write OEBPS/toc.ncx (table of contents)
            zipOutputStream.putNextEntry(ZipEntry("OEBPS/toc.ncx"))
            zipOutputStream.write(generateTocNcx(book, chapters).toByteArray())
            zipOutputStream.closeEntry()
            
            // 5. Write OEBPS/stylesheet.css
            zipOutputStream.putNextEntry(ZipEntry("OEBPS/stylesheet.css"))
            zipOutputStream.write(generateStylesheet(options).toByteArray())
            zipOutputStream.closeEntry()
            
            // 6. Write chapter XHTML files
            chapters.forEachIndexed { index, chapter ->
                val progress = 0.4f + (0.4f * (index.toFloat() / chapters.size))
                onProgress(progress, "Writing chapter ${index + 1}/${chapters.size}...")
                
                zipOutputStream.putNextEntry(ZipEntry("OEBPS/chapter${index + 1}.xhtml"))
                zipOutputStream.write(generateChapterXhtml(chapter, index + 1, options).toByteArray())
                zipOutputStream.closeEntry()
            }
            
            // 7. Write cover image if available and requested
            if (options.includeCover && book.cover.isNotBlank()) {
                try {
                    val coverData = fileProvider.downloadCoverImage(book.cover)
                    if (coverData != null) {
                        zipOutputStream.putNextEntry(ZipEntry("OEBPS/images/cover.jpg"))
                        zipOutputStream.write(coverData)
                        zipOutputStream.closeEntry()
                    }
                } catch (e: Exception) {
                    Log.warn { "Failed to include cover image: ${e.message}" }
                }
            }
            
            zipOutputStream.close()
            return outputStream.toByteArray()
        } finally {
            zipOutputStream.close()
            outputStream.close()
        }
    }
    
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
    private fun generateContentOpf(book: Book, chapters: List<Chapter>, options: EpubExportOptions): String {
        val uuid = UUID.randomUUID().toString()
        val chapterManifest = chapters.indices.joinToString("\n") { index ->
            """        <item id="chapter${index + 1}" href="chapter${index + 1}.xhtml" media-type="application/xhtml+xml"/>"""
        }
        val chapterSpine = chapters.indices.joinToString("\n") { index ->
            """        <itemref idref="chapter${index + 1}"/>"""
        }
        val coverItem = if (options.includeCover) {
            """        <item id="cover" href="images/cover.jpg" media-type="image/jpeg"/>"""
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
                    <dc:date>${java.time.LocalDate.now()}</dc:date>
                    <meta name="generator" content="iReader"/>
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
                    <meta name="dtb:uid" content="urn:uuid:${UUID.randomUUID()}"/>
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
