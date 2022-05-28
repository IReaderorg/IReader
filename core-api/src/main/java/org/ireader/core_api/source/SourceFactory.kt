package org.ireader.core_api.source

import androidx.annotation.Keep
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
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
        private val commandList: CommandList = emptyList(),
        private val exploreFetchers: List<BaseExploreFetcher>,
        private val detailFetcher: SourceFactory.Detail,
        private val chapterFetcher: SourceFactory.Chapters? = null,
        private val contentFetcher: SourceFactory.Content,
) : HttpSource(deps) {

    override fun getCommands(): CommandList {
        return commandList
    }

    class LatestListing() : Listing(name = "Latest")

    override fun getListings(): List<Listing> {
        return listOf(
                LatestListing()
        )
    }

    fun bookListParse(document: Document, elementSelector: String, nextPageSelector: String?, parser: (element: Element) -> MangaInfo): MangasPageInfo {
        val books = document.select(elementSelector).map { element ->
            parser(element)
        }

        val hasNextPage = nextPageSelector?.let { selector ->
            document.select(selector).first()
        } != null

        return MangasPageInfo(books, hasNextPage)
    }


    fun requestBuilder(
            url: String,
            block: HeadersBuilder.() -> Unit = {
                append(HttpHeaders.UserAgent, "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36")
                append(HttpHeaders.CacheControl, "max-age=0")
            }
    ): HttpRequestBuilder {
        return HttpRequestBuilder().apply {
            url(url)
            HeadersBuilder().apply(block)
        }
    }


    private val page = "{page}"
    private val query = "{query}"
    private suspend fun getLists(baseExploreFetcher: BaseExploreFetcher, page: Int, query: String = ""): MangasPageInfo {
        if (baseExploreFetcher.selector == null) return MangasPageInfo(emptyList(), false)
        val res = requestBuilder("$baseUrl${
            (baseExploreFetcher.endpoint)?.replace(this.page, page.toString())?.replace(this
                    .query, query)
        }")
        return bookListParse(
                client.get(res).asJsoup(),
                baseExploreFetcher.selector,
                baseExploreFetcher.nextPageSelector
        ) { element ->

            val title = selectorReturnerStringType(element, baseExploreFetcher.nameSelector, baseExploreFetcher.nameAtt)
            val url = selectorReturnerStringType(element, baseExploreFetcher.linkSelector, baseExploreFetcher.linkAtt)
            val thumbnailUrl = selectorReturnerStringType(element, baseExploreFetcher.coverSelector, baseExploreFetcher.coverAtt)

            MangaInfo(key = if (baseExploreFetcher.addBaseUrlToLink) baseUrl + url else url, title = title, cover = if (baseExploreFetcher.addBaseurlToCoverLink) baseUrl + thumbnailUrl else thumbnailUrl)
        }
    }


    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return exploreFetchers.getOrNull(0)?.let {
            return getLists(it, page,"")
        } ?: MangasPageInfo(emptyList(), false)

    }

    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        val sorts = filters.findInstance<Filter.Sort>()?.value?.index
        val query = filters.findInstance<Filter.Title>()?.value

        if (query != null) {
            exploreFetchers.firstOrNull { it.type == Type.Search }?.let {
                return getLists(it, page,query)
            } ?: MangasPageInfo(emptyList(), false)
        }


        if (sorts != null) {
            return exploreFetchers.filter { it.type != Type.Search }.getOrNull(sorts)?.let {
                return getLists(it, page,"")
            } ?: MangasPageInfo(emptyList(), false)
        }


        return MangasPageInfo(emptyList(), false)
    }

    fun chapterFromElement(element: Element): ChapterInfo {
        if (chapterFetcher == null) return ChapterInfo("", "")
        val link = selectorReturnerStringType(element, chapterFetcher.linkSelector, chapterFetcher.linkAtt)
        val name = selectorReturnerStringType(element, chapterFetcher.nameSelector, chapterFetcher.nameAtt)
        return ChapterInfo(name = name, key = if (chapterFetcher.addBaseUrlToLink) baseUrl + link else link)
    }

    open fun chaptersParse(document: Document): List<ChapterInfo> {
        return document.select(chapterFetcher?.selector?:"").map { chapterFromElement(it) }
    }


    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        commands.findInstance<Command.Chapter.Fetch>()?.let { command ->
            return chaptersParse(Jsoup.parse(command.html)).let { if  (chapterFetcher?.reverseChapterList == true) it.reversed() else it }
        }
        if (chapterFetcher == null) return emptyList()
        return kotlin.runCatching {
            return@runCatching withContext(Dispatchers.IO) {
                var chapters =
                        chaptersParse(
                                client.post(requestBuilder(manga.key)).asJsoup(),
                        )
                if (chapters.isEmpty()) {
                    chapters = chaptersParse(client.post(requestBuilder(manga.key)).asJsoup())
                }
                return@withContext if (chapterFetcher.reverseChapterList) chapters else chapters.reversed()
            }
        }.getOrThrow()
    }

    override fun getFilters(): FilterList {
        return filterList
    }

    fun detailParse(document: Document): MangaInfo {
        val title = selectorReturnerStringType(document, detailFetcher.nameSelector, detailFetcher.nameAtt)
        val cover = selectorReturnerStringType(document, detailFetcher.coverSelector, detailFetcher.coverAtt)
        val authorBookSelector = selectorReturnerStringType(document, detailFetcher.authorBookSelector, detailFetcher.authorBookAtt)
        val description =
                selectorReturnerListType(document, detailFetcher.descriptionSelector, detailFetcher.descriptionBookAtt).joinToString("\n\n")
        val category = selectorReturnerListType(document, detailFetcher.categorySelector, detailFetcher.categoryAtt)
        return MangaInfo(
                title = title,
                cover = cover,
                description = description,
                author = authorBookSelector,
                genres = category,
                key = ""
        )
    }

    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        commands.findInstance<Command.Detail.Fetch>()?.let {
            return detailParse(Jsoup.parse(it.html)).copy(key = it.url)
        }
        return detailParse(client.get(requestBuilder(manga.key)).asJsoup())
    }

    open suspend fun getContents(chapter: ChapterInfo): List<String> {
        return pageContentParse(client.get(requestBuilder(chapter.key)).asJsoup())
    }

    fun pageContentParse(document: Document): List<String> {
        val par = selectorReturnerListType(document, selector = contentFetcher.pageContentSelector, contentFetcher.pageContentAtt)
        val head = selectorReturnerStringType(document, selector = contentFetcher.pageTitleSelector, contentFetcher.pageTitleAtt)

        return listOf(head) + par
    }

    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        commands.findInstance<Command.Content.Fetch>()?.let { command ->
            return pageContentParse(Jsoup.parse(command.html)).map { Text(it) }
        }
        return getContents(chapter).map { Text(it) }
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


    private fun selectorReturnerStringType(
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

    fun selectorReturnerStringType(
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

    fun selectorReturnerListType(
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

    fun selectorReturnerListType(
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