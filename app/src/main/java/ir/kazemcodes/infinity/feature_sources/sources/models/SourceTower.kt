package ir.kazemcodes.infinity.feature_sources.sources.models

import android.util.Patterns
import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import com.squareup.moshi.JsonClass
import ir.kazemcodes.infinity.api_feature.network.GET
import ir.kazemcodes.infinity.api_feature.network.POST
import ir.kazemcodes.infinity.core.data.network.models.*
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.models.SourceEntity
import ir.kazemcodes.infinity.core.utils.*
import kotlinx.serialization.Serializable
import okhttp3.Headers
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import kotlin.random.Random


@Serializable
@JsonClass(generateAdapter = true)
data class SourceTower constructor(
    override val sourceId: Long = Random.nextLong(),
    override val baseUrl: String,
    override val lang: String,
    override val name: String,
    override val creator: String,
    override val supportsMostPopular: Boolean = false,
    override val supportSearch: Boolean = false,
    override val supportsLatest: Boolean = false,
    override val iconUrl: String = "",
    val creatorNote : String?=null,
    val customSource: Boolean = false,
    val latest: Latest? = null,
    val popular: Popular? = null,
    val detail: Detail? = null,
    val search: Search? = null,
    val chapters: Chapters? = null,
    val content: Content? = null,
) : ParsedHttpSource() {

    companion object {
        fun create() : Source {
            return SourceTower(0,"","","","")
        }
    }


    fun toSourceEntity() : SourceEntity {
        return SourceEntity(
            sourceId = sourceId,
            baseUrl = baseUrl,
            lang = lang,
            name = name,
            creator = creator,
            supportsMostPopular = supportsMostPopular,
            supportSearch = supportSearch,
            supportsLatest = supportsLatest,
            latest = latest,
            popular = popular,
            detail = detail,
            search = search,
            chapters = chapters,
            content = content,
            creatorNote = creatorNote,
            customSource = customSource,
            dateAdded = System.currentTimeMillis(),
            dateChanged = System.currentTimeMillis(),
            imageIcon = iconUrl
        )
    }




    override val supportContentAppView: Boolean = content?.openInWebView == false



    override fun headersBuilder(): Headers.Builder = Headers.Builder().apply {
        add(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
        )
        add("Referer", baseUrl)
        add("cache-control", "max-age=0")
    }

    override val fetchLatestEndpoint: String? = latest?.endpoint
    override val fetchPopularEndpoint: String? = popular?.endpoint
    override val fetchSearchEndpoint: String? = search?.endpoint
    override val fetchChaptersEndpoint: String? = chapters?.endpoint
    override val fetchContentEndpoint: String? = content?.endpoint


    /****************************SELECTOR*************************************************************/
    override val popularSelector: String = popular?.selector ?: ""
    override val latestSelector: String = latest?.selector ?: ""

    override val popularBookNextPageSelector: String? = popular?.nextPageSelector
    override val latestUpdatesNextPageSelector: String? = latest?.nextPageSelector
    override val hasNextChapterSelector = chapters?.nextPageSelector ?: ""
    override val searchSelector: String = search?.selector ?: ""
    override val searchBookNextPageSelector: String = search?.nextPageSelector ?: ""


    override val chaptersSelector: String = chapters?.selector ?: ""


    /****************************SELECTOR*************************************************************/


    /****************************REQUESTS**********************************************************/

    override fun popularRequest(page: Int): Request {
        return if (popular?.isGetRequestType == true) {
            GET("$baseUrl${
                getUrlWithoutDomain(popular.endpoint?.applyPageFormat(page) ?: "")
            }")
        } else {
            POST("$baseUrl${
                getUrlWithoutDomain(popular?.endpoint?.applyPageFormat(page) ?: "")
            }")
        }
    }


    override fun latestRequest(page: Int): Request {
        return if (latest?.isGetRequestType == true) {
            GET(
                "$baseUrl${
                    getUrlWithoutDomain(latest.endpoint?.applyPageFormat(page) ?: "")
                }")
        } else {
            POST("$baseUrl${
                getUrlWithoutDomain(latest?.endpoint?.applyPageFormat(page) ?: "")
            }")
        }

    }

    var nextChapterListLink: String = ""
    override fun chaptersRequest(book: Book, page: Int): Request {
        var url = book.link
        /** This condition occurs when the next chapter selector returns a link to the next chapter**/
        if (nextChapterListLink.isNotBlank()) {
            url = nextChapterListLink
        }
        if (!chapters?.endpoint.isNullOrEmpty()) {
            url = book.link.replace(chapters?.chaptersEndpointWithoutPage
                ?: "", (chapters?.endpoint ?: "").replace(pageFormat, page.toString()))
        }
        if (!chapters?.subStringSomethingAtEnd.isNullOrEmpty()) {
            url = book.link + chapters?.subStringSomethingAtEnd
        }
        if (chapters?.isGetRequestType == true) {
            return GET(baseUrl + getUrlWithoutDomain(url))
        } else {
            return POST(baseUrl + getUrlWithoutDomain(url))
        }
    }

    override fun searchRequest(page: Int, query: String): Request {
        if (search?.isGetRequestType == true) {
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

    override fun contentRequest(chapter: Chapter): Request {
        return if (chapters?.isGetRequestType == true) {
            GET(baseUrl + getUrlWithoutDomain(chapter.link), headers)
        } else {
            POST(baseUrl + getUrlWithoutDomain(chapter.link), headers)
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
            attLink).shouldSubstring(popular?.addBaseUrlToLink, baseUrl))
        book.bookName = selectorReturnerStringType(element, selectorName, attName).formatHtmlText()
        book.coverLink = selectorReturnerStringType(element, selectorCover, attCover).shouldSubstring(popular?.addBaseurlToCoverLink, baseUrl)



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
            attLink).shouldSubstring(latest?.addBaseUrlToLink, baseUrl))
        book.bookName = selectorReturnerStringType(element, selectorName, attName).formatHtmlText()
        book.coverLink = selectorReturnerStringType(element, selectorCover, attCover).shouldSubstring(latest?.addBaseurlToCoverLink, baseUrl)


            //Timber.e("Timber: SourceCreator" + book.coverLink)

        return book
    }

    override fun chapterFromElement(element: Element): Chapter {
        val chapter = Chapter.create()

        val selectorLink = chapters?.linkSelector
        val attLink = chapters?.linkAtt
        val selectorName = chapters?.nameSelector
        val attName = chapters?.nameAtt

        chapter.link = baseUrl + getUrlWithoutDomain(selectorReturnerStringType(element,
            selectorLink,
            attLink).shouldSubstring(chapters?.addBaseUrlToLink, baseUrl))
        chapter.title = selectorReturnerStringType(element, selectorName, attName).formatHtmlText()
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

        book.link = baseUrl + getUrlWithoutDomain(selectorReturnerStringType(element, selectorLink, attLink).shouldSubstring(search?.addBaseUrlToLink, baseUrl))
        book.bookName = selectorReturnerStringType(element, selectorName, attName).formatHtmlText()
        book.coverLink = selectorReturnerStringType(element, selectorCover, attCover).shouldSubstring(search?.addBaseurlToCoverLink, baseUrl)


        return book
    }

    /****************************PARSE FROM ELEMENTS***********************************************/

    /****************************PARSE*************************************************************/

    override fun latestParse(document: Document, page: Int, isWebViewMode: Boolean): BooksPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)

        val ajaxLoaded: Boolean = if (isWebViewMode) {
            latest?.ajaxSelector.isNull() || (selectorReturnerStringType(document,
                latest?.ajaxSelector).isNotEmpty() && latest?.ajaxSelector.isNotNull())
        } else {
            true
        }

        val books = mutableListOf<Book>()
        if (this.latest?.isHtmlType == true) {
            books.addAll(document.select(latest.selector).map { element ->
                latestFromElement(element)
            })
        } else {
            val crudeJson = document.html()
            val json = JsonPath.parse(crudeJson)
            val selector = json?.read<List<Map<String, String>>>(this.latest?.selector ?: "")
            selector?.forEach { jsonObject ->
                books.add(latestFromJson(jsonObject))
            }
        }

        val hasNextPage = latestUpdatesNextPageSelector?.let { selector ->
            document.select(selector).first()
        } != null
        if (latest?.supportPageList == true) {
            nextChapterListLink = parseNextChapterListType(document, page)
        }

        return BooksPage(books,
            hasNextPage,
            document.body().allElements.text(),
            ajaxLoaded = ajaxLoaded,
            errorMessage = if (isCloudflareEnable) Constants.CLOUDFLARE_PROTECTION_ERROR else "",
            )
    }

    override fun detailParse(document: Document, isWebViewMode: Boolean): BookPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)

        val ajaxLoaded: Boolean = if (isWebViewMode) {
            detail?.ajaxSelector.isNull() || (selectorReturnerStringType(document,
                detail?.ajaxSelector).isNotEmpty() && detail?.ajaxSelector.isNotNull())
        } else {
            true
        }
        var book = Book.create()
        if (this.detail?.isHtmlType == true) {
            val selectorBookName = detail.nameSelector
            val attBookName = detail.nameAtt
            val coverSelector = detail.coverSelector
            val coverAtt = detail.coverAtt
            val selectorDescription = detail.descriptionSelector
            val attDescription = detail.descriptionBookAtt
            val selectorAuthor = detail.authorBookSelector
            val attAuthor = detail.authorBookAtt
            val selectorCategory = detail.categorySelector
            val attCategory = detail.categoryAtt



            book.bookName = selectorReturnerStringType(document, selectorBookName, attAuthor)
            book.coverLink = selectorReturnerStringType(document,
                coverSelector,
                coverAtt).replace("render_isfalse", "")
            book.author = selectorReturnerStringType(document, selectorAuthor, attBookName)
            book.description = selectorReturnerListType(document,
                selectorDescription,
                attDescription).map { it.formatHtmlText() }
            book.category = selectorReturnerListType(document, selectorCategory, attCategory)
        } else {
            val crudeJson = Jsoup.parse(document.html()).text().trim()
            val json = JsonPath.parse(crudeJson)
            val selector = json?.read<List<Map<String, String>>>(this.detail?.selector ?: "")
            selector?.forEach { jsonObject ->
                book = detailFromJson(jsonObject)
            }
        }
        book.sourceId = sourceId


        return BookPage(book,
            errorMessage = if (isCloudflareEnable) Constants.CLOUDFLARE_PROTECTION_ERROR else "",
            ajaxLoaded = ajaxLoaded)
    }


    override fun chaptersParse(document: Document, isWebViewMode: Boolean): ChaptersPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)


        val ajaxLoaded = if (isWebViewMode) {
            chapters?.ajaxSelector.isNull() || (selectorReturnerStringType(document,
                chapters?.ajaxSelector).isNotEmpty() && chapters?.ajaxSelector.isNotNull())
        } else {
            true
        }


        val chapters = mutableListOf<Chapter>()
        if (this.chapters?.isHtmlType == true) {

            chapters.addAll(document.select(chaptersSelector).map { chapterFromElement(it) })
        } else {
            val crudeJson = Jsoup.parse(document.html()).text().trim()
            val json = JsonPath.parse(crudeJson)
            val selector = json?.read<List<Map<String, String>>>(this.chapters?.selector ?: "")
            selector?.forEach { jsonObject ->
                chapters.add(chapterListFromJson(jsonObject))
            }
        }

        val hasNext = hasNextChaptersParse(document)


        return ChaptersPage(chapters,
            hasNext,
            errorMessage = if (isCloudflareEnable) Constants.CLOUDFLARE_PROTECTION_ERROR else "",
            ajaxLoaded = ajaxLoaded)
    }

    override fun contentFromElementParse(document: Document, isWebViewMode: Boolean): ChapterPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)

        val ajaxLoaded: Boolean = if (isWebViewMode) {
            content?.ajaxSelector.isNull() || (selectorReturnerStringType(document,
                content?.ajaxSelector).isNotEmpty() && content?.ajaxSelector.isNotNull())
        } else {
            true
        }
        val contentList: MutableList<String> = mutableListOf()
        if (content?.isHtmlType == true) {
            val contentSelector = content.pageContentSelector
            val contentAtt = content.pageContentAtt
            val titleSelector = content.pageTitleSelector
            val titleAtt = content.pageTitleAtt
            val title = selectorReturnerStringType(document, titleSelector, titleAtt).formatHtmlText()
            val page = selectorReturnerListType(document, contentSelector, contentAtt).map { it.formatHtmlText() }
            contentList.addAll(merge(listOf(title), page))
        } else {
            val crudeJson = Jsoup.parse(document.html()).text().trim()
            val json = JsonPath.parse(crudeJson)
            val selector = json?.read<List<Map<String, String>>>(this.chapters?.selector ?: "")
            selector?.forEach { jsonObject ->
                contentList.addAll(contentFromJson(jsonObject).content)
            }
        }
        return ChapterPage(contentList,
            errorMessage = if (isCloudflareEnable) Constants.CLOUDFLARE_PROTECTION_ERROR else "",
            ajaxLoaded = ajaxLoaded)
    }

    override fun searchParse(document: Document, page: Int, isWebViewMode: Boolean): BooksPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)
        val ajaxLoaded: Boolean = if (isWebViewMode) {
            search?.ajaxSelector.isNull() || (selectorReturnerStringType(document,
                search?.ajaxSelector).isNotEmpty() && search?.ajaxSelector.isNotNull())
        } else {
            true
        }
        var books = mutableListOf<Book>()

        if (search?.isHtmlType == true) {
            /**
             * I Add Filter Because sometimes this value contains null values
             * so the null book shows in search screen
             */
            books.addAll(document.select(searchSelector).map { element ->
                searchFromElement(element)
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

        return BooksPage(
            books,
            hasNextPage,
            ajaxLoaded = ajaxLoaded,
            errorMessage = if (isCloudflareEnable) Constants.CLOUDFLARE_PROTECTION_ERROR else "",
        )
    }

    override fun popularParse(document: Document, page: Int, isWebViewMode: Boolean): BooksPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)
        val books = document.select(popularSelector).map { element ->
            popularFromElement(element)
        }

        val hasNextPage = popularBookNextPageSelector?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(
            books,
            hasNextPage,
            errorMessage = if (isCloudflareEnable) Constants.CLOUDFLARE_PROTECTION_ERROR else "",
        )
    }

    /****************************PARSE FROM JSON**********************************/
    fun chapterListFromJson(jsonObject: Map<String, String>): Chapter {
        val chapter = Chapter.create()
        val mName = chapters?.nameSelector
        val mLink = chapters?.linkSelector
        chapter.title = jsonObject[mName]?.formatHtmlText() ?: ""
        chapter.link = jsonObject[mLink] ?: ""
        chapter.haveBeenRead = false
        return chapter
    }

    fun searchBookFromJson(jsonObject: Map<String, String>): Book {
        val book = Book.create()
        val mName = search?.nameSelector
        val mLink = search?.linkSelector
        val mCover = search?.coverSelector
        book.bookName = jsonObject[mName]?.formatHtmlText() ?: ""
        book.link = jsonObject[mLink] ?: ""
        book.coverLink = jsonObject[mCover]
        return book
    }

    fun detailFromJson(jsonObject: Map<String, String>): Book {
        val book = Book.create()
        val mName = detail?.nameSelector
        val mCover = detail?.coverSelector
        val mDescription = detail?.descriptionSelector
        val mAuthor = detail?.authorBookSelector
        val mCategory = detail?.categorySelector

        book.bookName = jsonObject[mName]?.formatHtmlText() ?: ""
        book.coverLink = jsonObject[mCover]
        book.description = listOf(jsonObject[mDescription] ?: "").map { it.formatHtmlText() }
        book.author = jsonObject[mAuthor]?.formatHtmlText() ?:""
        book.category = listOf(jsonObject[mCategory]?.formatHtmlText() ?: "")


        return book
    }

    fun latestFromJson(jsonObject: Map<String, String>): Book {
        val book = Book.create()
        val mName = latest?.nameSelector
        val mLink = latest?.linkSelector
        val mCover = latest?.coverSelector
        val mId = latest?.idSelector
        book.bookName = jsonObject[mName]?.formatHtmlText() ?: ""
        book.link = jsonObject[mLink] ?: ""
        book.coverLink = jsonObject[mCover]
        return book
    }

    fun popularFromJson(jsonObject: Map<String, String>): Book {
        val book = Book.create()
        val mName = popular?.nameSelector
        val mLink = popular?.linkSelector
        val mCover = popular?.coverSelector
        book.bookName = jsonObject[mName]?.formatHtmlText() ?: ""
        book.link = jsonObject[mLink] ?: ""
        book.coverLink = jsonObject[mCover]
        return book
    }

    fun contentFromJson(jsonObject: Map<String, String>): Chapter {

        val chapter = Chapter.create()

        val mContent = content?.pageContentSelector

        chapter.content = listOf(jsonObject[mContent]?.formatHtmlText()?: "")

        return chapter
    }

    /****************************PARSE FROM JSON**********************************/


    override fun hasNextChaptersParse(document: Document, isWebViewMode: Boolean): Boolean {
        if (chapters?.supportNextPagesList == true) {
            val docs = selectorReturnerStringType(document,
                chapters.nextPageSelector,
                chapters.nextPageAtt).shouldSubstring(chapters.addBaseUrlToLink,
                baseUrl,
                ::getUrlWithoutDomain)
            val condition =
                Patterns.WEB_URL.matcher(docs)
                    .matches() || docs.contains(chapters.nextPageValue
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
        val selector = latest?.nextPageSelector
        val att = latest?.nextPageAtt
        val maxIndex = latest?.maxPageIndex
        val urlList = selectorReturnerListType(document,
            selector = selector,
            att)
        return urlList[page % (maxIndex ?: 0)]
    }

    /**
     * Fetchers
     */
    override suspend fun fetchChapters(book: Book, page: Int): ChaptersPage {
        val request = client.call(chaptersRequest(book, page))
        var chapters = chapterListParse(request)
        if (chapters.errorMessage.isNotBlank() || request.code != 200 || !chapters.ajaxLoaded) {
            chapters =
                chaptersParse(network.getHtmlFromWebView(baseUrl + getUrlWithoutDomain(book.link),
                    this.chapters?.ajaxSelector), isWebViewMode = true)
        }

        return chapters.copy(if (this.chapters?.isChapterStatsFromFirst == true) chapters.chapters else chapters.chapters.reversed() )
    }

    /**
     * Returns a page with a list of book. Normally it's not needed to
     * override this method.
     * @param page the page number to retrieve.
     */
    override suspend fun fetchPopular(page: Int): BooksPage {
        val request = client.call(popularRequest(page))
        var books = popularParse(request, page = page)
        if (books.errorMessage.isNotBlank() || request.code != 200 || !books.ajaxLoaded) {
            books =
                popularParse(document = network.getHtmlFromWebView(baseUrl + fetchPopularEndpoint?.applyPageFormat(
                    page)), page = page, isWebViewMode = true)
        }
        return books
    }

    /**
     * Parses the response from the site and returns a [BooksPage] object.
     *
     * @param response the response from the site.
     */
    private fun popularParse(
        response: Response,
        page: Int,
        isWebViewMode: Boolean = false,
    ): BooksPage {
        return popularParse(response.asJsoup(), page)
    }

    override suspend fun fetchLatest(page: Int): BooksPage {
        val request = client.call(latestRequest(page))
        var books = latestParse(request, page = page)
        if (books.errorMessage.isNotBlank() || request.code != 200 || !books.ajaxLoaded) {
            books =
                latestParse(network.getHtmlFromWebView(baseUrl + fetchLatestEndpoint?.applyPageFormat(
                    page)), page = page, isWebViewMode = true)
        }

        return books
    }

    /**
     * Returns a book. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun fetchBook(book: Book): BookPage {
        val request = client.call(detailsRequest(book))
        var completebook = detailParse(client.call(detailsRequest(book)))
        if (completebook.errorMessage.isNotBlank() || request.code != 200 || !completebook.ajaxLoaded) {
            completebook =
                detailParse(network.getHtmlFromWebView(baseUrl + getUrlWithoutDomain(book.link)),
                    isWebViewMode = true)
        }
        return completebook
    }


    /**
     * Returns a ChapterPage. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun fetchContent(chapter: Chapter): ChapterPage {
        val request = client.call(contentRequest(chapter))
        var content = pageContentParse(request)

        if (content.errorMessage.isNotBlank() || request.code != 200 || !content.ajaxLoaded) {
            content =
                contentFromElementParse(network.getHtmlFromWebView(baseUrl + getUrlWithoutDomain(
                    chapter.link)),
                    isWebViewMode = true)
        }
        return content
    }

    /**
     * Returns a BooksPage. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     * @param query the search query to retrieve.
     */
    override suspend fun fetchSearch(page: Int, query: String): BooksPage {
        val request = client.call(searchRequest(page, query))
        var books = searchBookParse(request, page)
        if (books.errorMessage.isNotBlank() || request.code != 200 || !books.ajaxLoaded) {
            books =
                searchParse(network.getHtmlFromWebView(baseUrl + fetchSearchEndpoint?.applySearchFormat(query, page)), page = page, isWebViewMode = true)
        }
        return books
    }


}