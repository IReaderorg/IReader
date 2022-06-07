package org.ireader.domain.use_cases.epub.importer

import android.content.Context
import org.ireader.common_data.repository.BookRepository
import org.ireader.common_data.repository.ChapterRepository
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.core_api.source.LocalSource
import org.ireader.core_api.source.model.MangaInfo
import org.ireader.domain.use_cases.epub.epup_parser.model.EpubBook
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import java.io.File
import javax.inject.Inject

class ImportEpub @Inject constructor(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
) {
    suspend operator fun invoke(epub: EpubBook, context: Context) {

        val key = epub.epubMetadataModel?.id ?: epub.epubMetadataModel?.title
        ?: throw Exception("Unknown novel")
        val imgFile = File(context.filesDir, "library_covers/${key}")
        bookRepository.delete(key)
        // Insert new book data
        val bookId = Book(
            title = epub.epubMetadataModel?.title ?: "",
            key = key ?: "",
            favorite = true,
            sourceId = LocalSource.SOURCE_ID,
            cover = imgFile.path,
            author = epub.epubMetadataModel?.creators?.joinToString("-") ?: "",
            status = MangaInfo.PUBLISHING_FINISHED,
            description = epub.epubMetadataModel?.description?.let { Jsoup.parse(it).text() } ?: "",
        )
            .let { bookRepository.insertBook(it) }
        val chapterExtensions = listOf("xhtml", "xml", "html").map { ".$it" }
        val information =
            epub.epubTableOfContentsModel?.tableOfContents?.associate { it.location to it.label }
        var index = 0

        epub.epubManifestModel?.resources?.filter { item ->
            chapterExtensions.any {
                item.href?.endsWith(
                    it,
                    ignoreCase = true
                ) ?: false
            }
        }?.map { epubResourceModel ->

           epubResourceModel.byteArray?.let {
                EpubXMLFileParser(it).parse()
            }?.let { output ->
               val title =   information?.get(epubResourceModel.href) ?: output.title

                val content =  output.body.split("\n")
                index++
                Chapter(
                    name = title ?: "Unknown",
                    key = epubResourceModel.href ?: "",
                    bookId = bookId,
                    content = content,
                    number = index.toFloat()
                )
            }


        }?.mapNotNull {  it }?.let {
            chapterRepository.insertChapters(it)
        }


        epub.epubCoverImage!!.byteArray?.let {
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
        fun parse(): Output? {
            val body = Jsoup.parse(data.inputStream(), "UTF-8", "").body()
            val title = body.selectFirst("h1, h2, h3, h4, h5, h6")?.text()
            body.selectFirst("h1, h2, h3, h4, h5, h6")?.remove()

            val content = getNodeStructuredText(body)
           if (content.isBlank()) return null

            return Output(
                title = title,
                body = content
            )
        }
    }

    private data class Output(val title: String?, val body: String)

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


