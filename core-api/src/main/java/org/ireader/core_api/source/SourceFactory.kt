package org.ireader.core_api.source

import androidx.annotation.Keep
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ireader.core_api.source.model.ChapterInfo
import org.ireader.core_api.source.model.Command
import org.ireader.core_api.source.model.CommandList
import org.ireader.core_api.source.model.Filter
import org.ireader.core_api.source.model.FilterList
import org.ireader.core_api.source.model.Listing
import org.ireader.core_api.source.model.MangaInfo
import org.ireader.core_api.source.model.MangasPageInfo
import org.ireader.core_api.source.model.Page
import org.ireader.core_api.source.model.Text
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

@Keep
open class SourceFactory(
    private val deps: Dependencies,
    override val lang: String,
    override val baseUrl: String,
    override val id: Long,
    override val name: String,
    val filterList: FilterList = emptyList(),
    val commandList: CommandList = emptyList(),
    val exploreFetchers: List<BaseExploreFetcher>,
    val detailFetcher: SourceFactory.Detail,
    val chapterFetcher: SourceFactory.Chapters? = null,
    val contentFetcher: SourceFactory.Content,
) : HttpSource(deps) {

    override fun getCommands(): CommandList {
        return commandList
    }

    class LatestListing() : Listing(name = "Latest")

    open fun getCustomBaseUrl(): String = baseUrl

    override fun getListings(): List<Listing> {
        return listOf(
            LatestListing()
        )
    }

    open fun bookListParse(
        document: Document,
        elementSelector: String,
        nextPageSelector: String?,
        parser: (element: Element) -> MangaInfo
    ): MangasPageInfo {
        val books = document.select(elementSelector).map { element ->
            parser(element)
        }

        val hasNextPage = nextPageSelector?.let { selector ->
            document.select(selector).first()
        } != null

        return MangasPageInfo(books, hasNextPage)
    }

    open fun getUserAgent() =
        "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"

    open fun HttpRequestBuilder.headersBuilder(block: HeadersBuilder.() -> Unit = {
        append(HttpHeaders.UserAgent, getUserAgent())
        append(HttpHeaders.CacheControl, "max-age=0")
    }) {
        headers(block)
    }

    open fun requestBuilder(
        url: String,
        block: HeadersBuilder.() -> Unit = {
            append(HttpHeaders.UserAgent, getUserAgent())
            append(HttpHeaders.CacheControl, "max-age=0")
        }
    ): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(url)
            headers(block)
        }
    }

    open val page = "{page}"
    open val query = "{query}"

    open suspend fun getListRequest(
        baseExploreFetcher: BaseExploreFetcher,
        page: Int,
        query: String = "",
        key: String
    ): Document {
        val res = requestBuilder(
            "${getCustomBaseUrl()}${
                (baseExploreFetcher.endpoint)?.replace(this.page, page.toString())?.replace(
                    this
                        .query, query
                )
            }"
        )
        return  client.get(res).asJsoup()
    }

    open suspend fun getLists(
        baseExploreFetcher: BaseExploreFetcher,
        page: Int,
        query: String = "",
        key: String,
        filters: FilterList,
    ): MangasPageInfo {
        if (baseExploreFetcher.selector == null) return MangasPageInfo(emptyList(), false)
        return bookListParse(
            getListRequest(baseExploreFetcher, page, query, key),
            baseExploreFetcher.selector,
            baseExploreFetcher.nextPageSelector
        ) { element ->

            val title = selectorReturnerStringType(
                element,
                baseExploreFetcher.nameSelector,
                baseExploreFetcher.nameAtt
            )
            val url = selectorReturnerStringType(
                element,
                baseExploreFetcher.linkSelector,
                baseExploreFetcher.linkAtt
            )
            val thumbnailUrl = selectorReturnerStringType(
                element,
                baseExploreFetcher.coverSelector,
                baseExploreFetcher.coverAtt
            )

            MangaInfo(
                key = if (baseExploreFetcher.addBaseUrlToLink) baseUrl + url else url,
                title = title,
                cover = if (baseExploreFetcher.addBaseurlToCoverLink) baseUrl + thumbnailUrl else thumbnailUrl
            )
        }
    }

    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return exploreFetchers.firstOrNull { it.type != Type.Search }?.let {
            return getLists(it, page, "", it.key, emptyList())
        } ?: MangasPageInfo(emptyList(), false)
    }

    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        val sorts = filters.findInstance<Filter.Sort>()?.value?.index
        val query = filters.findInstance<Filter.Title>()?.value

        if (query != null) {
            exploreFetchers.firstOrNull { it.type == Type.Search }?.let {
                return getLists(it, page, query,it.key,filters)
            } ?: MangasPageInfo(emptyList(), false)
        }


        if (sorts != null) {
            return exploreFetchers.filter { it.type != Type.Search }.getOrNull(sorts)?.let {
                return getLists(it, page, "",it.key,filters)
            } ?: MangasPageInfo(emptyList(), false)
        }


        return MangasPageInfo(emptyList(), false)
    }

    open fun chapterFromElement(element: Element): ChapterInfo {
        if (chapterFetcher == null) return ChapterInfo("", "")
        val link =
            selectorReturnerStringType(element, chapterFetcher.linkSelector, chapterFetcher.linkAtt)
        val name =
            selectorReturnerStringType(element, chapterFetcher.nameSelector, chapterFetcher.nameAtt)
        val translator =
            selectorReturnerStringType(element, chapterFetcher.translatorSelector, chapterFetcher.translatorAtt)

        val releaseDate = selectorReturnerStringType(element,chapterFetcher.releaseDateSelector,chapterFetcher.releaseDateAtt).let {
            chapterFetcher.releaseDateParser(it)
        }
        val number = selectorReturnerStringType(element,chapterFetcher.numberSelector,chapterFetcher.numberAtt).let {
            kotlin.runCatching {
                it.toFloat()
            }.getOrDefault(-1f)
        }
        return ChapterInfo(
            name = name,
            key = if (chapterFetcher.addBaseUrlToLink) baseUrl + link else link,
            number = number,
            dateUpload = releaseDate,
            scanlator = translator
        )
    }

    open fun chaptersParse(document: Document): List<ChapterInfo> {
        return document.select(chapterFetcher?.selector ?: "").map { chapterFromElement(it) }
    }

    open suspend fun getChapterListRequest(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): Document {
        return client.get(requestBuilder(manga.key)).asJsoup()
    }

    override suspend fun getChapterList(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): List<ChapterInfo> {
        commands.findInstance<Command.Chapter.Fetch>()?.let { command ->
            return chaptersParse(Jsoup.parse(command.html)).let { if (chapterFetcher?.reverseChapterList == true) it.reversed() else it }
        }
        if (chapterFetcher == null) return emptyList()
        return kotlin.runCatching {
            return@runCatching withContext(Dispatchers.IO) {
                val chapters =
                    chaptersParse(
                        getChapterListRequest(manga, commands),
                    )
                return@withContext if (chapterFetcher.reverseChapterList) chapters else chapters.reversed()
            }
        }.getOrThrow()
    }

    override fun getFilters(): FilterList {
        return filterList
    }

    open fun statusParser(text: String): Int {
        return detailFetcher.status.getOrDefault(text, MangaInfo.UNKNOWN)
    }

    open fun detailParse(document: Document): MangaInfo {
        val title =
            selectorReturnerStringType(document, detailFetcher.nameSelector, detailFetcher.nameAtt)
        val cover = selectorReturnerStringType(
            document,
            detailFetcher.coverSelector,
            detailFetcher.coverAtt
        )
        val authorBookSelector = selectorReturnerStringType(
            document,
            detailFetcher.authorBookSelector,
            detailFetcher.authorBookAtt
        )
        val status = statusParser(
            selectorReturnerStringType(
                document,
                detailFetcher.statusSelector,
                detailFetcher.statusAtt
            )
        )

        val description =
            selectorReturnerListType(
                document,
                detailFetcher.descriptionSelector,
                detailFetcher.descriptionBookAtt
            ).joinToString("\n\n")
        val category = selectorReturnerListType(
            document,
            detailFetcher.categorySelector,
            detailFetcher.categoryAtt
        )
        return MangaInfo(
            title = title,
            cover = cover,
            description = description,
            author = authorBookSelector,
            genres = category,
            status = status,
            key = "",
        )
    }

    open suspend fun getMangaDetailsRequest(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): Document {
        return client.get(requestBuilder(manga.key)).asJsoup()
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        commands.findInstance<Command.Detail.Fetch>()?.let {
            return detailParse(Jsoup.parse(it.html)).copy(key = it.url)
        }
        return detailParse(getMangaDetailsRequest(manga, commands))
    }

    open suspend fun getContentRequest(chapter: ChapterInfo, commands: List<Command<*>>): Document {
        return client.get(requestBuilder(chapter.key)).asJsoup()
    }

    open suspend fun getContents(chapter: ChapterInfo, commands: List<Command<*>>): List<String> {
        return pageContentParse(getContentRequest(chapter, commands))
    }

    open fun pageContentParse(document: Document): List<String> {
        val par = selectorReturnerListType(
            document,
            selector = contentFetcher.pageContentSelector,
            contentFetcher.pageContentAtt
        )
        val head = selectorReturnerStringType(
            document,
            selector = contentFetcher.pageTitleSelector,
            contentFetcher.pageTitleAtt
        )

        return listOf(head) + par
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        commands.findInstance<Command.Content.Fetch>()?.let { command ->
            return pageContentParse(Jsoup.parse(command.html)).map { Text(it) }
        }
        return getContents(chapter,commands).map { Text(it) }
    }

    data class BaseExploreFetcher(
        val key: String,
        val endpoint: String? = null,
        val selector: String? = null,
        val addBaseUrlToLink: Boolean = false,
        val nextPageSelector: String? = null,
        val nextPageAtt: String? = null,
        val nextPageValue: String? = null,
        val addToStringEnd: String? = null,
        val addBaseurlToCoverLink: Boolean = false,
        val linkSelector: String? = null,
        val linkAtt: String? = null,
        val nameSelector: String? = null,
        val nameAtt: String? = null,
        val coverSelector: String? = null,
        val coverAtt: String? = null,
        val type: Type = Type.Others,
    )

    data class Detail(
        val addBaseUrlToLink: Boolean = false,
        val addBaseurlToCoverLink: Boolean = false,
        val nameSelector: String? = null,
        val nameAtt: String? = null,
        val coverSelector: String? = null,
        val coverAtt: String? = null,
        val descriptionSelector: String? = null,
        val descriptionBookAtt: String? = null,
        val authorBookSelector: String? = null,
        val authorBookAtt: String? = null,
        val categorySelector: String? = null,
        val categoryAtt: String? = null,
        val statusSelector: String? = null,
        val statusAtt: String? = null,
        val status: Map<String, Int> = emptyMap<String, Int>(),
        val type: Type = Type.Detail,
    )

    data class Chapters(
        val selector: String? = null,
        val addBaseUrlToLink: Boolean = false,
        val nextPageSelector: String? = null,
        val nextPageAtt: String? = null,
        val reverseChapterList: Boolean = true,
        val linkSelector: String? = null,
        val linkAtt: String? = null,
        val nameSelector: String? = null,
        val nameAtt: String? = null,
        val numberSelector:String?=null,
        val numberAtt:String?=null,
        val releaseDateSelector:String?=null,
        val releaseDateAtt:String?=null,
        val releaseDateParser:(String) -> Long = { 0L },
        val translatorSelector:String?=null,
        val translatorAtt:String?=null,
        val type: Type = Type.Chapters,
    )

    data class Content(
        val pageTitleSelector: String? = null,
        val pageTitleAtt: String? = null,
        val pageContentSelector: String? = null,
        val pageContentAtt: String? = null,
        val type: Type = Type.Content,
    )

    enum class Type {
        Search,
        Detail,
        Chapters,
        Content,
        Others
    }

    open fun selectorReturnerStringType(
        document: Document,
        selector: String? = null,
        att: String? = null,
    ): String {
        if (selector.isNullOrBlank() && !att.isNullOrBlank()) {
            return document.attr(att)
        } else if (!selector.isNullOrBlank() && att.isNullOrBlank()) {
            return document.select(selector).text()
        } else if (!selector.isNullOrBlank() && !att.isNullOrBlank()) {
            return document.select(selector).attr(att)
        } else {
            return ""
        }
    }

    open fun selectorReturnerStringType(
        element: Element,
        selector: String? = null,
        att: String? = null,
    ): String {
        if (selector.isNullOrBlank() && !att.isNullOrBlank()) {
            return element.attr(att)
        } else if (!selector.isNullOrBlank() && att.isNullOrBlank()) {
            return element.select(selector).text()
        } else if (!selector.isNullOrBlank() && !att.isNullOrBlank()) {
            return element.select(selector).attr(att)
        } else {
            return ""
        }
    }

    open fun selectorReturnerListType(
        element: Element,
        selector: String? = null,
        att: String? = null,
    ): List<String> {
        if (selector.isNullOrBlank() && !att.isNullOrBlank()) {
            return listOf(element.attr(att))
        } else if (!selector.isNullOrBlank() && att.isNullOrBlank()) {
            return element.select(selector).eachText()
        } else if (!selector.isNullOrBlank() && !att.isNullOrBlank()) {
            return listOf(element.select(selector).attr(att))
        } else {
            return emptyList()
        }
    }

    open fun selectorReturnerListType(
        document: Document,
        selector: String? = null,
        att: String? = null,
    ): List<String> {
        if (selector.isNullOrBlank() && !att.isNullOrBlank()) {
            return listOf(document.attr(att))
        } else if (!selector.isNullOrBlank() && att.isNullOrBlank()) {
            return document.select(selector).map {
                it.text()
            }
        } else if (!selector.isNullOrBlank() && !att.isNullOrBlank()) {
            return listOf(document.select(selector).attr(att))
        } else {
            return emptyList()
        }
    }
}