package ireader.domain.usecases.epub

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import ireader.core.source.model.Text
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.image.CoverCache
import ireader.domain.models.BookCover
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


actual class EpubCreator(
    private val coverCache: CoverCache,
    private val chapterRepository: ChapterRepository,
    context: Context  // Use application context to avoid memory leaks
) {
    // Store application context which is safe to hold
    private val appContext: Context = context.applicationContext

    /**
     * Creates an EPUB file with progress reporting
     */
    private fun createEpub(
        uri: Uri,
        book: Book,
        chapters: List<Chapter>,
        coverFile: File?,
        progressCallback: (String) -> Unit
    ) {
        val contentResolver = appContext.contentResolver
        val pfd = contentResolver.openFileDescriptor(uri, "w") 
            ?: throw Exception("Cannot open file for writing")
        
        try {
            FileOutputStream(pfd.fileDescriptor).use { fos ->
                ZipOutputStream(fos).use { zip ->
                    progressCallback("Creating EPUB structure...")
                    
                    // mimetype (must be first, uncompressed per EPUB spec)
                    zip.setLevel(0)
                    zip.putNextEntry(ZipEntry("mimetype"))
                    zip.write("application/epub+zip".toByteArray())
                    zip.closeEntry()
                    zip.setLevel(9)
                    
                    progressCallback("Writing metadata...")
                    
                    // META-INF/container.xml
                    zip.putNextEntry(ZipEntry("META-INF/container.xml"))
                    zip.write(containerXml.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                    
                    // content.opf
                    zip.putNextEntry(ZipEntry("OEBPS/content.opf"))
                    zip.write(createContentOpf(book, chapters, coverFile != null).toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                    
                    // toc.ncx
                    zip.putNextEntry(ZipEntry("OEBPS/toc.ncx"))
                    zip.write(createTocNcx(book, chapters).toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                    
                    // nav.xhtml (EPUB 3 navigation)
                    zip.putNextEntry(ZipEntry("OEBPS/nav.xhtml"))
                    zip.write(createNavDocument(book, chapters).toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                    
                    // CSS stylesheet
                    zip.putNextEntry(ZipEntry("OEBPS/stylesheet.css"))
                    zip.write(createStylesheet().toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                    
                    // Cover image if exists
                    coverFile?.let {
                        if (it.exists()) {
                            progressCallback("Adding cover image...")
                            val extension = it.extension.lowercase()
                            val filename = "cover.$extension"
                            zip.putNextEntry(ZipEntry("OEBPS/$filename"))
                            zip.write(it.readBytes())
                            zip.closeEntry()
                        }
                    }
                    
                    // Chapter HTML files
                    chapters.forEachIndexed { index, chapter ->
                        progressCallback("Writing chapter ${index + 1}/${chapters.size}: ${chapter.name}")
                        zip.putNextEntry(ZipEntry("OEBPS/chapter$index.xhtml"))
                        zip.write(createChapterHtml(chapter, index).toByteArray(Charsets.UTF_8))
                        zip.closeEntry()
                    }
                    
                    progressCallback("Finalizing EPUB...")
                }
            }
            progressCallback("EPUB created successfully!")
        } catch (e: Exception) {
            throw Exception("Failed to create EPUB: ${e.message}", e)
        } finally {
            pfd.close()
        }
    }

    actual suspend operator fun invoke(
        book: Book,
        uri: ireader.domain.models.common.Uri,
        currentEvent: (String) -> Unit
    ) {
        try {
            currentEvent("Loading chapters...")
            val chapters = chapterRepository.findChaptersByBookId(book.id)
            
            if (chapters.isEmpty()) {
                throw Exception("No chapters found for this book")
            }
            
            currentEvent("Loading cover image...")
            val cover = coverCache.getCoverFile(BookCover.Companion.from(book))
            
            createEpub(uri.androidUri, book, chapters, cover, currentEvent)
        } catch (e: Exception) {
            currentEvent("Error: ${e.message}")
            throw e
        }
    }
    
    /**
     * Creates XHTML content for a chapter with proper formatting
     */
    private fun createChapterHtml(chapter: Chapter, index: Int): String {
        val contents = chapter.content.mapNotNull {
            when(it) {
                is Text -> it.text
                else -> null
            }
        }
        
        val paragraphs = contents.joinToString("\n") { text ->
            // Clean HTML content to remove scripts, styles, ads, and watermarks
            val cleanedText = if (HtmlContentCleaner.isHtml(text)) {
                HtmlContentCleaner.extractPlainText(text)
            } else {
                text.trim()
            }
            
            val escaped = escapeXml(cleanedText)
            if (escaped.isNotBlank()) {
                "    <p>$escaped</p>"
            } else {
                ""
            }
        }
        
        return """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops">
<head>
    <meta charset="UTF-8"/>
    <title>${escapeXml(chapter.name)}</title>
    <link rel="stylesheet" type="text/css" href="stylesheet.css"/>
</head>
<body>
    <section id="chapter$index" epub:type="chapter">
        <h1>${escapeXml(chapter.name)}</h1>
$paragraphs
    </section>
</body>
</html>"""
    }
    
    /**
     * Creates CSS stylesheet for better reading experience
     */
    private fun createStylesheet(): String {
        return """
body {
    font-family: Georgia, serif;
    line-height: 1.6;
    margin: 1em;
    text-align: justify;
}

h1 {
    font-size: 1.8em;
    margin-top: 1em;
    margin-bottom: 0.5em;
    text-align: center;
    font-weight: bold;
}

p {
    margin: 0.5em 0;
    text-indent: 1.5em;
}

p:first-of-type {
    text-indent: 0;
}

section {
    page-break-after: always;
}
""".trimIndent()
    }
    
    /**
     * Creates EPUB 3 navigation document
     */
    private fun createNavDocument(book: Book, chapters: List<Chapter>): String {
        val navItems = chapters.mapIndexed { index, chapter ->
            """        <li><a href="chapter$index.xhtml">${escapeXml(chapter.name)}</a></li>"""
        }.joinToString("\n")
        
        return """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops">
<head>
    <meta charset="UTF-8"/>
    <title>Table of Contents</title>
</head>
<body>
    <nav epub:type="toc" id="toc">
        <h1>Table of Contents</h1>
        <ol>
$navItems
        </ol>
    </nav>
</body>
</html>"""
    }
    
    /**
     * Creates OPF package document with complete metadata
     */
    private fun createContentOpf(book: Book, chapters: List<Chapter>, hasCover: Boolean): String {
        val coverExtension = if (hasCover) "jpg" else null
        val coverMediaType = when (coverExtension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> "image/jpeg"
        }
        
        val manifest = buildString {
            // Navigation documents
            appendLine("""    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>""")
            appendLine("""    <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>""")
            appendLine("""    <item id="stylesheet" href="stylesheet.css" media-type="text/css"/>""")
            
            // Cover image
            if (hasCover) {
                appendLine("""    <item id="cover-image" href="cover.$coverExtension" media-type="$coverMediaType" properties="cover-image"/>""")
            }
            
            // Chapters
            chapters.forEachIndexed { index, _ ->
                appendLine("""    <item id="chapter$index" href="chapter$index.xhtml" media-type="application/xhtml+xml"/>""")
            }
        }
        
        val spine = buildString {
            appendLine("""    <itemref idref="nav"/>""")
            chapters.indices.forEach { index ->
                appendLine("""    <itemref idref="chapter$index"/>""")
            }
        }
        
        val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        
        return """<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="http://www.idpf.org/2007/opf" version="3.0" unique-identifier="BookId">
  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:identifier id="BookId">${escapeXml(book.key)}</dc:identifier>
    <dc:title>${escapeXml(book.title)}</dc:title>
    <dc:creator>${escapeXml(book.author ?: "Unknown")}</dc:creator>
    <dc:language>en</dc:language>
    <dc:publisher>IReader</dc:publisher>
    <dc:date>$currentDate</dc:date>
    <meta property="dcterms:modified">$currentDate</meta>
    ${if (book.description.isNotBlank()) "<dc:description>${escapeXml(book.description)}</dc:description>" else ""}
  </metadata>
  <manifest>
$manifest
  </manifest>
  <spine toc="ncx">
$spine
  </spine>
</package>"""
    }
    
    private fun createTocNcx(book: Book, chapters: List<Chapter>): String {
        val navPoints = chapters.mapIndexed { index, chapter ->
            """    <navPoint id="navPoint-${index + 1}" playOrder="${index + 1}">
      <navLabel>
        <text>${escapeXml(chapter.name)}</text>
      </navLabel>
      <content src="chapter$index.xhtml"/>
    </navPoint>"""
        }.joinToString("\n")
        
        return """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE ncx PUBLIC "-//NISO//DTD ncx 2005-1//EN" "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd">
<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
  <head>
    <meta name="dtb:uid" content="${book.key}"/>
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
</ncx>"""
    }
    
    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
    
    private val containerXml = """<?xml version="1.0" encoding="UTF-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
  <rootfiles>
    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
  </rootfiles>
</container>"""
    private val reservedChars = "|\\?*<\":>+[]/'"
    private fun sanitizeFilename(name: String): String {
        var tempName = name
        for (c in reservedChars) {
            tempName = tempName.replace(c, ' ')
        }
        return tempName.replace("  ", " ")
    }


    @Composable
    actual fun onEpubCreateRequested(book: Book, onStart: @Composable ((Any) -> Unit)) {
        val mimeTypes = arrayOf("application/epub+zip")
        val fn = "${sanitizeFilename(book.title)}.epub"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("application/epub+zip")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            .putExtra(
                Intent.EXTRA_TITLE, fn
            )
        onStart(intent)
    }
}
