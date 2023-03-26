package ireader.domain.usecases.epub

import ireader.core.source.LocalSource
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.Text
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.file.FileSaver
import ireader.domain.usecases.files.GetSimpleStorage
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.TextNode
import org.xml.sax.InputSource
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

actual class ImportEpub(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val fileSaver: FileSaver,
    private val simpleStorage: GetSimpleStorage
) {


    private fun getEpubReader(uri: ireader.domain.models.common.Uri): nl.siegmann.epublib.domain.Book? {
        return fileSaver.readStream(uri) .let {
            EpubReader().readEpub(it)
        }
    }
    data class Section(val title: String, val html: Document)
    fun getSections(book: nl.siegmann.epublib.domain.Book) : List<Section>{
        val sections = mutableListOf<Section>()
        book.tableOfContents.tocReferences.forEach { tocReference ->
            val resource = tocReference.resource
            if (resource != null) {
                val href = resource.href
                val data = resource.getReader().readText()
                val contentType = resource.mediaType.toString()
                val html = Jsoup.parse(data)
                val title = tocReference.title
                val section = Section(title, html)
                sections.add(section)
            }
        }

        return sections
    }
    actual suspend fun parse(uris: List<ireader.domain.models.common.Uri>) {
        uris.forEach { uri ->


        val epub = getEpubReader(uri) ?: throw Exception()

        val key = epub.metadata?.titles?.firstOrNull() ?: throw Exception("Unknown novel")
        val imgFile = File(simpleStorage.ireaderCacheDir(), "library_covers/$key.png")
        bookRepository.delete(key)
        // Insert new book data
        val bookId = Book(
            title = epub.metadata.titles.firstOrNull() ?: "",
            key = key ?: "",
            favorite = true,
            sourceId = LocalSource.SOURCE_ID,
            cover = imgFile.absolutePath,
            author = epub.metadata?.authors?.firstOrNull()?.let { it.firstname + " " + it.lastname }
                ?: "",
            status = MangaInfo.PUBLISHING_FINISHED,
            description = epub.metadata?.descriptions?.firstOrNull()?.let { Jsoup.parse(it).text() }
                ?: "",
        )
            .let { bookRepository.upsert(it) }
            val sections = getSections(epub)
            sections.map { section ->
                Chapter(
                    name = section.title ?: "Unknown",
                    key = section.title ?: "",
                    bookId = bookId,
                    content = section.html.select("h1,h2,h3,h4,h5,h6,p").eachText().map { Text(it) },
                )
            }.filterNotNull().let {
            chapterRepository.insertChapters(it)
        }
        epub.coverImage?.data?.let {
            imgFile.parentFile?.also { parent ->
                parent.mkdirs()
                if (parent.exists()) {
                    imgFile.writeBytes(it)
                }
            }
        }
        }
    }

    private inner class EpubXMLFileParser(
        val data: ByteArray,
    ) {

        // Make all local references absolute to the root of the epub for consistent references
        val absBasePath: String = File("").canonicalPath
        fun parse(): Output {
            val body = Jsoup.parse(data.inputStream(), "UTF-8", "").body()
            val title = body.selectFirst("h1, h2, h3, h4, h5, h6")?.text()
            body.selectFirst("h1, h2, h3, h4, h5, h6")?.remove()

            val content = getNodeStructuredText(body)
            return Output(
                title = title,
                body = content
            )
        }
    }

    private data class Output(val title: String?, val body: String)
    internal fun parseXMLFile(inputSteam: InputStream): org.w3c.dom.Document? =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSteam)

    internal fun parseXMLFile(byteArray: ByteArray): org.w3c.dom.Document? = parseXMLFile(byteArray.inputStream())
    private fun parseXMLText(text: String): org.w3c.dom.Document? = text.reader().runCatching {
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(InputSource(this))
    }.getOrNull()

    private fun getPTraverse(node: org.jsoup.nodes.Node): String {
        fun innerTraverse(node: org.jsoup.nodes.Node): String =
            node.childNodes().joinToString("") { child ->
                when {
                    child.nodeName() == "br" -> "\n"
                    child.nodeName() == "img" -> ""
                    child is TextNode -> child.text()
                    else -> innerTraverse(child)
                }
            }

        val paragraph = innerTraverse(node).trim()
        return if (paragraph.isEmpty()) "" else innerTraverse(node).trim() + "\n\n"
    }

    private fun getNodeTextTraverse(node: org.jsoup.nodes.Node): String {
        val children = node.childNodes()
        if (children.isEmpty())
            return ""
        return children.joinToString("") { child ->
            when {
                child.nodeName() == "p" -> getPTraverse(child)
                child.nodeName() == "br" -> "\n"
                child.nodeName() == "hr" -> "\n\n"
                child.nodeName() == "img" -> ""
                child is TextNode -> {
                    val text = child.text().trim()
                    if (text.isEmpty()) "" else text + "\n\n"
                }
                else -> getNodeTextTraverse(child)
            }
        }
    }

    private fun getNodeStructuredText(node: org.jsoup.nodes.Node): String {
        val children = node.childNodes()
        if (children.isEmpty())
            return ""
        return children.joinToString("") { child ->
            when {
                child.nodeName() == "p" -> getPTraverse(child)
                child.nodeName() == "br" -> "\n"
                child.nodeName() == "hr" -> "\n\n"
                child.nodeName() == "img" -> ""
                child is TextNode -> child.text().trim()
                else -> getNodeTextTraverse(child)
            }
        }
    }

    actual fun getCacheSize() : String {
        return simpleStorage.getCacheSize()
    }
    actual fun removeCache() {
        simpleStorage.clearCache()
    }
}
