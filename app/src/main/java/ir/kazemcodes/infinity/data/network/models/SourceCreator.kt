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
    private val _supportsSearch: Boolean,
    private val _latestUpdateEndpoint: String? = null,
    private val _popularEndpoint: String? = null,
    private val _searchEndpoint: String? = null,
    private val _chaptersEndpoint: String? = null,
    private val _contentEndpoint: String? = null,
    private val _popularBookSelector: String? = null,
    private val _popularNextBookSelector: String? = null,
    private val _linkPopularSelector: String? = null,
    private val _linkAttPopularSelector: String? = null,
    private val _namePopularBookSelector: String? = null,
    private val _nameAttPopularBookSelector: String? = null,
    private val _bookCoverPopularBookSelector: String? = null,
    private val _bookAttCoverPopularBookSelector: String? = null,
    private val _latestBookSelector: String? = null,
    private val _latestNextBookSelector: String? = null,
    private val _linkLatestSelector: String? = null,
    private val _linkAttLatestSelector: String? = null,
    private val _nameLatestBookSelector: String? = null,
    private val _nameAttLatestBookSelector: String? = null,
    private val _bookCoverLatestBookSelector: String? = null,
    private val _bookCoverAttLatestBookSelector: String? = null,
    private val _bookNameBookDetailSelector: String? = null,
    private val _bookNameAttBookDetailSelector: String? = null,
    private val _descriptionBookDetailBookSelector: String? = null,
    private val _descriptionAttBookDetailBookSelector: String? = null,
    private val _authorBookDetailBookSelector: String? = null,
    private val _authorAttBookDetailBookSelector: String? = null,
    private val _categoryBookDetailBookSelector: String? = null,
    private val _categoryAttBookDetailBookSelector: String? = null,
    private val _hasNextChapterSelector: String? = null,
    private val _hasNextChapterValue: String? = null,
    private val _chapterListSelector: String? = null,
    private val _linkChaptersSelector: String? = null,
    private val _linkAttChaptersSelector: String? = null,
    private val _nameChaptersBookSelector: String? = null,
    private val _nameAttChaptersBookSelector: String? = null,
    private val _hasNextChaptersNextPageSelector: String? = null,
    private val _hasNextChaptersNextPageValueSelector: String? = null,
    private val _chapterPageContentSelector: String? = null,
    private val _chapterPageContentAttSelector: String? = null,
    private val _searchBookItemSelector: String? = null,
    private val _linkSearchedBookSelector: String? = null,
    private val _linkAttSearchedBookSelector: String? = null,
    private val _nameSearchedBookSelector: String? = null,
    private val _nameAttSearchedBookSelector: String? = null,
    private val _bookCoverSearchedBookSelector: String? = null,
    private val _bookCoverAttSearchedBookSelector: String? = null,
    private val _hasNextSearchedBookNextPageSelector: String? = null,
    private val _hasNextSearchedBookNextPageValueSelector: String? = null,


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

    override val supportSearch: Boolean  = _supportsSearch


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

        book.link = baseUrl + getUrlWithoutDomain(selectorReturnerStringType(element, selectorLink, attLink))
        book.bookName = selectorReturnerStringType(element, selectorName, attName)
        book.coverLink = selectorReturnerStringType(element, selectorCover, attCover)

        return book
    }

    override fun latestUpdatesNextPageSelector(): String? = _latestNextBookSelector

    override fun latestUpdatesRequest(page: Int): Request {
        val url = fetchLatestUpdatesEndpoint()?.replace(pageFormat,
            page.toString()) ?: ""
        return GET("$baseUrl${
            getUrlWithoutDomain(url)
        }")
    }


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


    override fun chapterListRequest(book: Book, page: Int): Request {

        if (!fetchChaptersEndpoint().isNullOrEmpty()) {
            return  GET(baseUrl + getUrlWithoutDomain(fetchChaptersEndpoint() ?: ""))
        } else {
            return  GET(baseUrl + getUrlWithoutDomain(book.link))
        }
    }

    override fun chapterListSelector(): String? = _chapterListSelector

    override fun chapterFromElement(element: Element): Chapter {
        val chapter = Chapter.create()

        val selectorLink = _linkChaptersSelector
        val attLink = _linkAttChaptersSelector
        val selectorName = _nameChaptersBookSelector
        val attName = _nameAttChaptersBookSelector

        chapter.link = selectorReturnerStringType(element, selectorLink, attLink)
        chapter.title = selectorReturnerStringType(element, selectorName, attName)
        chapter.haveBeenRead = false


        return chapter
    }

    override fun chapterListNextPageSelector(): String? = _hasNextChaptersNextPageSelector
    override fun chapterListNextPageValueSelector(): String? = _hasNextChaptersNextPageValueSelector



    override fun pageContentParse(document: Document): ChapterPage {

        val contentSelector = _chapterPageContentSelector
        val contentAtt = _chapterPageContentAttSelector
        val content: MutableList<String> = mutableListOf()
        val page = selectorReturnerListType(document, contentSelector, contentAtt)

        content.addAll(page)

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
        GET("$baseUrl${
            getUrlWithoutDomain(fetchSearchBookEndpoint()?.replace(searchQueryFormat,
                query) ?: "")
        }")

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