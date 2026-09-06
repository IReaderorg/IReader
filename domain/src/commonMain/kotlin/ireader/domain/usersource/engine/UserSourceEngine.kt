package ireader.domain.usersource.engine

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import ireader.core.source.CatalogSource
import ireader.core.source.model.*
import ireader.domain.usersource.model.UserSource
import ireader.domain.usersource.parser.ParsedRequest
import ireader.domain.usersource.parser.RuleParser
import ireader.domain.usersource.parser.UrlParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Engine that executes user-defined source rules.
 * Converts UserSource JSON configuration into a working CatalogSource.
 */
class UserSourceEngine(
    private val userSource: UserSource,
    private val httpClient: HttpClient
) : CatalogSource {
    
    override val id: Long = userSource.generateId()
    override val name: String = userSource.sourceName
    override val lang: String = userSource.lang
    
    private val baseUrl: String = userSource.sourceUrl.trimEnd('/')
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    private sealed class EngineResponse {
        data class Html(val doc: Document) : EngineResponse()
        data class Json(val element: JsonElement) : EngineResponse()
    }
    
    // ==================== Search ====================
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        val query = filters.filterIsInstance<Filter.Title>().firstOrNull()?.value ?: ""
        return search(query, page)
    }
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        return when (sort?.name) {
            "Latest" -> getLatest(page)
            else -> getPopular(page)
        }
    }
    
    private suspend fun search(query: String, page: Int): MangasPageInfo {
        if (userSource.searchUrl.isBlank()) return MangasPageInfo(emptyList(), false)
        
        val parsed = UrlParser.parseRequest(userSource.searchUrl, baseUrl, key = query, page = page)
        val response = fetchResponse(parsed)
        return parseBookList(response, isSearch = true)
    }
    
    private suspend fun getLatest(page: Int): MangasPageInfo {
        if (userSource.exploreUrl.isBlank()) return MangasPageInfo(emptyList(), false)
        
        val parsed = UrlParser.parseRequest(userSource.exploreUrl, baseUrl, page = page)
        val response = fetchResponse(parsed)
        return parseBookList(response, isSearch = false)
    }
    
    private suspend fun getPopular(page: Int): MangasPageInfo = getLatest(page)
    
    private fun parseBookList(response: EngineResponse, isSearch: Boolean): MangasPageInfo {
        val searchRule = userSource.ruleSearch
        val exploreRule = userSource.ruleExplore
        val bookListSelector = if (isSearch) searchRule.bookList else exploreRule.bookList
        
        if (bookListSelector.isBlank()) return MangasPageInfo(emptyList(), false)
        
        return when (response) {
            is EngineResponse.Html -> {
                val elements = RuleParser.getElements(response.doc, bookListSelector)
                val books = elements.mapNotNull { element ->
                    try {
                        val bookName = RuleParser.getString(element, if (isSearch) searchRule.name else exploreRule.name)
                        if (bookName.isBlank()) return@mapNotNull null
                        
                        val bookUrl = RuleParser.getString(element, if (isSearch) searchRule.bookUrl else exploreRule.bookUrl)
                        val author = RuleParser.getString(element, if (isSearch) searchRule.author else exploreRule.author)
                        val coverUrl = RuleParser.getString(element, if (isSearch) searchRule.coverUrl else exploreRule.coverUrl)
                        val intro = RuleParser.getString(element, if (isSearch) searchRule.intro else exploreRule.intro)
                        
                        MangaInfo(
                            key = UrlParser.toAbsoluteUrl(bookUrl, baseUrl),
                            title = bookName,
                            author = author,
                            cover = if (coverUrl.isNotBlank()) UrlParser.toAbsoluteUrl(coverUrl, baseUrl) else "",
                            description = intro
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                MangasPageInfo(books, books.isNotEmpty())
            }
            is EngineResponse.Json -> {
                val jsonElements = RuleParser.getJsonElements(response.element, bookListSelector)
                val books = jsonElements.mapNotNull { item ->
                    try {
                        val bookName = RuleParser.getString(item, if (isSearch) searchRule.name else exploreRule.name)
                        if (bookName.isBlank()) return@mapNotNull null
                        
                        val bookUrl = RuleParser.getString(item, if (isSearch) searchRule.bookUrl else exploreRule.bookUrl)
                        val author = RuleParser.getString(item, if (isSearch) searchRule.author else exploreRule.author)
                        val coverUrl = RuleParser.getString(item, if (isSearch) searchRule.coverUrl else exploreRule.coverUrl)
                        val intro = RuleParser.getString(item, if (isSearch) searchRule.intro else exploreRule.intro)
                        
                        MangaInfo(
                            key = UrlParser.toAbsoluteUrl(bookUrl, baseUrl),
                            title = bookName,
                            author = author,
                            cover = if (coverUrl.isNotBlank()) UrlParser.toAbsoluteUrl(coverUrl, baseUrl) else "",
                            description = intro
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                MangasPageInfo(books, books.isNotEmpty())
            }
        }
    }
    
    // ==================== Book Details ====================
    
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        val rule = userSource.ruleBookInfo
        val response = fetchResponse(manga.key)
        
        val name: String
        val author: String
        val intro: String
        val coverUrl: String
        val kind: String
        
        when (response) {
            is EngineResponse.Html -> {
                name = RuleParser.getString(response.doc, rule.name).ifBlank { manga.title }
                author = RuleParser.getString(response.doc, rule.author).ifBlank { manga.author }
                intro = RuleParser.getString(response.doc, rule.intro).ifBlank { manga.description }
                coverUrl = RuleParser.getString(response.doc, rule.coverUrl).ifBlank { manga.cover }
                kind = RuleParser.getString(response.doc, rule.kind)
            }
            is EngineResponse.Json -> {
                name = RuleParser.getString(response.element, rule.name).ifBlank { manga.title }
                author = RuleParser.getString(response.element, rule.author).ifBlank { manga.author }
                intro = RuleParser.getString(response.element, rule.intro).ifBlank { manga.description }
                coverUrl = RuleParser.getString(response.element, rule.coverUrl).ifBlank { manga.cover }
                kind = RuleParser.getString(response.element, rule.kind)
            }
        }
        
        val genres = if (kind.isNotBlank()) {
            kind.split(",", "，", "/", "|").map { it.trim() }.filter { it.isNotBlank() }
        } else {
            manga.genres
        }
        
        return manga.copy(
            title = name,
            author = author,
            description = intro,
            cover = if (coverUrl.isNotBlank()) UrlParser.toAbsoluteUrl(coverUrl, baseUrl) else "",
            genres = genres
        )
    }
    
    // ==================== Chapters ====================
    
    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        val rule = userSource.ruleToc
        
        // Get TOC URL (might be different from book URL)
        val tocUrl = if (userSource.ruleBookInfo.tocUrl.isNotBlank()) {
            val bookResponse = fetchResponse(manga.key)
            val url = when (bookResponse) {
                is EngineResponse.Html -> RuleParser.getString(bookResponse.doc, userSource.ruleBookInfo.tocUrl)
                is EngineResponse.Json -> RuleParser.getString(bookResponse.element, userSource.ruleBookInfo.tocUrl)
            }
            if (url.isNotBlank()) UrlParser.toAbsoluteUrl(url, baseUrl) else manga.key
        } else {
            manga.key
        }
        
        val chapters = mutableListOf<ChapterInfo>()
        var currentUrl: String? = tocUrl
        var pageCount = 0
        val maxPages = 50
        
        while (currentUrl != null && pageCount < maxPages) {
            val response = fetchResponse(currentUrl)
            val pageChapters = parseChapterList(response, rule)
            chapters.addAll(pageChapters)
            
            currentUrl = if (rule.nextTocUrl.isNotBlank()) {
                val nextUrl = when (response) {
                    is EngineResponse.Html -> RuleParser.getString(response.doc, rule.nextTocUrl)
                    is EngineResponse.Json -> RuleParser.getString(response.element, rule.nextTocUrl)
                }
                if (nextUrl.isNotBlank() && nextUrl != currentUrl) {
                    UrlParser.toAbsoluteUrl(nextUrl, baseUrl)
                } else null
            } else null
            pageCount++
        }
        
        val result = if (rule.isReverse) chapters.reversed() else chapters
        
        return result.mapIndexed { index, chapter ->
            chapter.copy(number = (index + 1).toFloat())
        }
    }
    
    private fun parseChapterList(response: EngineResponse, rule: ireader.domain.usersource.model.TocRule): List<ChapterInfo> {
        if (rule.chapterList.isBlank()) return emptyList()
        
        return when (response) {
            is EngineResponse.Html -> {
                val elements = RuleParser.getElements(response.doc, rule.chapterList)
                elements.mapNotNull { element ->
                    try {
                        val chapterName = RuleParser.getString(element, rule.chapterName)
                        if (chapterName.isBlank()) return@mapNotNull null
                        
                        val chapterUrl = RuleParser.getString(element, rule.chapterUrl)
                        if (chapterUrl.isBlank()) return@mapNotNull null
                        
                        ChapterInfo(
                            key = UrlParser.toAbsoluteUrl(chapterUrl, baseUrl),
                            name = chapterName
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            is EngineResponse.Json -> {
                val jsonElements = RuleParser.getJsonElements(response.element, rule.chapterList)
                jsonElements.mapNotNull { item ->
                    try {
                        val chapterName = RuleParser.getString(item, rule.chapterName)
                        if (chapterName.isBlank()) return@mapNotNull null
                        
                        val chapterUrl = RuleParser.getString(item, rule.chapterUrl)
                        if (chapterUrl.isBlank()) return@mapNotNull null
                        
                        ChapterInfo(
                            key = UrlParser.toAbsoluteUrl(chapterUrl, baseUrl),
                            name = chapterName
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }
    }
    
    // ==================== Content ====================
    
    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        val rule = userSource.ruleContent
        if (rule.content.isBlank()) return listOf(Text("Content rule not configured"))
        
        val contentParts = mutableListOf<String>()
        var currentUrl: String? = chapter.key
        var pageCount = 0
        val maxPages = 100
        
        while (currentUrl != null && pageCount < maxPages) {
            val response = fetchResponse(currentUrl)
            val content = when (response) {
                is EngineResponse.Html -> parseContent(response.doc, rule)
                is EngineResponse.Json -> {
                    var c = RuleParser.getString(response.element, rule.content)
                    if (rule.replaceRegex.isNotBlank()) {
                        try {
                            val parts = rule.replaceRegex.split("##")
                            if (parts.size >= 2) {
                                val pattern = Regex(parts[0])
                                val replacement = parts.getOrElse(1) { "" }
                                c = c.replace(pattern, replacement)
                            }
                        } catch (e: Exception) { }
                    }
                    c
                }
            }
            if (content.isNotBlank()) {
                contentParts.add(content)
            }
            
            currentUrl = if (rule.nextContentUrl.isNotBlank()) {
                val nextUrl = when (response) {
                    is EngineResponse.Html -> RuleParser.getString(response.doc, rule.nextContentUrl)
                    is EngineResponse.Json -> RuleParser.getString(response.element, rule.nextContentUrl)
                }
                if (nextUrl.isNotBlank() && nextUrl != currentUrl) {
                    UrlParser.toAbsoluteUrl(nextUrl, baseUrl)
                } else null
            } else null
            pageCount++
        }
        
        val fullContent = contentParts.joinToString("\n\n")
        
        val paragraphs = fullContent
            .split(Regex("\n{2,}"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        
        return if (paragraphs.isEmpty()) {
            listOf(Text(fullContent))
        } else {
            paragraphs.map { Text(it) }
        }
    }
    
    private fun parseContent(doc: Document, rule: ireader.domain.usersource.model.ContentRule): String {
        // Remove unwanted elements
        if (rule.purify.isNotBlank()) {
            rule.purify.split(",", "&&").forEach { selector ->
                try {
                    doc.select(selector.trim()).remove()
                } catch (e: Exception) { }
            }
        }
        
        var content = RuleParser.getString(doc, rule.content)
        
        // Apply regex replacement
        if (rule.replaceRegex.isNotBlank()) {
            try {
                val parts = rule.replaceRegex.split("##")
                if (parts.size >= 2) {
                    val pattern = Regex(parts[0])
                    val replacement = parts.getOrElse(1) { "" }
                    content = content.replace(pattern, replacement)
                }
            } catch (e: Exception) { }
        }
        
        return content
    }
    
    // ==================== HTTP ====================
    
    private suspend fun fetchResponse(url: String): EngineResponse {
        val parsed = UrlParser.parseRequest(url, baseUrl)
        return fetchResponse(parsed)
    }
    
    private suspend fun fetchResponse(parsed: ParsedRequest): EngineResponse {
        val response = httpClient.request(parsed.url) {
            method = io.ktor.http.HttpMethod.parse(parsed.method)
            if (userSource.header.isNotBlank()) {
                try {
                    val headerMap = json.decodeFromString<Map<String, String>>(userSource.header)
                    headers {
                        headerMap.forEach { (key, value) -> append(key, value) }
                    }
                } catch (e: Exception) { }
            }
            if (parsed.headers.isNotEmpty()) {
                headers {
                    parsed.headers.forEach { (key, value) -> append(key, value) }
                }
            }
            if (parsed.body != null) {
                setBody(parsed.body)
                val hasContentType = parsed.headers.keys.any { it.equals("Content-Type", ignoreCase = true) }
                if (!hasContentType) {
                    val trimmedBody = parsed.body.trim()
                    if (trimmedBody.startsWith("{") || trimmedBody.startsWith("[")) {
                        contentType(io.ktor.http.ContentType.Application.Json)
                    } else {
                        contentType(io.ktor.http.ContentType.Application.FormUrlEncoded)
                    }
                }
            }
        }
        val text = response.bodyAsText().trim()
        val isJson = (text.startsWith("{") && text.endsWith("}")) || (text.startsWith("[") && text.endsWith("]"))
        return if (isJson) {
            try {
                EngineResponse.Json(json.parseToJsonElement(text))
            } catch (e: Exception) {
                EngineResponse.Html(Ksoup.parse(text, parsed.url))
            }
        } else {
            EngineResponse.Html(Ksoup.parse(text, parsed.url))
        }
    }
    
    // ==================== Listings & Filters ====================
    
    override fun getListings(): List<Listing> = listOf(
        PopularListing(),
        LatestListing()
    )
    
    override fun getFilters(): FilterList = listOf(Filter.Title())
    
    override fun getCommands(): CommandList = emptyList()
    
    // Concrete Listing implementations
    private class PopularListing : Listing("Popular")
    private class LatestListing : Listing("Latest")
}
