package ireader.domain.usecases.epub

import androidx.compose.runtime.Composable
import ireader.core.source.model.Text
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Desktop implementation of EPUB creator using pure Kotlin
 */
actual class EpubCreator(
    private val chapterRepository: ChapterRepository
) {
    
    actual suspend operator fun invoke(book: Book, uri: Uri, currentEvent: (String) -> Unit) = withContext(Dispatchers.IO) {
        try {
            currentEvent("Loading chapters...")
            val chapters = chapterRepository.findChaptersByBookId(book.id)
            
            if (chapters.isEmpty()) {
                throw Exception("No chapters found for this book")
            }
            
            val file = File(uri.uriString)
            file.parentFile?.mkdirs()
            
            currentEvent("Creating EPUB...")
            createEpub(file, book, chapters, currentEvent)
            
            currentEvent("EPUB created successfully!")
        } catch (e: Exception) {
            currentEvent("Error: ${e.message}")
            throw e
        }
    }
    
    private fun createEpub(
        file: File,
        book: Book,
        chapters: List<ireader.domain.models.entities.Chapter>,
        progressCallback: (String) -> Unit
    ) {
        FileOutputStream(file).use { fos ->
            ZipOutputStream(fos).use { zip ->
                progressCallback("Creating EPUB structure...")
                
                // mimetype (must be first, uncompressed)
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
                zip.write(createContentOpf(book, chapters).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                
                // toc.ncx
                zip.putNextEntry(ZipEntry("OEBPS/toc.ncx"))
                zip.write(createTocNcx(book, chapters).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                
                // nav.xhtml
                zip.putNextEntry(ZipEntry("OEBPS/nav.xhtml"))
                zip.write(createNavDocument(book, chapters).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                
                // stylesheet.css
                zip.putNextEntry(ZipEntry("OEBPS/stylesheet.css"))
                zip.write(createStylesheet().toByteArray(Charsets.UTF_8))
                zip.closeEntry()
                
                // Chapter files
                chapters.forEachIndexed { index, chapter ->
                    progressCallback("Writing chapter ${index + 1}/${chapters.size}: ${chapter.name}")
                    zip.putNextEntry(ZipEntry("OEBPS/chapter$index.xhtml"))
                    zip.write(createChapterHtml(chapter, index).toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
                
                progressCallback("Finalizing EPUB...")
            }
        }
    }
    
    private fun createChapterHtml(chapter: ireader.domain.models.entities.Chapter, index: Int): String {
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
    
    private fun createNavDocument(book: Book, chapters: List<ireader.domain.models.entities.Chapter>): String {
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
    
    private fun createContentOpf(book: Book, chapters: List<ireader.domain.models.entities.Chapter>): String {
        val manifest = buildString {
            appendLine("""    <item id="nav" href="nav.xhtml" media-type="application/xhtml+xml" properties="nav"/>""")
            appendLine("""    <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>""")
            appendLine("""    <item id="stylesheet" href="stylesheet.css" media-type="text/css"/>""")
            
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
    
    private fun createTocNcx(book: Book, chapters: List<ireader.domain.models.entities.Chapter>): String {
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

    @Composable
    actual fun onEpubCreateRequested(book: Book, onStart: @Composable ((Any) -> Unit)) {
        // Desktop implementation - could open file chooser dialog
        val filename = sanitizeFilename(book.title) + ".epub"
        onStart(filename)
    }
    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[|\\\\?*<\":>+\\[\\]/']+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}