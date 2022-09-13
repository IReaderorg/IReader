package ireader.domain.usecases.epub.importer

import android.content.Context
import android.net.Uri
import nl.siegmann.epublib.epub.EpubReader
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.common.models.entities.Book
import ireader.common.models.entities.Chapter
import ireader.core.api.source.LocalSource
import ireader.core.api.source.model.MangaInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Parser
import org.koin.core.annotation.Factory
import org.xml.sax.InputSource
import java.io.File
import java.io.InputStream

import javax.xml.parsers.DocumentBuilderFactory
@Factory
class ImportEpub(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
) {

    private fun getEpubReader(context: Context, uri: Uri): nl.siegmann.epublib.domain.Book? {
        return context.contentResolver.openInputStream(uri)?.use {
            EpubReader().readEpub(it)
        }
    }

    suspend fun parse(uri: Uri, context: Context) {
        val epub = getEpubReader(context, uri) ?: throw Exception()

        val key = epub.metadata?.titles?.firstOrNull() ?: throw Exception("Unknown novel")
        val imgFile = File(context.filesDir, "library_covers/$key")
        bookRepository.delete(key)
        // Insert new book data
        val bookId = Book(
            title = epub.metadata.titles.firstOrNull() ?: "",
            key = key ?: "",
            favorite = true,
            sourceId = LocalSource.SOURCE_ID,
            cover = imgFile.path,
            author = epub.metadata?.authors?.firstOrNull()?.let { it.firstname + " " + it.lastname }
                ?: "",
            status = MangaInfo.PUBLISHING_FINISHED,
            description = epub.metadata?.descriptions?.firstOrNull()?.let { Jsoup.parse(it).text() }
                ?: "",
        )
            .let { bookRepository.upsert(it) }
        val chapterExtensions = listOf("xhtml", "xml", "html").map { ".$it" }
//        val information =
//            epub.tableOfContents?.tocReferences?.associate { it?.completeHref to it?.title }
        var index = 0
        var tableOfContents = mapOf<String, String>()

        epub.tableOfContents.tocReferences.map { ref ->
            tableOfContents = tableOfContents + (ref.completeHref.substringBefore("#") to ref.title)
            tableOfContents = tableOfContents + (ref.children.map { item -> (item.completeHref.substringBefore("#") to item.title) })
        }
        epub.opfResource.inputStream.let { stream ->
            Jsoup.parse(stream, "UTF-8", "", Parser.xmlParser()).select("item").eachAttr("href")
        }.filter { item ->
            chapterExtensions.any {
                item.endsWith(
                    it,
                    ignoreCase = true
                )
            }
        }.map {
            epub.resources.resourceMap[it]
        }.map { epubResourceModel ->
            epubResourceModel?.data?.let {
                EpubXMLFileParser(it).parse()
            }?.let { output ->
                val title = tableOfContents[epubResourceModel.href] ?: output.title

                val content = output.body.split("\n").filter { it.isNotBlank() }.map { ireader.core.api.source.model.Text(text = it) }
                index++
                Chapter(
                    name = title ?: "Unknown",
                    key = epubResourceModel.href ?: "",
                    bookId = bookId,
                    content = content,
                )
            }
        }.mapNotNull { it }.let {
            chapterRepository.insertChapters(it)
        }

//        epub.resources.all.sortedBy { it.id }.map { epubResourceModel ->
//            epubResourceModel.data?.let {
//                EpubXMLFileParser(it).parse()
//            }?.let { output ->
//                val title = tableOfContents[epubResourceModel.href] ?: output.title
//
//                val content = output.body.split("\n").filter { it.isNotBlank() }
//                index++
//                Chapter(
//                    name = title ?: "Chapter $index",
//                    key = epubResourceModel.href ?: "",
//                    bookId = bookId,
//                    content = content,
//                )
//            }
//
//        }.mapNotNull { it }.let {
//            chapterRepository.insertChapters(it)
//        }

        epub.coverImage?.data?.let {
            imgFile.parentFile?.also { parent ->
                parent.mkdirs()
                if (parent.exists())
                    imgFile.writeBytes(it)
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
}
