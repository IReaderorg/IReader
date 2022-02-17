package org.ireader.domain.source

import android.util.Patterns
import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import org.ireader.core.utils.*
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.FilterList
import org.ireader.domain.models.source.*
import org.ireader.infinity.core.data.network.models.*
import org.ireader.source.models.BookInfo
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import kotlin.random.Random


data class SourceTower constructor(
    private val deps: Dependencies,
    override val sourceId: Long = Random.nextLong(),
    override val baseUrl: String,
    override val lang: String,
    override val name: String,
    override val creator: String,
    override val supportsMostPopular: Boolean = false,
    override val supportSearch: Boolean = false,
    override val supportsLatest: Boolean = false,
    override val iconUrl: String = "",
    val creatorNote: String? = null,
    val customSource: Boolean = false,
    val latest: Latest? = null,
    val popular: Popular? = null,
    val detail: Detail? = null,
    val search: Search? = null,
    val chapters: Chapters? = null,
    val content: Content? = null,
) : ParsedHttpSource(deps) {


    override fun getFilterList(): FilterList {
        return FilterList()
    }


    override fun headersBuilder(): Headers.Builder = Headers.Builder().apply {
        add(
            "User-Agent",
            "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
        )
        add("Referer", baseUrl)
        add("cache-control", "max-age=0")
    }

    override fun fetchLatestEndpoint(page: Int): String? = latest?.endpoint
    override fun fetchPopularEndpoint(page: Int): String? = popular?.endpoint
    override fun fetchSearchEndpoint(page: Int, query: String): String? = search?.endpoint
    override fun fetchChaptersEndpoint(): String? = chapters?.endpoint
    override fun fetchContentEndpoint(): String? = content?.endpoint


    /****************************SELECTOR*************************************************************/
    override fun popularSelector(): String = popular?.selector ?: ""
    override fun latestSelector(): String = latest?.selector ?: ""

    override fun popularNextPageSelector(): String? = popular?.nextPageSelector
    override fun latestNextPageSelector(): String? = latest?.nextPageSelector
    override fun hasNextChapterSelector() = chapters?.nextPageSelector ?: ""
    override fun searchSelector(): String = search?.selector ?: ""
    override fun searchNextPageSelector(): String = search?.nextPageSelector ?: ""


    override fun chaptersSelector(): String = chapters?.selector ?: ""


    /****************************SELECTOR*************************************************************/


    /****************************REQUESTS**********************************************************/

    override fun popularRequest(page: Int): HttpRequestBuilder {
        return requestBuilder("$baseUrl${
            getUrlWithoutDomain(popular?.endpoint?.applyPageFormat(page) ?: "")
        }")
    }


    override fun latestRequest(page: Int): HttpRequestBuilder {
        return requestBuilder("$baseUrl${
            getUrlWithoutDomain(latest?.endpoint?.applyPageFormat(page) ?: "")
        }")
    }

    var nextChapterListLink: String = ""
    override fun chaptersRequest(book: Book, page: Int): HttpRequestBuilder {
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
        return requestBuilder(baseUrl + getUrlWithoutDomain(url))
    }

    override fun searchRequest(page: Int, query: String, filters: FilterList): HttpRequestBuilder {
        return requestBuilder("$baseUrl${
            getUrlWithoutDomain(fetchSearchEndpoint(page, query)?.replace(searchQueryFormat,
                query) ?: "")
        }")
    }

    override fun contentRequest(chapter: Chapter): HttpRequestBuilder {
        return requestBuilder(baseUrl + getUrlWithoutDomain(chapter.link))
    }


    override fun detailsRequest(book: Book): HttpRequestBuilder {
        return requestBuilder(baseUrl + getUrlWithoutDomain(book.link))
    }
    /****************************REQUESTS**********************************************************/


    /****************************PARSE FROM ELEMENTS***********************************************/


    override fun popularFromElement(element: Element): BookInfo {
        val selectorLink = popular?.linkSelector
        val attLink = popular?.linkAtt
        val selectorName = popular?.nameSelector
        val attName = popular?.nameAtt
        val selectorCover = popular?.coverSelector
        val attCover = popular?.coverAtt


        val link = baseUrl + getUrlWithoutDomain(selectorReturnerStringType(element,
            selectorLink,
            attLink).shouldSubstring(popular?.addBaseUrlToLink, baseUrl))
        val title = selectorReturnerStringType(element, selectorName, attName).formatHtmlText()
        val cover = selectorReturnerStringType(element,
            selectorCover,
            attCover).shouldSubstring(popular?.addBaseurlToCoverLink, baseUrl)



        return BookInfo(link = link, title = title, cover = cover)
    }


    override fun latestFromElement(element: Element): BookInfo {

        val selectorLink = latest?.linkSelector
        val attLink = latest?.linkAtt
        val selectorName = latest?.nameSelector
        val attName = latest?.nameAtt
        val selectorCover = latest?.coverSelector
        val attCover = latest?.coverAtt

        val link = baseUrl + getUrlWithoutDomain(selectorReturnerStringType(element,
            selectorLink,
            attLink).shouldSubstring(latest?.addBaseUrlToLink, baseUrl))
        val title = selectorReturnerStringType(element, selectorName, attName).formatHtmlText()
        val cover = selectorReturnerStringType(element,
            selectorCover,
            attCover).shouldSubstring(latest?.addBaseurlToCoverLink, baseUrl)


        //Timber.e("Timber: SourceCreator" + val coverLink)

        return BookInfo(link = link, title = title, cover = cover)
    }

    override fun chapterFromElement(element: Element): Chapter {

        val selectorLink = chapters?.linkSelector
        val attLink = chapters?.linkAtt
        val selectorName = chapters?.nameSelector
        val attName = chapters?.nameAtt

        val link = baseUrl + getUrlWithoutDomain(selectorReturnerStringType(element,
            selectorLink,
            attLink).shouldSubstring(chapters?.addBaseUrlToLink, baseUrl))
        val title = selectorReturnerStringType(element, selectorName, attName).formatHtmlText()



        return Chapter(link = link, title = title, bookId = 0)
    }

    override fun searchFromElement(element: Element): BookInfo {
        val selectorLink = search?.linkSelector
        val attLink = search?.linkAtt
        val selectorName = search?.nameSelector
        val attName = search?.nameAtt
        val selectorCover = search?.coverSelector
        val attCover = search?.coverAtt

        val link = baseUrl + getUrlWithoutDomain(selectorReturnerStringType(element,
            selectorLink,
            attLink).shouldSubstring(search?.addBaseUrlToLink, baseUrl))
        val title = selectorReturnerStringType(element, selectorName, attName).formatHtmlText()
        val cover = selectorReturnerStringType(element,
            selectorCover,
            attCover).shouldSubstring(search?.addBaseurlToCoverLink, baseUrl)


        return BookInfo(link = link, title = title, cover = cover)
    }

    /****************************PARSE FROM ELEMENTS***********************************************/

    /****************************PARSE*************************************************************/

    override fun latestParse(document: Document): BooksPage {
        val books = mutableListOf<BookInfo>()
        if (this.latest?.isHtmlType == true && latest.selector != null) {
            books.addAll(document.select(latest.selector).map { element ->
                latestFromElement(element)
            })
        } else {
            try {
                val crudeJson = document.html()
                val json = JsonPath.parse(crudeJson)
                val selector = json?.read<List<Map<String, String>>>(this.latest?.selector ?: "")
                selector?.forEach { jsonObject ->
                    books.add(latestFromJson(jsonObject))
                }
            } catch (e: Exception) {
            }

        }

        val hasNextPage = latestNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null


        return BooksPage(
            books = books,
            hasNextPage = hasNextPage,
        )
    }

    override fun detailParse(document: Document): BookInfo {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)

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


            val title = selectorReturnerStringType(document, selectorBookName, attAuthor)
            val coverLink = selectorReturnerStringType(document,
                coverSelector,
                coverAtt).replace("render_isfalse", "")
            val author = selectorReturnerStringType(document, selectorAuthor, attBookName)
            val description = selectorReturnerListType(document,
                selectorDescription,
                attDescription).map { it.formatHtmlText() }.joinToString()
            val category = selectorReturnerListType(document, selectorCategory, attCategory)
            return BookInfo(
                title = title,
                cover = coverLink,
                author = author,
                description = description,
                genres = category,
                link = "")
        } else {
            val crudeJson = Jsoup.parse(document.html()).text().trim()
            val json = JsonPath.parse(crudeJson)
            val selector = json?.read<List<Map<String, String>>>(this.detail?.selector ?: "")
            var book = BookInfo(title = "", link = "")
            selector?.forEach { jsonObject ->
                book = detailFromJson(jsonObject)
            }

            return book
        }


    }


    override fun chaptersParse(document: Document): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        if (this.chapters?.isHtmlType == true) {
            chapters.addAll(document.select(chaptersSelector()).map { chapterFromElement(it) })
        } else {
            try {
                val crudeJson = Jsoup.parse(document.html()).text().trim()
                val json = JsonPath.parse(crudeJson)
                val selector = json?.read<List<Map<String, String>>>(this.chapters?.selector ?: "")
                selector?.forEach { jsonObject ->
                    chapters.add(chapterListFromJson(jsonObject))
                }
            } catch (e: Exception) {
            }

        }

        val hasNext = hasNextChaptersParse(document)

        val mChapters =
            if (this.chapters?.isChapterStatsFromFirst == true) chapters else chapters.reversed()

        return mChapters

    }

    override fun pageContentParse(document: Document): List<String> {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)

        val contentList: MutableList<String> = mutableListOf()
        if (content?.isHtmlType == true) {
            val contentSelector = content.pageContentSelector
            val contentAtt = content.pageContentAtt
            val titleSelector = content.pageTitleSelector
            val titleAtt = content.pageTitleAtt
            val title =
                selectorReturnerStringType(document, titleSelector, titleAtt).formatHtmlText()
            val page = selectorReturnerListType(document,
                contentSelector,
                contentAtt).map { it.formatHtmlText() }
            contentList.addAll(merge(listOf(title), page))
        } else {
            try {
                val crudeJson = Jsoup.parse(document.html()).text().trim()
                val json = JsonPath.parse(crudeJson)
                val selector = json?.read<List<Map<String, String>>>(this.chapters?.selector ?: "")
                selector?.forEach { jsonObject ->
                    contentList.addAll(contentFromJson(jsonObject).content)
                }
            } catch (e: Exception) {
            }
        }

        return contentList
    }

    override fun searchParse(document: Document): BooksPage {
        val isCloudflareEnable =
            document.body().allElements.text().contains(Constants.CLOUDFLARE_LOG)
        var books = mutableListOf<BookInfo>()

        if (search?.isHtmlType == true) {
            /**
             * I Add Filter Because sometimes this value contains null values
             * so the null book shows in search screen
             */
            books.addAll(document.select(searchSelector()).map { element ->
                searchFromElement(element)
            })
        } else {
            try {
                val crudeJson = Jsoup.parse(document.html()).text().trim()
                val json = JsonPath.parse(crudeJson)

                val selector = json?.read<List<Map<String, String>>>(search?.selector ?: "")
                selector?.forEach { jsonObject ->
                    books.add(searchBookFromJson(jsonObject))
                }
            } catch (e: Exception) {
            }


        }
        val hasNextPage = false


        return BooksPage(
            books,
            hasNextPage,
        )
    }

    override fun popularParse(document: Document): BooksPage {
        val books = document.select(popularSelector()).map { element ->
            popularFromElement(element)
        }

        val hasNextPage = popularNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(
            books,
            hasNextPage,
        )
    }

    /****************************PARSE FROM JSON**********************************/
    fun chapterListFromJson(jsonObject: Map<String, String>): Chapter {

        val mName = chapters?.nameSelector
        val mLink = chapters?.linkSelector
        val title = jsonObject[mName]?.formatHtmlText() ?: ""
        val link = jsonObject[mLink] ?: ""

        return Chapter(title = title, link = link, bookId = 0)
    }

    fun searchBookFromJson(jsonObject: Map<String, String>): BookInfo {

        val mName = search?.nameSelector
        val mLink = search?.linkSelector
        val mCover = search?.coverSelector
        val title = jsonObject[mName]?.formatHtmlText() ?: ""
        val link = jsonObject[mLink] ?: ""
        val coverLink = jsonObject[mCover]
        return BookInfo(title = title, link = link, cover = coverLink ?: "")
    }

    fun detailFromJson(jsonObject: Map<String, String>): BookInfo {

        val mName = detail?.nameSelector
        val mCover = detail?.coverSelector
        val mDescription = detail?.descriptionSelector
        val mAuthor = detail?.authorBookSelector
        val mCategory = detail?.categorySelector

        val title = jsonObject[mName]?.formatHtmlText() ?: ""
        val coverLink = jsonObject[mCover]
        val description = jsonObject[mDescription]?.formatHtmlText() ?: ""
        val author = jsonObject[mAuthor]?.formatHtmlText() ?: ""
        val category = listOf(jsonObject[mCategory]?.formatHtmlText() ?: "")

        return BookInfo(
            title = title,
            description = description,
            author = author,
            genres = category,
            cover = coverLink ?: "",
            link = "",
        )
    }

    fun latestFromJson(jsonObject: Map<String, String>): BookInfo {

        val mName = latest?.nameSelector
        val mLink = latest?.linkSelector
        val mCover = latest?.coverSelector
        val mId = latest?.idSelector
        val title = jsonObject[mName]?.formatHtmlText() ?: ""
        val link = jsonObject[mLink] ?: ""
        val coverLink = jsonObject[mCover]
        return BookInfo(
            title = title,
            link = link,
            cover = coverLink ?: ""
        )
    }

    fun popularFromJson(jsonObject: Map<String, String>): Book {
        val mName = popular?.nameSelector
        val mLink = popular?.linkSelector
        val mCover = popular?.coverSelector
        val title = jsonObject[mName]?.formatHtmlText() ?: ""
        val link = jsonObject[mLink] ?: ""
        val coverLink = jsonObject[mCover]
        return Book(
            title = title,
            link = link,
            cover = coverLink ?: "",
            sourceId = sourceId
        )
    }

    fun contentFromJson(jsonObject: Map<String, String>): Chapter {
        val mContent = content?.pageContentSelector

        val content = listOf(jsonObject[mContent]?.formatHtmlText() ?: "")

        return Chapter(
            link = "",
            title = "",
            content = content,
            bookId = 0
        )
    }

    /****************************PARSE FROM JSON**********************************/


    override fun hasNextChaptersParse(document: Document): Boolean {
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
    override suspend fun fetchChapters(book: Book): List<Chapter> {
        return kotlin.runCatching {
            return@runCatching withContext(Dispatchers.IO) {
                val request = client.get<Document>(chaptersRequest(book))

                return@withContext chaptersParse(request)
            }
        }.getOrThrow()
    }

    /**
     * Returns a page with a list of book. Normally it's not needed to
     * override this method.
     * @param page the page number to retrieve.
     */
    override suspend fun getPopular(page: Int): BooksPage {
        return kotlin.runCatching {
            withContext(Dispatchers.IO) {
                val request = client.get<Document>(popularRequest(page))

                var books = popularParse(request)

                return@withContext books
            }
        }.getOrThrow()


    }


    override suspend fun getLatest(page: Int): BooksPage {
        return kotlin.runCatching {
            return@runCatching withContext(Dispatchers.IO) {
                val request = client.get<Document>(latestRequest(page))
                //val request = client.get<Document>(latestRequest(page))
                return@withContext latestParse(request)
            }
        }.getOrThrow()
    }

    /**
     * Returns a book. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun getDetails(book: Book): BookInfo {
        return kotlin.runCatching {
            return@runCatching withContext(Dispatchers.IO) {
                return@withContext detailParse(client.get<Document>(detailsRequest(book)))
            }
        }.getOrThrow()

    }


    /**
     * Returns a ChapterPage. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     */
    override suspend fun getContentList(chapter: Chapter): List<String> {
        return kotlin.runCatching {
            return@runCatching withContext(Dispatchers.IO) {
                return@withContext pageContentParse(client.get<Document>(contentRequest(chapter)))
            }
        }.getOrThrow()


    }

    /**
     * Returns a BooksPage. Normally it's not needed to
     * override this method.
     *
     * @param page the page number to retrieve.
     * @param query the search query to retrieve.
     */
    override suspend fun getSearch(page: Int, query: String, filters: FilterList): BooksPage {
        return kotlin.runCatching {
            return@runCatching withContext(Dispatchers.IO) {
                return@withContext searchParse(client.get<Document>(searchRequest(page,
                    query,
                    FilterList())))
            }
        }.getOrThrow()


    }


}