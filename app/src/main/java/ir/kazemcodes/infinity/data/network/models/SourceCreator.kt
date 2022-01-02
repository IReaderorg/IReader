package ir.kazemcodes.infinity.data.network.models

import android.content.Context
import ir.kazemcodes.infinity.api_feature.network.GET
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class SourceCreator(
    context: Context,
    private val _baseUrl: String,
    private val _lang: String,
    private val _name: String,
    private val _supportsLatest: Boolean,
    private val _supportsMostPopular: Boolean,
    private val _latestUpdateEndpoint: String?,
    private val _popularEndpoint: String?,
    private val _searchEndpoint: String?,
    private val _chaptersEndpoint: String?,
    private val _contentEndpoint: String?,
    private val _popularBookSelector: String?,
    private val _popularNextBookSelector: String?,
    private val _linkPopularSelector: String?,
    private val _linkAttPopularSelector: String?,
    private val _namePopularBookSelector: String?,
    private val _nameAttPopularBookSelector: String?,
    private val _bookCoverPopularBookSelector: String?,
    private val _bookAttCoverPopularBookSelector: String?,
    private val _latestBookSelector: String?,
    private val _latestNextBookSelector: String?,
    private val _linkLatestSelector: String?,
    private val _linkAttLatestSelector: String?,
    private val _nameLatestBookSelector: String?,
    private val _nameAttLatestBookSelector: String?,
    private val _bookCoverLatestBookSelector: String?,
    private val _bookCoverAttLatestBookSelector: String?,
    private val _bookNameBookDetailSelector: String?,
    private val _bookNameAttBookDetailSelector: String?,
    private val _descriptionBookDetailBookSelector: String?,
    private val _descriptionAttBookDetailBookSelector: String?,
    private val _authorBookDetailBookSelector: String?,
    private val _authorAttBookDetailBookSelector: String?,
    private val _categoryBookDetailBookSelector: String?,
    private val _categoryAttBookDetailBookSelector: String?,
    private val _hasNextChapterSelector: String?,
    private val _hasNextChapterValue: String?,
    private val _chapterListSelector: String?,
    private val _linkChaptersSelector: String?,
    private val _linkAttChaptersSelector: String?,
    private val _nameChaptersBookSelector: String?,
    private val _nameAttChaptersBookSelector: String?,
    private val _hasNextChaptersNextPageSelector: String?,
    private val _hasNextChaptersNextPageValueSelector: String?,
    private val _chapterPageContentSelector: String?,
    private val _chapterPageContentAttSelector: String?,
    private val _searchBookItemSelector: String?,
    private val _linkSearchedBookSelector: String?,
    private val _linkAttSearchedBookSelector: String?,
    private val _nameSearchedBookSelector: String?,
    private val _nameAttSearchedBookSelector: String?,
    private val _bookCoverSearchedBookSelector: String?,
    private val _bookCoverAttSearchedBookSelector: String?,
    private val _hasNextSearchedBookNextPageSelector: String?,
    private val _hasNextSearchedBookNextPageValueSelector: String?,


    ) : ParsedHttpSource(context) {
    override val lang: String
        get() = _lang
    override val name: String
        get() = _name
    override val supportsLatest: Boolean
        get() = _supportsLatest
    override val supportsMostPopular: Boolean
        get() = _supportsMostPopular
    override val baseUrl: String = _baseUrl

    override fun fetchLatestUpdatesEndpoint(): String? = _latestUpdateEndpoint
    override fun fetchPopularEndpoint(): String? = _popularEndpoint
    override fun fetchSearchBookEndpoint(): String? = _searchEndpoint
    override fun fetchChaptersEndpoint(): String? = _chaptersEndpoint
    override fun fetchContentEndpoint(): String? = _contentEndpoint

    override fun popularBookSelector(): String? = _popularBookSelector

    override fun popularBookNextPageSelector(): String? = _popularNextBookSelector

    override fun popularBookRequest(page: Int): Request =
        GET("$baseUrl${
            getUrlWithoutDomain(fetchPopularEndpoint()?.replace(pageFormat,
                page.toString()) ?: "")
        }")


    override fun popularBookFromElement(element: Element): Book {
        val book: Book = Book.create()

        val selectorLink = _linkPopularSelector
        val attLink = _linkAttPopularSelector
        val selectorName = _namePopularBookSelector
        val attName = _nameAttPopularBookSelector
        val selectorCover = _bookCoverPopularBookSelector
        val attCover = _bookAttCoverPopularBookSelector

        book.link = selectorReturnerStringType(element, selectorLink, attLink)
        book.bookName = selectorReturnerStringType(element, selectorName, attName)
        book.coverLink = selectorReturnerStringType(element, selectorCover, attCover)


        return book
    }


    override fun latestUpdatesSelector(): String? = _latestBookSelector


    override fun latestUpdatesFromElement(element: Element): Book {
        val book: Book = Book.create()

        val selectorLink = _linkLatestSelector
        val attLink = _linkAttLatestSelector
        val selectorName = _nameLatestBookSelector
        val attName = _nameAttLatestBookSelector
        val selectorCover = _bookCoverLatestBookSelector
        val attCover = _bookCoverAttLatestBookSelector

        book.link = selectorReturnerStringType(element, selectorLink, attLink)
        book.bookName = selectorReturnerStringType(element, selectorName, attName)
        book.coverLink = selectorReturnerStringType(element, selectorCover, attCover)

        return book
    }

    override fun latestUpdatesNextPageSelector(): String? = _latestNextBookSelector

    override fun latestUpdatesRequest(page: Int): Request =
        GET("$baseUrl${
            getUrlWithoutDomain(fetchLatestUpdatesEndpoint()?.replace(pageFormat,
                page.toString()) ?: "")
        }")


    override fun bookDetailsParse(document: Document): Book {
        val book = Book.create()
        val selectorBookName = _bookNameBookDetailSelector
        val attBookName = _bookNameAttBookDetailSelector
        val selectorDescription = _descriptionBookDetailBookSelector
        val attDescription = _descriptionAttBookDetailBookSelector
        val selectorAuthor = _authorBookDetailBookSelector
        val attAuthor = _authorAttBookDetailBookSelector
        val selectorCategory = _categoryBookDetailBookSelector
        val attCategory = _categoryAttBookDetailBookSelector


        book.bookName = selectorReturnerStringType(document, selectorBookName, attAuthor)
        book.author = selectorReturnerStringType(document, selectorAuthor, attBookName)
        book.description = selectorReturnerListType(document, selectorDescription, attDescription)
        book.category = selectorReturnerListType(document, selectorCategory, attCategory)

        book.source = name
        return book
    }


    override fun hasNextChapterSelector() = _hasNextChapterSelector

    override fun hasNextChaptersParse(document: Document): Boolean {
        if (!hasNextChapterSelector().isNullOrEmpty()) {
            return document.select(hasNextChapterSelector()!!).text()
                .contains("$_hasNextChapterValue")
        } else {
            return false
        }
    }


    override fun chapterListRequest(book: Book, page: Int): Request =
        GET(baseUrl + getUrlWithoutDomain(fetchChaptersEndpoint() ?: ""))

    override fun chapterListSelector(): String? = _chapterListSelector

    override fun chapterFromElement(element: Element): Chapter {
        val chapter = Chapter.create()

        val selectorLink = _linkChaptersSelector
        val attLink = _linkAttChaptersSelector
        val selectorName = _nameChaptersBookSelector
        val attName = _nameAttChaptersBookSelector

        chapter.link = selectorReturnerStringType(element, selectorLink, attLink)
        chapter.title = selectorReturnerStringType(element, selectorName, attName)


        return chapter
    }

    override fun chapterListNextPageSelector(): String? = _hasNextChaptersNextPageSelector
    override fun chapterListNextPageValueSelector(): String? = _hasNextChaptersNextPageValueSelector

    override fun pageContentParse(document: Document): ChapterPage {

        val contentSelector = _chapterPageContentSelector
        val contentAtt = _chapterPageContentAttSelector
        val content: MutableList<String> = mutableListOf()

        content.addAll(selectorReturnerListType(document, contentSelector, contentAtt))

        //val content = document.select("div.txt h4,p").eachText().joinToString("\n\n\n")

        return ChapterPage(content)
    }


    override fun searchBookSelector(): String? = _searchBookItemSelector

    override fun searchBookFromElement(element: Element): Book {
        val book: Book = Book.create()
        val selectorLink = _linkSearchedBookSelector
        val attLink = _linkAttSearchedBookSelector
        val selectorName = _nameSearchedBookSelector
        val attName = _nameAttSearchedBookSelector
        val selectorCover = _bookCoverSearchedBookSelector
        val attCover = _bookCoverAttSearchedBookSelector

        book.link = selectorReturnerStringType(element, selectorLink, attLink)
        book.bookName = selectorReturnerStringType(element, selectorName, attName)
        book.coverLink = selectorReturnerStringType(element, selectorCover, attCover)

        return book
    }

    override fun searchBookRequest(page: Int, query: String): Request =
        GET("$baseUrl${fetchContentEndpoint()?.replace(searchQueryFormat, query)}")

    override fun searchBookNextPageSelector(): String? = _hasNextSearchedBookNextPageSelector

    override fun searchBookNextValuePageSelector(): String? =
        _hasNextSearchedBookNextPageValueSelector

    private fun selectorReturnerStringType(
        document: Document,
        selector: String? = null,
        att: String? = null,
    ): String {
        if (selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
            return document.attr(att)
        } else if (!selector.isNullOrEmpty() && att.isNullOrEmpty()) {
            return document.select(selector).text()
        } else if (!selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
            return document.select(selector).attr(att)
        } else {
            return ""
        }
    }

    private fun selectorReturnerStringType(
        element: Element,
        selector: String? = null,
        att: String? = null,
    ): String {
        if (selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
            return element.attr(att)
        } else if (!selector.isNullOrEmpty() && att.isNullOrEmpty()) {
            return element.select(selector).text()
        } else if (!selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
            return element.select(selector).attr(att)
        } else {
            return ""
        }
    }

    private fun selectorReturnerListType(
        element: Element,
        selector: String? = null,
        att: String? = null,
    ): List<String> {
        if (selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
            return listOf(element.attr(att))
        } else if (!selector.isNullOrEmpty() && att.isNullOrEmpty()) {
            return element.select(selector).eachText()
        } else if (!selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
            return listOf(element.select(selector).attr(att))
        } else {
            return emptyList()
        }
    }

    private fun selectorReturnerListType(
        document: Document,
        selector: String? = null,
        att: String? = null,
    ): List<String> {
        if (selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
            return listOf(document.attr(att))
        } else if (!selector.isNullOrEmpty() && att.isNullOrEmpty()) {
            return document.select(selector).eachText()
        } else if (!selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
            return listOf(document.select(selector).attr(att))
        } else {
            return emptyList()
        }
    }
}