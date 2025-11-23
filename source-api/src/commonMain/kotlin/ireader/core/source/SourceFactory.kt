package ireader.core.source

import io.ktor.client.request.*
import io.ktor.http.*
import ireader.core.http.DEFAULT_USER_AGENT
import ireader.core.source.SourceHelpers.buildAbsoluteUrl
import ireader.core.source.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/**
 * a simple class that makes Source Creation difficulty lower
 *
 * check out this site for more info [check out](https://github.com/IReaderorg/IReader/blob/master/core-api/src/main/java/org/ireader/core_api/source/SourceFactory.kt)
 */
abstract class SourceFactory(
    private val deps: Dependencies
) : HttpSource(deps) {

    /**
     * devs need to fill this if they wanted parse detail functionality
     */
    open val detailFetcher: Detail = Detail()

    /**
     * devs need to fill this if they wanted parse chapters functionality
     */
    open val chapterFetcher: Chapters = Chapters()

    /**
     * devs need to fill this if they wanted parse content functionality
     */
    open val contentFetcher: Content = Content()

    /**
     * devs need to fill this if they wanted parse explore functionality
     */
    open val exploreFetchers: List<BaseExploreFetcher> = listOf()

    class LatestListing() : Listing(name = "Latest")

    /**
     * the custom baseUrl
     */
    open fun getCustomBaseUrl(): String = baseUrl

    /**
     * the default listing, it must have a default value
     * if not the [getMangaList] return nothing
     */
    override fun getListings(): List<Listing> {
        return listOf(
            LatestListing()
        )
    }

    /**
     * parse books based on selector that is passed from [BaseExploreFetcher]
     */
    open fun bookListParse(
        document: Document,
        elementSelector: String,
        baseExploreFetcher: BaseExploreFetcher,
        parser: (element: Element) -> MangaInfo,
        page: Int,
    ): MangasPageInfo {
        // Improved: Use mapNotNull to filter out invalid manga and catch parsing errors
        val books = document.select(elementSelector).mapNotNull { element ->
            try {
                val manga = parser(element)
                // Only include valid manga with required fields
                if (manga.key.isNotBlank() && manga.title.isNotBlank()) manga else null
            } catch (e: Exception) {
                // Log error but continue parsing other elements
                null
            }
        }
        
        // Improved: More efficient hasNextPage logic with early returns
        val hasNextPage: Boolean = when {
            baseExploreFetcher.infinitePage -> true
            baseExploreFetcher.maxPage != -1 -> page < baseExploreFetcher.maxPage
            else -> {
                val nextPageText = selectorReturnerStringType(
                    document,
                    baseExploreFetcher.nextPageSelector,
                    baseExploreFetcher.nextPageAtt
                ).trim()
                
                baseExploreFetcher.nextPageValue?.let { expectedValue ->
                    nextPageText == expectedValue
                } ?: nextPageText.isNotBlank()
            }
        }

        return MangasPageInfo(books, hasNextPage)
    }

    /**
     * default user agent for requests
     */
    open fun getUserAgent() = DEFAULT_USER_AGENT

    /**
     * simple header builder
     */
    open fun HttpRequestBuilder.headersBuilder(
        block: HeadersBuilder.() -> Unit = {
            append(HttpHeaders.UserAgent, getUserAgent())
            append(HttpHeaders.CacheControl, "max-age=0")
        }
    ) {
        headers(block)
    }

    /**
     * the current request builder to make  ktor request easier to write
     */
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

    /**
     * the request for each [BaseExploreFetcher]
     */
    open suspend fun getListRequest(
        baseExploreFetcher: BaseExploreFetcher,
        page: Int,
        query: String = "",
    ): Document {
        // Improved: Build URL more cleanly and safely
        val endpoint = baseExploreFetcher.endpoint ?: ""
        val processedQuery = baseExploreFetcher.onQuery(query)
        val processedPage = baseExploreFetcher.onPage(page.toString())
        
        val finalUrl = buildString {
            append(getCustomBaseUrl())
            append(endpoint
                .replace(this@SourceFactory.page, processedPage)
                .replace(this@SourceFactory.query, processedQuery)
            )
        }
        
        val request = requestBuilder(finalUrl)
        return client.get(request).asJsoup()
    }

    /**
     * parse the documents based on selector that is passes from [BaseExploreFetcher]
     */
    open suspend fun getLists(
        baseExploreFetcher: BaseExploreFetcher,
        page: Int,
        query: String = "",
        filters: FilterList,
    ): MangasPageInfo {
        // Improved: Early return with null check
        val selector = baseExploreFetcher.selector ?: return MangasPageInfo(emptyList(), false)
        
        val document = getListRequest(baseExploreFetcher, page, query)
        
        return bookListParse(
            document,
            selector,
            page = page,
            baseExploreFetcher = baseExploreFetcher,
            parser = { element ->
                // Improved: Extract and process data more cleanly
                val rawTitle = selectorReturnerStringType(
                    element,
                    baseExploreFetcher.nameSelector,
                    baseExploreFetcher.nameAtt
                ).trim()
                val title = baseExploreFetcher.onName(rawTitle, baseExploreFetcher.key)
                
                val rawUrl = selectorReturnerStringType(
                    element,
                    baseExploreFetcher.linkSelector,
                    baseExploreFetcher.linkAtt
                ).trim()
                val processedUrl = baseExploreFetcher.onLink(rawUrl, baseExploreFetcher.key)
                val url = if (baseExploreFetcher.addBaseUrlToLink) {
                    // Improved: Better URL joining
                    buildAbsoluteUrl(baseUrl, processedUrl)
                } else {
                    processedUrl
                }
                
                val rawCover = selectorReturnerStringType(
                    element,
                    baseExploreFetcher.coverSelector,
                    baseExploreFetcher.coverAtt
                ).trim()
                val processedCover = baseExploreFetcher.onCover(rawCover, baseExploreFetcher.key)
                val cover = if (baseExploreFetcher.addBaseurlToCoverLink) {
                    buildAbsoluteUrl(baseUrl, processedCover)
                } else {
                    processedCover
                }

                MangaInfo(
                    key = url,
                    title = title,
                    cover = cover
                )
            }
        )
    }

    /**
     * this function is the first funciton that app request,
     * @param sort the sorts which users takes comes from [getListings] currently it does nothing in the main app
     * @param page current page
     * @return [MangasPageInfo]
     */
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        // Improved: Find first non-search fetcher more efficiently
        val fetcher = exploreFetchers.firstOrNull { it.type != Type.Search }
            ?: return MangasPageInfo(emptyList(), false)
        
        return getLists(fetcher, page, "", emptyList())
    }

    /**
     * @param filters filters that users passed over to the source
    this filters comes from the [getFilters]
     * @param page current page
     * @return [MangasPageInfo]
     */
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        // Improved: Extract filter values once
        val titleFilter = filters.findInstance<Filter.Title>()
        val sortFilter = filters.findInstance<Filter.Sort>()
        
        val query = titleFilter?.value?.takeIf { it.isNotBlank() }
        val sortIndex = sortFilter?.value?.index
        
        // Improved: Handle search query first (most common case)
        if (query != null) {
            val searchFetcher = exploreFetchers.firstOrNull { it.type == Type.Search }
                ?: return MangasPageInfo(emptyList(), false)
            return getLists(searchFetcher, page, query, filters)
        }

        // Improved: Handle sort selection
        if (sortIndex != null) {
            val nonSearchFetchers = exploreFetchers.filter { it.type != Type.Search }
            val sortFetcher = nonSearchFetchers.getOrNull(sortIndex)
                ?: return MangasPageInfo(emptyList(), false)
            return getLists(sortFetcher, page, "", filters)
        }

        return MangasPageInfo(emptyList(), false)
    }

    /**
     * a function that parse each elements that is passed from [chaptersParse] and return a [ChapterInfo]
     */
    open fun chapterFromElement(element: Element): ChapterInfo {
        // Improved: Extract and process data more cleanly with trimming
        val rawLink = selectorReturnerStringType(
            element,
            chapterFetcher.linkSelector,
            chapterFetcher.linkAtt
        ).trim()
        val link = chapterFetcher.onLink(rawLink)
        
        val rawName = selectorReturnerStringType(
            element,
            chapterFetcher.nameSelector,
            chapterFetcher.nameAtt
        ).trim()
        val name = chapterFetcher.onName(rawName)
        
        val rawTranslator = selectorReturnerStringType(
            element,
            chapterFetcher.translatorSelector,
            chapterFetcher.translatorAtt
        ).trim()
        val translator = chapterFetcher.onTranslator(rawTranslator)

        val rawDate = selectorReturnerStringType(
            element,
            chapterFetcher.uploadDateSelector,
            chapterFetcher.uploadDateAtt
        ).trim()
        val releaseDate = chapterFetcher.uploadDateParser(rawDate)
        
        val rawNumber = selectorReturnerStringType(
            element,
            chapterFetcher.numberSelector,
            chapterFetcher.numberAtt
        ).trim()
        val processedNumber = chapterFetcher.onNumber(rawNumber)
        // Improved: Better number parsing with fallback
        val number = processedNumber.toFloatOrNull() ?: -1f
        
        // Improved: Better URL building
        val finalLink = if (chapterFetcher.addBaseUrlToLink) {
            getAbsoluteUrl(baseUrl + link)
        } else {
            link
        }
        
        return ChapterInfo(
            name = name,
            key = finalLink,
            number = number,
            dateUpload = releaseDate,
            scanlator = translator
        )
    }

    /**
     * a function that get document from [getChapterList] and
     * based on chapterFetcher's selector parameter it would pass each element to [chapterFromElement]
     */
    open fun chaptersParse(document: Document): List<ChapterInfo> {
        // Improved: Use mapNotNull to filter out invalid chapters and handle errors
        val selector = chapterFetcher.selector ?: return emptyList()
        
        return document.select(selector).mapNotNull { element ->
            try {
                val chapter = chapterFromElement(element)
                // Only include valid chapters
                if (chapter.key.isNotBlank() && chapter.name.isNotBlank()) chapter else null
            } catch (e: Exception) {
                // Skip invalid chapters but continue parsing
                null
            }
        }
    }

    /**
     * a request that take a [book](MangaInfo)  and return a document
     */
    open suspend fun getChapterListRequest(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): Document {
        return client.get(requestBuilder(manga.key)).asJsoup()
    }

    /**
     * @param manga the manga that is passed from main app app, which is get from [getMangaList] or [getMangaList]
     * @param commands commands that is passes over from main app
     *                  this list can  have [Command.Detail.Chapters]
     *                  which the source should return List<[ChapterInfo]>
     *                  this is optional, this command is only available if you add
     *                  this command to command list
     * @return return List<[ChapterInfo]>
     */
    override suspend fun getChapterList(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): List<ChapterInfo> {
        // Improved: Check for command first (faster path)
        val fetchCommand = commands.findInstance<Command.Chapter.Fetch>()
        if (fetchCommand != null) {
            val chapters = chaptersParse(Jsoup.parse(fetchCommand.html))
            return if (chapterFetcher.reverseChapterList) chapters.reversed() else chapters
        }
        
        // Improved: Fetch from network with better error handling
        return withContext(Dispatchers.IO) {
            val document = getChapterListRequest(manga, commands)
            val chapters = chaptersParse(document)
            // Note: reversed logic - if reverseChapterList is false, we reverse (to show newest first)
            if (chapterFetcher.reverseChapterList) chapters else chapters.reversed()
        }
    }

    /**
     * the function that parse book Status
     * @return a status
     *          which should be one of
     *
    const val UNKNOWN = 0

    const val ONGOING = 1

    const val COMPLETED = 2

    const val LICENSED = 3

    const val PUBLISHING_FINISHED = 4

    const val CANCELLED = 5

    const val ON_HIATUS = 6
     *
     *
     *
     */
    open fun statusParser(text: String): Long {
        return detailFetcher.onStatus(text)
    }

    /**
     * a function that takes a document that is passed from [getMangaDetailsRequest]
     * it return as [MangaInfo] base on detail fetcher
     */
    open fun detailParse(document: Document): MangaInfo {
        // Improved: Extract and process all fields with trimming
        val rawTitle = selectorReturnerStringType(
            document,
            detailFetcher.nameSelector,
            detailFetcher.nameAtt
        ).trim()
        val title = detailFetcher.onName(rawTitle)
        
        val rawCover = selectorReturnerStringType(
            document,
            detailFetcher.coverSelector,
            detailFetcher.coverAtt
        ).trim()
        val processedCover = detailFetcher.onCover(rawCover)
        val cover = if (detailFetcher.addBaseurlToCoverLink) {
            getAbsoluteUrl(if (processedCover.startsWith("/")) baseUrl + processedCover else processedCover)
        } else {
            processedCover
        }
        
        val rawAuthor = selectorReturnerStringType(
            document,
            detailFetcher.authorBookSelector,
            detailFetcher.authorBookAtt
        ).trim()
        val author = detailFetcher.onAuthor(rawAuthor)
        
        val rawStatus = selectorReturnerStringType(
            document,
            detailFetcher.statusSelector,
            detailFetcher.statusAtt
        ).trim()
        val status = statusParser(rawStatus)

        // Improved: Better description handling with filtering
        val rawDescriptions = selectorReturnerListType(
            document,
            detailFetcher.descriptionSelector,
            detailFetcher.descriptionBookAtt
        )
        val processedDescriptions = detailFetcher.onDescription(rawDescriptions)
        val description = processedDescriptions
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
        
        val rawCategories = selectorReturnerListType(
            document,
            detailFetcher.categorySelector,
            detailFetcher.categoryAtt
        )
        val categories = detailFetcher.onCategory(rawCategories)
            .filter { it.isNotBlank() }
        
        return MangaInfo(
            title = title,
            cover = cover,
            description = description,
            author = author,
            genres = categories,
            status = status,
            key = "",
        )
    }

    /**
     * the request handler for book detail request which return a documents that is passed to [getMangaDetails]
     */
    open suspend fun getMangaDetailsRequest(
        manga: MangaInfo,
        commands: List<Command<*>>
    ): Document {
        return client.get(requestBuilder(manga.key)).asJsoup()
    }

    /**
     * @param manga the book that is passed from main app app, which is get from [getMangaList] or [getMangaList]
     * @param commands commands that is passes over from main app
     *                  this list can  have [Command.Detail.Fetch]
     *                  which the source should return MangaInfo
     *                  this is optional, this command is only available if you add
     *                  this command to command list
     * @return return a [MangaInfo]
     */
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        // Improved: Check for command first (faster path)
        val fetchCommand = commands.findInstance<Command.Detail.Fetch>()
        if (fetchCommand != null) {
            val parsed = detailParse(Jsoup.parse(fetchCommand.html))
            return parsed.copy(key = fetchCommand.url)
        }
        
        // Improved: Fetch from network and preserve original key
        val document = getMangaDetailsRequest(manga, commands)
        val parsed = detailParse(document)
        return parsed.copy(key = manga.key)
    }

    /**
     * the request handler for content request which return a documents that is passed to [getContents]
     */
    open suspend fun getContentRequest(chapter: ChapterInfo, commands: List<Command<*>>): Document {
        return client.get(requestBuilder(chapter.key)).asJsoup()
    }

    /**
     * a wrapper around getPageList that return a List<String>
     */
    open suspend fun getContents(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        return pageContentParse(getContentRequest(chapter, commands))
    }

    /**
     * parse chapter contents based on contentFetcher
     */
    open fun pageContentParse(document: Document): List<Page> {
        // Improved: Extract and process content with better filtering
        val rawContent = selectorReturnerListType(
            document,
            selector = contentFetcher.pageContentSelector,
            contentFetcher.pageContentAtt
        )
        val processedContent = contentFetcher.onContent(rawContent)
            .filter { it.isNotBlank() }
        
        val rawTitle = selectorReturnerStringType(
            document,
            selector = contentFetcher.pageTitleSelector,
            contentFetcher.pageTitleAtt
        ).trim()
        val processedTitle = contentFetcher.onTitle(rawTitle)

        // Improved: Only add title if it's not blank
        val pages = mutableListOf<Page>()
        if (processedTitle.isNotBlank()) {
            pages.add(processedTitle.toPage())
        }
        pages.addAll(processedContent.map { it.toPage() })
        
        return pages
    }

    open fun String.toPage(): Page {
        return Text(this)
    }
    open fun List<String>.toPage(): List<Page> {
        return this.map { it.toPage() }
    }

    /**
     * @param chapter the chapter that is passed from main app app
     * @param commands commands that is passes over from main app
     *                  this list can  have Command.Content.Fetch
     *                  which the source should return the content of chapter
     *                  this is optional, this command is only available if you add
     *                  this command to command list
     * @return a Page which is basically list of strings, need to map Strings to Text()
     */
    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        // Improved: Check for command first (faster path)
        val fetchCommand = commands.findInstance<Command.Content.Fetch>()
        if (fetchCommand != null) {
            return pageContentParse(Jsoup.parse(fetchCommand.html))
        }
        
        return getContents(chapter, commands)
    }

    /**
     * @param key the key that is passed to to request and get list,
     *              and must be unique for each ExploreFetcher,devs can
     *              use this to customize requests
     * @param endpoint the endpoints for each fetcher example : "/popular/{page}/{query}
     *                  replace the pages in url with "{url}
     *                  replace the query in the url with {query}
     *
     * @param selector selector for each book elements
     * @param addBaseUrlToLink add baseUrl to Link
     * @param nextPageSelector the selector for the element that indicated that next page exists
     * @param nextPageAtt the attribute for the element that indicated that next page exists
     * @param nextPageValue the expected value that next page,
    this value can be left empty
     * @param onLink  this value pass a string and after applying the required changed
    it should return the changed link
     * @param addBaseurlToCoverLink "true" if you want to add baseUrl to link
     * @param linkSelector selector for link of book
     * @param linkAtt attribute for the link of book
     * @param onName it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param nameSelector selector for name of book
     * @param nameAtt attribute for name of book
     * @param coverSelector selector for cover of book
     * @param coverAtt attribute for cover of book
     * @param onCover it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param type the type this data class, don't change this parameter
     */
    data class BaseExploreFetcher(
        val key: String,
        val endpoint: String? = null,
        val selector: String? = null,
        val addBaseUrlToLink: Boolean = false,
        val nextPageSelector: String? = null,
        val nextPageAtt: String? = null,
        val nextPageValue: String? = null,
        val addBaseurlToCoverLink: Boolean = false,
        val linkSelector: String? = null,
        val linkAtt: String? = null,
        val onLink: (url: String, key: String) -> String = { url, _ -> url },
        val nameSelector: String? = null,
        val nameAtt: String? = null,
        val onName: (String, key: String) -> String = { url, _ -> url },
        val coverSelector: String? = null,
        val coverAtt: String? = null,
        val onCover: (String, key: String) -> String = { url, _ -> url },
        val onQuery: (query: String) -> String = { query -> query },
        val onPage: (page: String) -> String = { page -> page },
        val infinitePage: Boolean = false,
        val maxPage: Int = -1,
        val type: Type = Type.Others,
    )

    /**
     * all parameter are optional
     * @param addBaseurlToCoverLink "true" if you want to add base url to cover link
     * @param onName it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param nameSelector the selector for the name of book
     * @param nameAtt the attribute for the name of book
     * @param onCover it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param coverSelector the selector for the cover of book
     * @param coverAtt the attribute for the cover of att
     * @param descriptionSelector the selector for the description of book
     * @param descriptionBookAtt the attribute for the description of book
     * @param onDescription it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param authorBookSelector the selector for the author of book
     * @param authorBookAtt the attribute for the author of book
     * @param onAuthor it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param categorySelector the selector for the category of book
     * @param categoryAtt the attribute for the category of book
     * @param onCategory it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param statusSelector the selector for the status of book
     * @param statusAtt the attribute for the status of book
     * @param onStatus it take title that is get based on selector and attribute and it should return a value after applying changes, the value must be [MangaInfo.status]
     * @param status a map that take expected value as key and take the result Status as value @example "OnGoing" to MangaInfo.ONGOING
     * @param type the type this data class, don't change this parameter
     */
    data class Detail(
        val addBaseurlToCoverLink: Boolean = false,
        val nameSelector: String? = null,
        val nameAtt: String? = null,
        val onName: (String) -> String = { it },
        val coverSelector: String? = null,
        val coverAtt: String? = null,
        val onCover: (String) -> String = { it },
        val descriptionSelector: String? = null,
        val descriptionBookAtt: String? = null,
        val onDescription: (List<String>) -> List<String> = { it },
        val authorBookSelector: String? = null,
        val authorBookAtt: String? = null,
        val onAuthor: (String) -> String = { it },
        val categorySelector: String? = null,
        val categoryAtt: String? = null,
        val onCategory: (List<String>) -> List<String> = { it },
        val statusSelector: String? = null,
        val statusAtt: String? = null,
        val onStatus: (String) -> Long = { MangaInfo.UNKNOWN },
        val type: Type = Type.Detail,
    )

    /**
     * all parameter are optional
     * @param selector selector for each chapter elements
     * @param addBaseUrlToLink true, if you want to add baseUrl to the url
     * @param reverseChapterList "true" if you want to reverse chapter list
     * @param linkSelector the selector for the link of chapter
     * @param linkAtt the attribute for the link of chapter
     * @param onLink it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param nameSelector the selector for the name of chapter
     * @param nameAtt the attribute for the name of chapter
     * @param onName it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param numberSelector the selector for the number of chapter
     * @param numberAtt the attribute for the number of chapter
     * @param onNumber it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param uploadDateSelector  the selector for the uploadDate of chapter
     * @param uploadDateAtt the attribute for the uploadDate of chapter
     * @param uploadDateParser take a string which is the string that document get from "uploadDateSelector" and "uploadDateAtt"
     * @param translatorSelector the selector for the translator of chapter
     * @param translatorAtt the attribute for the translator of chapter
     * @param onTranslator it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param type the type this data class, don't change this parameter
     */
    data class Chapters(
        val selector: String? = null,
        val addBaseUrlToLink: Boolean = false,
        val reverseChapterList: Boolean = false,
        val linkSelector: String? = null,
        val onLink: ((String) -> String) = { it },
        val linkAtt: String? = null,
        val nameSelector: String? = null,
        val nameAtt: String? = null,
        val onName: ((String) -> String) = { it },
        val numberSelector: String? = null,
        val numberAtt: String? = null,
        val onNumber: ((String) -> String) = { it },
        val uploadDateSelector: String? = null,
        val uploadDateAtt: String? = null,
        val uploadDateParser: (String) -> Long = { 0L },
        val translatorSelector: String? = null,
        val translatorAtt: String? = null,
        val onTranslator: ((String) -> String) = { it },
        val type: Type = Type.Chapters,
    )

    /**
     * all parameter are optional
     * @param pageTitleSelector selector for title of novel
     * @param pageTitleAtt att for title of novel
     * @param onTitle it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param pageContentSelector selector for content of novel
     * @param pageContentAtt att for content of novel
     * @param onContent it take title that is get based on selector and attribute and it should return a value after applying changes
     * @param type the type this data class, don't change this parameter
     */
    data class Content(
        val pageTitleSelector: String? = null,
        val pageTitleAtt: String? = null,
        val onTitle: (String) -> String = { it },
        val pageContentSelector: String? = null,
        val pageContentAtt: String? = null,
        val onContent: (List<String>) -> List<String> = { it },
        val type: Type = Type.Content,
    )

    /**
     * type of fetchers
     * if there are not under any types like popular, date, new, then
     * it is part of "Other" Typ]e
     *
     */
    enum class Type {
        Search,
        Detail,
        Chapters,
        Content,
        Others
    }

    /**
     * get list of text based on selector and attribute
     */
    open fun selectorReturnerStringType(
        document: Document,
        selector: String? = null,
        att: String? = null,
    ): String {
        // Improved: Use when expression for cleaner logic and better error handling
        return try {
            when {
                selector.isNullOrBlank() && !att.isNullOrBlank() -> document.attr(att)
                !selector.isNullOrBlank() && att.isNullOrBlank() -> document.select(selector).text()
                !selector.isNullOrBlank() && !att.isNullOrBlank() -> document.select(selector).attr(att)
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * get list of text based on selector and attribute
     */
    open fun selectorReturnerStringType(
        element: Element,
        selector: String? = null,
        att: String? = null,
    ): String {
        // Improved: Use when expression for cleaner logic and better error handling
        return try {
            when {
                selector.isNullOrBlank() && !att.isNullOrBlank() -> element.attr(att)
                !selector.isNullOrBlank() && att.isNullOrBlank() -> element.select(selector).text()
                !selector.isNullOrBlank() && !att.isNullOrBlank() -> element.select(selector).attr(att)
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * get list of text based on selector and attribute
     */
    open fun selectorReturnerListType(
        element: Element,
        selector: String? = null,
        att: String? = null,
    ): List<String> {
        // Improved: Use when expression and filter blank entries
        return try {
            when {
                selector.isNullOrBlank() && !att.isNullOrBlank() -> {
                    val value = element.attr(att)
                    if (value.isNotBlank()) listOf(value) else emptyList()
                }
                !selector.isNullOrBlank() && att.isNullOrBlank() -> {
                    element.select(selector).eachText().filter { it.isNotBlank() }
                }
                !selector.isNullOrBlank() && !att.isNullOrBlank() -> {
                    val value = element.select(selector).attr(att)
                    if (value.isNotBlank()) listOf(value) else emptyList()
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * get a list of text based on selector and attribute
     */
    open fun selectorReturnerListType(
        document: Document,
        selector: String? = null,
        att: String? = null,
    ): List<String> {
        // Improved: Use when expression and filter blank entries
        return try {
            when {
                selector.isNullOrBlank() && !att.isNullOrBlank() -> {
                    val value = document.attr(att)
                    if (value.isNotBlank()) listOf(value) else emptyList()
                }
                !selector.isNullOrBlank() && att.isNullOrBlank() -> {
                    document.select(selector).mapNotNull { 
                        val text = it.text()
                        if (text.isNotBlank()) text else null
                    }
                }
                !selector.isNullOrBlank() && !att.isNullOrBlank() -> {
                    val value = document.select(selector).attr(att)
                    if (value.isNotBlank()) listOf(value) else emptyList()
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Safe selector that returns empty string on error
     */
    protected fun safeSelectorString(
        element: Element,
        selector: String?,
        att: String? = null
    ): String {
        return try {
            selectorReturnerStringType(element, selector, att)
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Safe selector that returns empty list on error
     */
    protected fun safeSelectorList(
        element: Element,
        selector: String?,
        att: String? = null
    ): List<String> {
        return try {
            selectorReturnerListType(element, selector, att)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Helper to normalize URLs
     */
    protected fun normalizeUrl(url: String, addBaseUrl: Boolean = false): String {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("//") -> "https:$url"
            addBaseUrl && url.startsWith("/") -> "$baseUrl$url"
            addBaseUrl -> "$baseUrl/$url"
            else -> url
        }
    }
    
    /**
     * Helper to clean text content
     */
    protected fun cleanText(text: String): String {
        return text.replace(Regex("\\s+"), " ").trim()
    }
    
    /**
     * Helper to parse status with common patterns
     */
    protected fun parseStatusFromText(text: String): Long {
        return MangaInfo.parseStatus(text)
    }
    
    /**
     * Helper to extract chapter number from name
     */
    protected fun extractChapterNumber(name: String): Float {
        return ChapterInfo.extractChapterNumber(name)
    }
    
    /**
     * Validate parsed manga info
     */
    protected fun MangaInfo.validate(): MangaInfo {
        require(this.isValid()) { "Invalid MangaInfo: key or title is blank" }
        return this
    }
    
    /**
     * Validate parsed chapter info
     */
    protected fun ChapterInfo.validate(): ChapterInfo {
        require(this.isValid()) { "Invalid ChapterInfo: key or name is blank" }
        return this
    }
}
