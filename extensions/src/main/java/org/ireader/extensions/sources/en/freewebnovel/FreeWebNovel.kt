//package org.ireader.extensions.sources.en.freewebnovel
//
//import io.ktor.client.request.*
//import kotlinx.coroutines.*
//import okhttp3.Headers
//import org.ireader.source.models.*
//import org.jsoup.nodes.Document
//import org.jsoup.nodes.Element
//
//class FreeWebNovel(deps: Dependencies) : ParsedHttpSource(deps) {
//
//    override val name = "FreeWebNovel.com"
//
//    override val iconUrl: String = "https://freewebnovel.com/static/freewebnovel/images/logo.png"
//
//    override val baseUrl = "https://freewebnovel.com"
//
//    override val lang = "en"
//
//
//    override fun getFilters(): FilterList {
//        return listOf()
//    }
//
//
//    override fun getListings(): List<Listing> {
//        return listOf(PopularListing(), LatestListing(), SearchListing())
//    }
//
//    override fun fetchLatestEndpoint(page: Int): String? =
//        "/latest-release-novel/$page/"
//
//    override fun fetchPopularEndpoint(page: Int): String? =
//        "/most-popular-novel/"
//
//    override fun fetchSearchEndpoint(page: Int, query: String): String? =
//        "/search/?searchkey=$query"
//
//
//    override fun headersBuilder() = Headers.Builder().apply {
//        add(
//            "User-Agent",
//            "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
//        )
//        add("Referer", baseUrl)
//        add("cache-control", "max-age=0")
//    }
//
//    override val headers: Headers = headersBuilder().build()
//
//
//    // popular
//    override fun popularRequest(page: Int): HttpRequestBuilder {
//        return HttpRequestBuilder().apply {
//            url(baseUrl + fetchPopularEndpoint(page = page))
//        }
//    }
//
//    override fun popularSelector() = "div.ul-list1 div.li-row"
//
//    override fun popularFromElement(element: Element): BookInfo {
//        val url = baseUrl + element.select("a").attr("href")
//        val title = element.select("a").attr("title")
//        val thumbnailUrl = element.select("img").attr("src")
//        return BookInfo(key = url, title = title, cover = thumbnailUrl)
//    }
//
//    override fun popularNextPageSelector() = null
//
//
//    // latest
//
//    override fun latestRequest(page: Int): HttpRequestBuilder {
//        return HttpRequestBuilder().apply {
//            url(baseUrl + fetchLatestEndpoint(page)!!)
//            headers { headers }
//        }
//    }
//
//    override fun latestSelector(): String = "div.ul-list1 div.li"
//
//
//    override fun latestFromElement(element: Element): BookInfo {
//        val title = element.select("div.txt a").attr("title")
//        val url = baseUrl + element.select("div.txt a").attr("href")
//        val thumbnailUrl = element.select("div.pic img").attr("src")
//        return BookInfo(key = url, title = title, cover = thumbnailUrl)
//    }
//
//    override fun latestNextPageSelector() = "div.ul-list1"
//
//    override fun searchSelector() = "div.ul-list1 div.li-row"
//
//    override fun searchFromElement(element: Element): BookInfo {
//        val title = element.select("div.txt a").attr("title")
//        val url = baseUrl + element.select("div.txt a").attr("href")
//        val thumbnailUrl = element.select("div.pic img").attr("src")
//        return BookInfo(key = url, title = title, cover = thumbnailUrl)
//    }
//
//    override fun searchNextPageSelector(): String? = null
//
//
//    // manga details
//    override fun detailParse(document: Document): BookInfo {
//        val title = document.select("div.m-desc h1.tit").text()
//        val cover = document.select("div.m-book1 div.pic img").text()
//        val link = baseUrl + document.select("div.cur div.wp a:nth-child(5)").attr("href")
//        val authorBookSelector = document.select("div.right a.a1").attr("title")
//        val description = document.select("div.inner p").eachText().joinToString("\n")
//        val category = document.select("div.item div.right a.a1").eachText()
//
//        return BookInfo(
//            title = title,
//            cover = cover,
//            description = description,
//            author = authorBookSelector,
//            genres = category,
//            key = link,
//        )
//    }
//
//    // chapters
//    override fun chaptersRequest(book: BookInfo): HttpRequestBuilder {
//        return HttpRequestBuilder().apply {
//            url(book.key)
//            headers { headers }
//        }
//    }
//
//    override fun chaptersSelector() = "div.m-newest2 ul.ul-list5 li"
//
//    override fun chapterFromElement(element: Element): ChapterInfo {
//        val link = baseUrl + element.select("a").attr("href").substringAfter(baseUrl)
//        val name = element.select("a").attr("title")
//
//        return ChapterInfo(name = name, key = link)
//    }
//
//    fun uniqueChaptersRequest(book: BookInfo, page: Int): HttpRequestBuilder {
//        return HttpRequestBuilder().apply {
//            url(book.key.replace("/${page - 1}.html", "").replace(".html", "")
//                .plus("/$page.html"))
//            headers { headers }
//        }
//    }
//
//    override suspend fun getChapterList(book: BookInfo): List<ChapterInfo> {
//        return kotlin.runCatching {
//            return@runCatching withContext(Dispatchers.IO) {
//                val page = client.get<Document>(chaptersRequest(book = book))
//                val maxPage = parseMaxPage(book)
//                val list = mutableListOf<Deferred<List<ChapterInfo>>>()
//                for (i in 1..maxPage) {
//                    val pChapters = async {
//                        chaptersParse(client.get<Document>(uniqueChaptersRequest(book = book,
//                            page = i)))
//                    }
//                    list.addAll(listOf(pChapters))
//                }
//                //  val request = client.get<Document>(chaptersRequest(book = book))
//
//                return@withContext list.awaitAll().flatten()
//            }
//        }.getOrThrow()
//    }
//
//    suspend fun parseMaxPage(book: BookInfo): Int {
//        val page = client.get<Document>(chaptersRequest(book = book))
//        val maxPage = page.select("#indexselect option").eachText().size
//        return maxPage
//    }
//
//
//    override fun pageContentParse(document: Document): List<String> {
//        return document.select("div.txt h4,p").eachText()
//    }
//
//    override suspend fun getContents(chapter: ChapterInfo): List<String> {
//        return pageContentParse(client.get<Document>(contentRequest(chapter)))
//    }
//
//
//    override fun contentRequest(chapter: ChapterInfo): HttpRequestBuilder {
//        return HttpRequestBuilder().apply {
//            url(chapter.key)
//            headers { headers }
//        }
//    }
//
//
//    override fun searchRequest(
//        page: Int,
//        query: String,
//        filters: List<Filter<*>>,
//    ): HttpRequestBuilder {
//        return requestBuilder(baseUrl + fetchSearchEndpoint(page = page, query = query))
//    }
//
//    override suspend fun getSearch(query: String, filters: FilterList, page: Int): BookPageInfo {
//        return searchParse(client.get<Document>(searchRequest(page, query, filters)))
//    }
//
//
//}