package ir.kazemcodes.infinity.sources.models

import android.content.Context
import android.util.Patterns
import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import ir.kazemcodes.infinity.api_feature.network.GET
import ir.kazemcodes.infinity.api_feature.network.POST
import ir.kazemcodes.infinity.data.network.models.*
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.presentation.book_detail.Constants
import ir.kazemcodes.infinity.util.applyPageFormat
import ir.kazemcodes.infinity.util.shouldSubstring
import okhttp3.Headers
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import timber.log.Timber


class SourceCreator(
    context: Context,
    _baseUrl: String,
    private val _lang: String,
    private val _name: String,
    private val _supportsMostPopular: Boolean = false,
    private val _supportsSearch: Boolean = false,
    private val _supportsLatest: Boolean = false,
    private val latest: Latest? = null,
    private val popular: Popular? = null,
    private val detail: Detail? = null,
    private val search: Search? = null,
    private val chapterList: ChapterList? = null,
    private val content: Content? = null,
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

    override val supportSearch: Boolean = _supportsSearch


    override fun headersBuilder(): Headers.Builder = Headers.Builder().apply {
        add(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Mobile Safari/537.36 "
        )
        add("Referer", baseUrl)
        add("cache-control", "max-age=0")
    }

    override val fetchLatestEndpoint: String? = latest?.endpoint
    override val fetchPopularEndpoint: String? = popular?.endpoint
    override val fetchSearchEndpoint: String? = search?.endpoint
    override val fetchChaptersEndpoint: String? = chapterList?.endpoint
    override val fetchContentEndpoint: String? = content?.endpoint

    override val popularSelector: String? = popular?.selector

    override fun popularBookNextPageSelector(): String? = popular?.nextBookSelector
    /****************************REQUESTS**********************************************************/

    override fun popularRequest(page: Int): Request =
        GET("$baseUrl${
            getUrlWithoutDomain(fetchPopularEndpoint?.applyPageFormat(page) ?: "")
        }")

    override fun latestRequest(page: Int): Request {
        val url = fetchLatestEndpoint?.applyPageFormat(page) ?: ""
        return GET("$baseUrl${
            getUrlWithoutDomain(url)
        }")
    }

    override fun chaptersRequest(book: Book, page: Int): Request {
        var url = book.link
        /** This condition occurs when the next chapter selector returns a link to the next chapter**/
        if (nextChapterListLink.isNotBlank()) {
            url = nextChapterListLink
        }
        if (!chapterList?.endpoint.isNullOrEmpty()) {
            url = book.link.replace(chapterList?.chaptersEndpointWithoutPage
                ?: "", (chapterList?.endpoint ?: "").replace(pageFormat, page.toString()))
        }
        if (chapterList?._shouldStringSomethingAtEnd == true && !chapterList._subStringSomethingAtEnd.isNullOrEmpty()) {
            url  = book.link + chapterList._subStringSomethingAtEnd
        }
        if (chapterList?.isGetRequest == true) {
            return GET(baseUrl + getUrlWithoutDomain(url))
        }else {
            return POST(baseUrl + getUrlWithoutDomain(url))
        }
    }
    override fun searchRequest(page: Int, query: String): Request {

        if (search?.isGetRequest == true) {
            return GET("$baseUrl${
                getUrlWithoutDomain(fetchSearchEndpoint?.replace(searchQueryFormat,
                    query) ?: "")
            }")
        } else {
            return POST(
                "$baseUrl${
                    getUrlWithoutDomain(fetchSearchEndpoint?.replace(searchQueryFormat,
                        query) ?: "")
                }")
        }

    }
    /****************************REQUESTS**********************************************************/


    /****************************PARSE FROM ELEMENTS***********************************************/



    override fun popularFromElement(element: Element): Book {
        val book: Book = Book.create()

        val selectorLink = popular?.linkSelector
        val attLink = popular?.linkAtt
        val selectorName = popular?.nameSelector
        val attName = popular?.nameAtt
        val selectorCover = popular?.coverSelector
        val attCover = popular?.coverAtt


        book.link = baseUrl + getUrlWithoutDomain(selectorReturnerStringType(element,
            selectorLink,
            attLink).shouldSubstring(popular?.linkSubString, baseUrl))
        book.bookName = selectorReturnerStringType(element, selectorName, attName)
        book.coverLink = selectorReturnerStringType(element, selectorCover, attCover)


        return book
    }


    override fun latestFromElement(element: Element): Book {
        val book: Book = Book.create()

        val selectorLink = latest?.linkSelector
        val attLink = latest?.linkAtt
        val selectorName = latest?.nameSelector
        val attName = latest?.nameAtt
        val selectorCover = latest?.coverSelector
        val attCover = latest?.coverAtt

        book.link = baseUrl + getUrlWithoutDomain(selectorReturnerStringType(element,
            selectorLink,
            attLink))
        book.bookName = selectorReturnerStringType(element, selectorName, attName)
        book.coverLink = selectorReturnerStringType(element, selectorCover, attCover)


        //Timber.e("Timber: SourceCreator" + book.coverLink)

        return book
    }
    override fun chapterFromElement(element: Element): Chapter {
        val chapter = Chapter.create()

        val selectorLink = chapterList?.linkSelector
        val attLink = chapterList?.linkAtt
        val selectorName = chapterList?.nameSelector
        val attName = chapterList?.nameAtt

        chapter.link = baseUrl + getUrlWithoutDomain(selectorReturnerStringType(element,
            selectorLink,
            attLink))
        chapter.title = selectorReturnerStringType(element, selectorName, attName)
        chapter.haveBeenRead = false


        return chapter
    }

    override fun searchFromElement(element: Element): Book {
        val book: Book = Book.create()
        val selectorLink = search?.linkSelector
        val attLink = search?.linkAtt
        val selectorName = search?.nameSelector
        val attName = search?.nameAtt
        val selectorCover = search?.coverSelector
        val attCover = search?.coverAtt

        book.link = baseUrl + getUrlWithoutDomain(selectorReturnerStringType(element,
            selectorLink,
            attLink))
        book.bookName = selectorReturnerStringType(element, selectorName, attName)
        book.coverLink = selectorReturnerStringType(element, selectorCover, attCover)

        return book
    }
    /****************************PARSE FROM ELEMENTS***********************************************/

    /****************************PARSE*************************************************************/

    override fun latestParse(document: Document, page: Int): BooksPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)

        val books = document.select(latest?.selector).map { element ->
            latestFromElement(element)
        }

        val hasNextPage = latestUpdatesNextPageSelector?.let { selector ->
            document.select(selector).first()
        } != null
        if (latest?.supportPageList == true) {
            nextChapterListLink = parseNextChapterListType(document, page)
        }

        return BooksPage(books, hasNextPage, isCloudflareEnable, document.body().allElements.text())
    }

    override fun detailParse(document: Document): BookPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)
        val book = Book.create()

        Timber.e("BookDetail: ${document.body()}")

        val selectorBookName = detail?.nameSelector
        val attBookName = detail?.nameAtt
        val coverSelector = detail?.coverSelector
        val coverAtt = detail?.coverAtt
        val selectorDescription = detail?.descriptionSelector
        val attDescription = detail?.descriptionBookAtt
        val selectorAuthor = detail?.authorBookSelector
        val attAuthor = detail?.authorBookAtt
        val selectorCategory = detail?.categorySelector
        val attCategory = detail?.categoryAtt



        book.bookName = selectorReturnerStringType(document, selectorBookName, attAuthor)
        book.coverLink = selectorReturnerStringType(document, coverSelector, coverAtt)
        book.author = selectorReturnerStringType(document, selectorAuthor, attBookName)
        book.description = selectorReturnerListType(document, selectorDescription, attDescription)
        book.category = selectorReturnerListType(document, selectorCategory, attCategory)

        book.source = name

        return BookPage(book, isCloudflareEnabled = isCloudflareEnable)
    }



    override fun chaptersParse(document: Document): ChaptersPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)
        val chapters = mutableListOf<Chapter>()
        if (chapterList?.isHtmlType == true) {
            chapters.addAll(document.select(chaptersSelector).map { chapterFromElement(it) })
        } else {
            val crudeJson = Jsoup.parse(document.html()).text().trim()
            val json = JsonPath.parse(crudeJson)
            val selector = json?.read<List<Map<String, String>>>(chapterList?.selector ?: "")
            selector?.forEach { jsonObject ->
                chapters.add(chapterListFromJson(jsonObject))
            }
        }

        val hasNext = hasNextChaptersParse(document)


        return ChaptersPage(chapters, hasNext, isCloudflareEnabled = isCloudflareEnable)
    }
    override fun contentParse(document: Document): ChapterPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)
        val contentSelector = content?.pageContentSelector
        val contentAtt = content?.pageContentAtt
        val titleSelector = content?.pageTitleSelector
        val titleAtt = content?.pageTitleAtt
        val content: MutableList<String> = mutableListOf()
        val title = selectorReturnerStringType(document,titleSelector,titleAtt)
        val page = selectorReturnerListType(document, contentSelector, contentAtt)
        content.add(title)
        content.addAll(page)

        return ChapterPage(content, isCloudflareEnabled = isCloudflareEnable)
    }

    override fun searchParse(document: Document, page: Int): BooksPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)
        var books = mutableListOf<Book>()

        if (search?.isHTMLType == true) {
            /**
             * I Add Filter Because sometimes this value contains null values
             * so the null book shows in search screen
             */
            books.addAll(document.select(searchSelector).map { element ->
                searchFromElement(element)
            }.filter {
                it.bookName.isNotBlank()
            })
        } else {
            val crudeJson = Jsoup.parse(document.html()).text().trim()
            val json = JsonPath.parse(crudeJson)
            val selector = json?.read<List<Map<String, String>>>(search?.selector ?: "")
            selector?.forEach { jsonObject ->
                books.add(searchBookFromJson(jsonObject))
            }
        }
        val hasNextPage = false

        return BooksPage(books, hasNextPage, isCloudflareEnable)
    }
    /****************************PARSE FROM JSON**********************************/
    fun chapterListFromJson(jsonObject: Map<String, String>): Chapter {
        val chapter = Chapter.create()
        val _name = chapterList?.nameSelector
        val _link = chapterList?.linkSelector
        chapter.title = jsonObject[_name]?.replace("</strong>", "") ?: ""
        chapter.link = jsonObject[_link] ?: ""
        chapter.haveBeenRead = false
        return chapter
    }
    fun searchBookFromJson(jsonObject: Map<String, String>): Book {
        val _name = search?.nameSelector
        val _link = search?.linkSelector
        val _cover = search?.coverSelector
        val name = jsonObject[_name]?.replace("</strong>", "")
        val link = jsonObject[_link]
        val cover = jsonObject[_cover]
        return Book(bookName = name ?: "", link = link ?: "", coverLink = cover)
    }
    /****************************PARSE FROM JSON**********************************/

    /****************************SELECTOR*************************************************************/
    override val latestSelector: String? = latest?.selector
    override val latestUpdatesNextPageSelector: String? = latest?.nextPageSelector
    override fun hasNextChapterSelector() = chapterList?.hasNextChapterListSelector
    override val searchSelector: String? = search?.selector
    override fun searchBookNextPageSelector(): String? =
        search?.hasNextSearchedBooksNextPageSelector

    override fun searchBookNextValuePageSelector(): String? =
        search?.hasNextSearchedBookNextPageValue
    override val chaptersSelector: String? = chapterList?.selector
    /****************************SELECTOR*************************************************************/

    var nextChapterListLink: String = ""


    override fun hasNextChaptersParse(document: Document): Boolean {
        if (chapterList?.supportNextPagesList == true) {
            val docs = selectorReturnerStringType(document,
                chapterList.hasNextChapterListSelector,
                chapterList.hasNextChapterListAtt).shouldSubstring(chapterList._shouldSubstringBaseUrlAtFirst
                ?: false,
                baseUrl,
                ::getUrlWithoutDomain)
            val condition =
                Patterns.WEB_URL.matcher(docs)
                    .matches() || docs.contains(chapterList.hasNextChapterListValue
                    ?: "")
            if (Patterns.WEB_URL.matcher(docs).matches()) {
                nextChapterListLink = baseUrl + getUrlWithoutDomain(docs)
            }
            return condition
        } else {
            return false
        }
    }

    fun parseNextChapterListType(document: Document, page: Int): String {
        val selector = latest?.nextPageLinkSelector
        val att = latest?.nextPageLinkAtt
        val maxIndex = latest?.maxPageIndex
        val urlList = selectorReturnerListType(document,
            selector = selector,
            att)
        return urlList[page % (maxIndex ?: 0)]
    }

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