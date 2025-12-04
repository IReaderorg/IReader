# Source API Redesign - Making Source Creation Easy

## Executive Summary

This document proposes a redesign of the source API to make it significantly easier for developers (even beginners) to create novel/manga sources. The goal is to reduce boilerplate, provide sensible defaults, and offer a declarative approach where possible.

---

## Current Problems

### 1. Too Many Abstract Methods
Developers must implement many methods even for simple sources:
- `getMangaList(sort, page)` 
- `getMangaList(filters, page)`
- `getMangaDetails(manga, commands)`
- `getChapterList(manga, commands)`
- `getPageList(chapter, commands)`
- `getFilters()`
- `getListings()`
- `getCommands()`
- Plus parsing methods: `detailParse`, `chapterFromElement`, `chaptersSelector`, `pageContentParse`

### 2. Confusing Terminology
- "Manga" is used for novels (confusing for novel source developers)
- "Page" can mean chapter content or image pages
- Commands are rarely used but always required

### 3. No Built-in Patterns
Common patterns must be reimplemented:
- Pagination handling
- Search with query parameter
- Latest/Popular listings
- Chapter number extraction
- Date parsing

### 4. Complex Dependencies
- Must understand `Dependencies` injection
- Must understand Ktor HTTP client
- Must understand Ksoup parsing

---

## Proposed Solution: Layered API

```
┌─────────────────────────────────────────────────────────────────┐
│                    Level 3: Declarative DSL                      │
│  NovelSource.create { ... }  - Zero boilerplate, config only    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Level 2: SimpleNovelSource                      │
│  Extend class, override only what you need                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Level 1: ParsedHttpSource                       │
│  Full control, current API (for advanced users)                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Level 3: Declarative DSL (Easiest)

For sources with standard HTML structure, developers can create sources with just configuration:

```kotlin
val mySource = NovelSource.create("MyNovelSite") {
    baseUrl = "https://mynovelsite.com"
    language = "en"
    
    // Search configuration
    search {
        url = "/search?q={query}&page={page}"
        // Or use POST
        // method = POST
        // body = "query={query}"
        
        selector {
            list = "div.novel-list > div.novel-item"
            title = "h3.title"
            url = "a@href"
            cover = "img@src"
            // Optional
            author = "span.author"
            description = "p.desc"
        }
        
        nextPage = "a.next-page"
    }
    
    // Novel details page
    details {
        selector {
            title = "h1.novel-title"
            cover = "div.cover img@src"
            author = "span.author"
            description = "div.description"
            genres = "div.genres a"
            status = "span.status"
        }
        
        // Status mapping
        statusMapping {
            "Ongoing" to ONGOING
            "Completed" to COMPLETED
            "Hiatus" to ON_HIATUS
        }
    }
    
    // Chapter list
    chapters {
        selector {
            list = "ul.chapter-list li"
            title = "a"
            url = "a@href"
            date = "span.date"
        }
        
        // Date format
        dateFormat = "MMM dd, yyyy"
        
        // Or relative dates
        relativeDates {
            "yesterday" to 1
            "ago" to parseRelative
        }
        
        // Reverse order (newest first)
        reverseOrder = true
    }
    
    // Chapter content
    content {
        selector = "div.chapter-content"
        
        // Clean up
        removeSelectors = listOf(
            "div.ads",
            "script",
            ".social-share"
        )
        
        // Split by
        splitBy = "p" // or "br" or custom regex
    }
    
    // Listings (Popular, Latest, etc.)
    listings {
        listing("Popular") {
            url = "/popular?page={page}"
            // Uses same selectors as search by default
        }
        
        listing("Latest") {
            url = "/latest?page={page}"
        }
    }
}
```


---

## Level 2: SimpleNovelSource (Easy)

For sources that need some custom logic but want sensible defaults:

```kotlin
class MyNovelSource : SimpleNovelSource() {
    
    override val name = "My Novel Site"
    override val baseUrl = "https://mynovelsite.com"
    override val language = "en"
    
    // REQUIRED: How to search for novels
    override suspend fun searchNovels(query: String, page: Int): NovelListResult {
        val doc = fetchDocument("$baseUrl/search?q=$query&page=$page")
        
        val novels = doc.select("div.novel-item").map { element ->
            Novel(
                title = element.selectText("h3.title"),
                url = element.selectUrl("a"),
                cover = element.selectImage("img")
            )
        }
        
        val hasNext = doc.exists("a.next-page")
        
        return NovelListResult(novels, hasNext)
    }
    
    // REQUIRED: Get novel details
    override suspend fun getNovelDetails(novel: Novel): Novel {
        val doc = fetchDocument(novel.url)
        
        return novel.copy(
            title = doc.selectText("h1.title"),
            author = doc.selectText("span.author"),
            description = doc.selectText("div.description"),
            genres = doc.selectTexts("div.genres a"),
            status = parseStatus(doc.selectText("span.status")),
            cover = doc.selectImage("div.cover img")
        )
    }
    
    // REQUIRED: Get chapter list
    override suspend fun getChapters(novel: Novel): List<Chapter> {
        val doc = fetchDocument(novel.url)
        
        return doc.select("ul.chapter-list li").mapIndexed { index, element ->
            Chapter(
                title = element.selectText("a"),
                url = element.selectUrl("a"),
                number = index + 1f,
                date = element.selectDate("span.date", "MMM dd, yyyy")
            )
        }
    }
    
    // REQUIRED: Get chapter content
    override suspend fun getChapterContent(chapter: Chapter): List<String> {
        val doc = fetchDocument(chapter.url)
        
        return doc.select("div.chapter-content")
            .cleanAds()
            .extractParagraphs()
    }
    
    // OPTIONAL: Popular novels listing
    override suspend fun getPopularNovels(page: Int): NovelListResult {
        val doc = fetchDocument("$baseUrl/popular?page=$page")
        return parseNovelList(doc, "div.novel-item", "a.next-page")
    }
    
    // OPTIONAL: Latest novels listing
    override suspend fun getLatestNovels(page: Int): NovelListResult {
        val doc = fetchDocument("$baseUrl/latest?page=$page")
        return parseNovelList(doc, "div.novel-item", "a.next-page")
    }
}
```

### Helper Methods Provided by SimpleNovelSource

```kotlin
abstract class SimpleNovelSource {
    
    // Fetch and parse HTML
    suspend fun fetchDocument(url: String): Document
    suspend fun fetchText(url: String): String
    suspend fun fetchJson<T>(url: String): T
    
    // Common parsing helpers
    fun Document.selectText(selector: String): String
    fun Document.selectTexts(selector: String): List<String>
    fun Document.selectUrl(selector: String): String
    fun Document.selectImage(selector: String): String
    fun Document.selectDate(selector: String, format: String): Long
    fun Document.exists(selector: String): Boolean
    
    // Element helpers
    fun Element.selectText(selector: String): String
    fun Element.selectUrl(selector: String): String
    fun Element.selectImage(selector: String): String
    
    // Content cleaning
    fun Element.cleanAds(): Element
    fun Element.extractParagraphs(): List<String>
    fun Element.extractText(): String
    
    // Status parsing
    fun parseStatus(text: String): NovelStatus
    
    // Date parsing
    fun parseDate(text: String, format: String): Long
    fun parseRelativeDate(text: String): Long
    
    // URL helpers
    fun absoluteUrl(path: String): String
    
    // Common list parsing
    fun parseNovelList(
        doc: Document,
        itemSelector: String,
        nextPageSelector: String?
    ): NovelListResult
}
```


---

## Simplified Data Models

### Novel (replaces MangaInfo)

```kotlin
data class Novel(
    val url: String,           // Unique identifier (URL)
    val title: String,
    val author: String = "",
    val artist: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: NovelStatus = NovelStatus.UNKNOWN,
    val cover: String = "",
    val alternativeTitles: List<String> = emptyList(),
    val rating: Float? = null,
    val views: Long? = null
) {
    // Easy creation
    companion object {
        fun create(url: String, title: String, block: NovelBuilder.() -> Unit = {}): Novel
    }
}

enum class NovelStatus {
    UNKNOWN,
    ONGOING,
    COMPLETED,
    LICENSED,
    CANCELLED,
    ON_HIATUS
}
```

### Chapter (replaces ChapterInfo)

```kotlin
data class Chapter(
    val url: String,           // Unique identifier (URL)
    val title: String,
    val number: Float = -1f,   // Auto-extracted if not provided
    val date: Long = 0L,
    val scanlator: String = ""
) {
    // Auto-extract chapter number from title
    fun withAutoNumber(): Chapter
}
```

### NovelListResult (replaces MangasPageInfo)

```kotlin
data class NovelListResult(
    val novels: List<Novel>,
    val hasNextPage: Boolean
) {
    companion object {
        fun empty() = NovelListResult(emptyList(), false)
        fun single(novel: Novel) = NovelListResult(listOf(novel), false)
    }
}
```

### ChapterContent (replaces List<Page>)

```kotlin
sealed class ChapterContent {
    // For text-based content (novels)
    data class Text(val paragraphs: List<String>) : ChapterContent()
    
    // For image-based content (manga)
    data class Images(val urls: List<String>) : ChapterContent()
    
    // For mixed content
    data class Mixed(val items: List<ContentItem>) : ChapterContent()
}

sealed class ContentItem {
    data class TextItem(val text: String) : ContentItem()
    data class ImageItem(val url: String) : ContentItem()
}
```

---

## Selector DSL

A mini-DSL for CSS selectors with attribute extraction:

```kotlin
// Simple text extraction
"h1.title"                    // Get text content
"h1.title@text"               // Same as above (explicit)

// Attribute extraction
"a@href"                      // Get href attribute
"img@src"                     // Get src attribute
"div@data-id"                 // Get data-id attribute

// Multiple selectors (first match wins)
"h1.title | h2.title | .name" // Try each selector

// With filters
"a@href[contains=/novel/]"    // Only if href contains /novel/
"img@src[starts=http]"        // Only if src starts with http

// Nested selection
"div.item > a@href"           // Select a inside div.item
```

### Implementation

```kotlin
fun Element.select(selector: String): String {
    val parts = selector.split("@")
    val cssSelector = parts[0].trim()
    val attribute = parts.getOrNull(1)?.trim() ?: "text"
    
    val element = this.selectFirst(cssSelector) ?: return ""
    
    return when (attribute) {
        "text" -> element.text()
        "html" -> element.html()
        "outerHtml" -> element.outerHtml()
        else -> element.attr(attribute)
    }
}
```


---

## Common Patterns as Built-in Features

### 1. Pagination

```kotlin
// Automatic pagination handling
class MySource : SimpleNovelSource() {
    
    // Define once, use everywhere
    override val pagination = Pagination.QueryParam("page") // ?page=1, ?page=2
    // Or: Pagination.PathSegment // /page/1, /page/2
    // Or: Pagination.Offset(20)  // ?offset=0, ?offset=20
    // Or: Pagination.Custom { page -> "?p=${page - 1}" }
}
```

### 2. Search Filters

```kotlin
// Easy filter definition
override fun getFilters() = filters {
    text("Title")
    text("Author")
    
    select("Genre") {
        option("All", "")
        option("Action", "action")
        option("Romance", "romance")
        option("Fantasy", "fantasy")
    }
    
    select("Status") {
        option("All", "")
        option("Ongoing", "ongoing")
        option("Completed", "completed")
    }
    
    sort("Sort By") {
        option("Latest", "latest")
        option("Popular", "popular")
        option("Rating", "rating")
    }
    
    checkbox("Completed Only", "completed=1")
}

// Filters automatically converted to URL params or form data
override suspend fun searchNovels(query: String, page: Int, filters: FilterValues): NovelListResult {
    val url = buildUrl("$baseUrl/search") {
        param("q", query)
        param("page", page)
        // Filters automatically added
        applyFilters(filters)
    }
    // ...
}
```

### 3. Date Parsing

```kotlin
// Built-in date parsing with common formats
val date1 = parseDate("Jan 15, 2024", "MMM dd, yyyy")
val date2 = parseDate("2024-01-15", "yyyy-MM-dd")
val date3 = parseDate("15/01/2024", "dd/MM/yyyy")

// Relative date parsing
val date4 = parseRelativeDate("2 hours ago")
val date5 = parseRelativeDate("yesterday")
val date6 = parseRelativeDate("3 days ago")

// Auto-detect format
val date7 = parseAnyDate("Jan 15, 2024")
```

### 4. Chapter Number Extraction

```kotlin
// Automatic chapter number extraction
val chapter = Chapter(
    title = "Chapter 123: The Beginning",
    url = "/chapter/123"
).withAutoNumber() // number = 123f

// Supports various formats:
// "Chapter 123" -> 123
// "Ch. 45.5" -> 45.5
// "Episode 10" -> 10
// "第123章" -> 123 (Chinese)
// "123화" -> 123 (Korean)
```

### 5. Content Cleaning

```kotlin
// Built-in content cleaning
val content = doc.select("div.content")
    .removeAds()           // Remove common ad selectors
    .removeScripts()       // Remove script tags
    .removeStyles()        // Remove style tags
    .removeComments()      // Remove HTML comments
    .removeEmpty()         // Remove empty elements
    .extractParagraphs()   // Get clean paragraphs
```

---

## Error Handling

### Automatic Retry with Backoff

```kotlin
// Built into SimpleNovelSource
override suspend fun fetchDocument(url: String): Document {
    return withRetry(maxAttempts = 3) {
        // Automatic retry on network errors
        // Automatic Cloudflare bypass
        // Automatic rate limiting
    }
}
```

### User-Friendly Errors

```kotlin
sealed class SourceError {
    data class NetworkError(val message: String) : SourceError()
    data class ParsingError(val message: String, val url: String) : SourceError()
    data class CloudflareError(val message: String) : SourceError()
    data class NotFoundError(val message: String) : SourceError()
    data class RateLimitError(val retryAfter: Long?) : SourceError()
}

// In source implementation
override suspend fun getChapterContent(chapter: Chapter): Result<List<String>> {
    return try {
        val doc = fetchDocument(chapter.url)
        val content = doc.select("div.content").extractParagraphs()
        
        if (content.isEmpty()) {
            Result.failure(SourceError.ParsingError("No content found", chapter.url))
        } else {
            Result.success(content)
        }
    } catch (e: Exception) {
        Result.failure(SourceError.NetworkError(e.message ?: "Unknown error"))
    }
}
```


---

## Complete Example: Creating a Source

### Example 1: Minimal Source (DSL)

```kotlin
// Just 30 lines for a complete source!
val royalRoad = NovelSource.create("Royal Road") {
    baseUrl = "https://www.royalroad.com"
    language = "en"
    
    search {
        url = "/fictions/search?title={query}&page={page}"
        selector {
            list = "div.fiction-list-item"
            title = "h2.fiction-title a"
            url = "h2.fiction-title a@href"
            cover = "img@src"
            author = "span.author"
        }
        nextPage = "a[rel=next]"
    }
    
    details {
        selector {
            title = "h1.fic-title"
            cover = "div.fic-header img@src"
            author = "span.author a"
            description = "div.description"
            genres = "span.tags a"
        }
    }
    
    chapters {
        url = "/fiction/{id}/chapters"  // {id} extracted from novel URL
        selector {
            list = "table#chapters tbody tr"
            title = "td:first-child a"
            url = "td:first-child a@href"
            date = "td:last-child time@datetime"
        }
    }
    
    content {
        selector = "div.chapter-content"
        removeSelectors = listOf(".author-note", ".ads")
    }
    
    listings {
        listing("Best Rated") { url = "/fictions/best-rated?page={page}" }
        listing("Trending") { url = "/fictions/trending?page={page}" }
        listing("Latest") { url = "/fictions/latest-updates?page={page}" }
    }
}
```

### Example 2: Simple Source (Class)

```kotlin
class NovelUpdates : SimpleNovelSource() {
    
    override val name = "Novel Updates"
    override val baseUrl = "https://www.novelupdates.com"
    override val language = "en"
    
    override suspend fun searchNovels(query: String, page: Int): NovelListResult {
        val doc = fetchDocument("$baseUrl/page/$page/?s=$query")
        
        return parseNovelList(doc, "div.search_main_box_nu") { element ->
            Novel(
                url = element.selectUrl("h2 a"),
                title = element.selectText("h2 a"),
                cover = element.selectImage("img"),
                description = element.selectText("div.search_body_nu")
            )
        }
    }
    
    override suspend fun getNovelDetails(novel: Novel): Novel {
        val doc = fetchDocument(novel.url)
        
        return novel.copy(
            title = doc.selectText("h4.seriestitlenu"),
            cover = doc.selectImage("div.seriesimg img"),
            author = doc.selectTexts("div#showauthors a").joinToString(", "),
            description = doc.selectText("div#editdescription"),
            genres = doc.selectTexts("div#seriesgenre a"),
            status = parseStatus(doc.selectText("div#editstatus"))
        )
    }
    
    override suspend fun getChapters(novel: Novel): List<Chapter> {
        // Novel Updates has chapters on a separate page
        val novelId = novel.url.substringAfterLast("/").substringBefore("/")
        val doc = fetchDocument("$baseUrl/series/$novelId/?pg=1")
        
        return doc.select("table#myTable tbody tr").mapIndexed { index, row ->
            Chapter(
                url = row.selectUrl("a"),
                title = row.selectText("a"),
                number = (index + 1).toFloat(),
                date = row.selectDate("td:last-child", "MM/dd/yy")
            )
        }.reversed() // Oldest first
    }
    
    override suspend fun getChapterContent(chapter: Chapter): List<String> {
        // Novel Updates links to external sites
        // This would need to handle redirects
        val doc = fetchDocument(chapter.url)
        return doc.select("div.chapter-content, div#content, article")
            .first()
            ?.cleanAds()
            ?.extractParagraphs()
            ?: emptyList()
    }
    
    override suspend fun getPopularNovels(page: Int): NovelListResult {
        val doc = fetchDocument("$baseUrl/series-ranking/?rank=popular&pg=$page")
        return parseNovelList(doc, "div.search_main_box_nu") { element ->
            Novel(
                url = element.selectUrl("h2 a"),
                title = element.selectText("h2 a"),
                cover = element.selectImage("img")
            )
        }
    }
}
```


---

## Migration Path

### Backward Compatibility

The new API will be **additive** - existing sources using `ParsedHttpSource` will continue to work. The new classes are alternatives, not replacements.

```
Existing:
  Source → CatalogSource → HttpSource → ParsedHttpSource → YourSource

New (Option 1 - Simple):
  Source → CatalogSource → HttpSource → SimpleNovelSource → YourSource

New (Option 2 - DSL):
  NovelSource.create { ... } → generates → SimpleNovelSource implementation
```

### Adapter for Old Sources

```kotlin
// Wrap old source in new API
fun ParsedHttpSource.toSimpleSource(): SimpleNovelSource {
    return object : SimpleNovelSource() {
        override val name = this@toSimpleSource.name
        override val baseUrl = this@toSimpleSource.baseUrl
        override val language = this@toSimpleSource.lang
        
        override suspend fun searchNovels(query: String, page: Int): NovelListResult {
            val filters = listOf(Filter.Title().apply { value = query })
            val result = this@toSimpleSource.getMangaList(filters, page)
            return NovelListResult(
                novels = result.mangas.map { it.toNovel() },
                hasNextPage = result.hasNextPage
            )
        }
        // ... other methods
    }
}
```

---

## Implementation Plan

### Phase 1: Core Classes (Week 1)
1. Create `Novel`, `Chapter`, `NovelListResult` data classes
2. Create `SimpleNovelSource` abstract class
3. Create selector DSL helpers
4. Create date parsing utilities

### Phase 2: Helper Methods (Week 2)
1. Implement `fetchDocument`, `fetchText`, `fetchJson`
2. Implement content cleaning utilities
3. Implement chapter number extraction
4. Implement status parsing

### Phase 3: DSL Builder (Week 3)
1. Create `NovelSourceBuilder` DSL
2. Create `SearchConfig`, `DetailsConfig`, `ChaptersConfig`, `ContentConfig`
3. Create `NovelSource.create()` factory

### Phase 4: Filters & Listings (Week 4)
1. Create simplified filter DSL
2. Create listing configuration
3. Implement filter-to-URL conversion

### Phase 5: Testing & Documentation (Week 5)
1. Create example sources
2. Write comprehensive documentation
3. Create source creation guide
4. Add unit tests

---

## File Structure

```
source-api/src/commonMain/kotlin/ireader/core/source/
├── simple/
│   ├── SimpleNovelSource.kt      # Main simplified base class
│   ├── Novel.kt                  # Novel data class
│   ├── Chapter.kt                # Chapter data class  
│   ├── NovelListResult.kt        # Pagination result
│   ├── ChapterContent.kt         # Content types
│   ├── NovelStatus.kt            # Status enum
│   └── SourceError.kt            # Error types
├── dsl/
│   ├── NovelSourceBuilder.kt     # DSL builder
│   ├── SearchConfig.kt           # Search configuration
│   ├── DetailsConfig.kt          # Details configuration
│   ├── ChaptersConfig.kt         # Chapters configuration
│   ├── ContentConfig.kt          # Content configuration
│   └── ListingConfig.kt          # Listing configuration
├── helpers/
│   ├── SelectorHelper.kt         # CSS selector DSL
│   ├── DateParser.kt             # Date parsing utilities
│   ├── ChapterNumberExtractor.kt # Chapter number extraction
│   ├── ContentCleaner.kt         # Content cleaning
│   ├── StatusParser.kt           # Status parsing
│   └── UrlBuilder.kt             # URL building utilities
├── filters/
│   ├── SimpleFilter.kt           # Simplified filters
│   └── FilterBuilder.kt          # Filter DSL
└── (existing files...)
```

---

## Success Metrics

1. **Lines of Code**: A basic source should require < 50 lines
2. **Time to Create**: A developer should be able to create a source in < 30 minutes
3. **Learning Curve**: A beginner should understand the API in < 1 hour
4. **Error Messages**: All errors should be actionable and clear

---

## Next Steps

1. Review and approve this design
2. Start implementation with Phase 1
3. Create a sample source using the new API
4. Gather feedback and iterate


---

## Implementation Status

### Completed

✅ **Core Data Classes** (`source-api/src/commonMain/kotlin/ireader/core/source/simple/`)
- `Novel.kt` - Simplified novel data class with builder pattern
- `NovelStatus.kt` - Status enum with parsing utilities
- `Chapter.kt` - Simplified chapter with auto-number extraction
- `NovelListResult.kt` - Pagination result class
- `ChapterContent.kt` - Content types (text, images, mixed)

✅ **Helper Classes** (`source-api/src/commonMain/kotlin/ireader/core/source/helpers/`)
- `SelectorHelper.kt` - CSS selector DSL with attribute extraction
- `DateParser.kt` - Date parsing utilities (absolute and relative)
- `ContentCleaner.kt` - HTML content cleaning utilities

✅ **Base Class**
- `SimpleNovelSource.kt` - Main simplified source base class

✅ **Examples**
- `ExampleSource.kt` - Two example implementations showing the API

### Completed (DSL)

✅ **DSL Builder** (`source-api/src/commonMain/kotlin/ireader/core/source/dsl/`)
- `NovelSourceBuilder.kt` - DSL for zero-code source creation
- `NovelSource.create()` factory function
- Configuration classes: `SearchConfig`, `DetailsConfig`, `ChaptersConfig`, `ContentConfig`
- `DslNovelSource` - Auto-generated source implementation

✅ **Filter DSL**
- `FilterBuilder.kt` - Simplified filter builder
- `filters { }` DSL for easy filter creation
- `UrlBuilder` for filter-to-URL conversion
- Extension functions for filter value extraction

✅ **Examples**
- `DslExamples.kt` - Complete, minimal, and filtered source examples

### Pending

⏳ **Documentation**
- Source creation guide (separate document)
- Migration guide from ParsedHttpSource
- Video tutorial

---

## Quick Start Guide

### Creating a Source in 5 Minutes

1. **Create a new class extending SimpleNovelSource:**

```kotlin
class MySource(deps: Dependencies) : SimpleNovelSource(deps) {
    override val name = "My Novel Site"
    override val baseUrl = "https://mysite.com"
    override val language = "en"
```

2. **Implement searchNovels:**

```kotlin
    override suspend fun searchNovels(query: String, page: Int): NovelListResult {
        val doc = fetchDocument("$baseUrl/search?q=$query&page=$page")
        val novels = doc.select("div.novel").map { el ->
            Novel(
                url = el.selectUrl("a"),
                title = el.selectText("h3")
            )
        }
        return NovelListResult(novels, doc.exists("a.next"))
    }
```

3. **Implement getNovelDetails:**

```kotlin
    override suspend fun getNovelDetails(novel: Novel): Novel {
        val doc = fetchDocument(novel.url)
        return novel.copy(
            title = doc.selectText("h1"),
            description = doc.selectText("div.desc"),
            cover = doc.selectImage("img.cover"),
            author = doc.selectText("span.author"),
            status = parseStatus(doc.selectText("span.status"))
        )
    }
```

4. **Implement getChapters:**

```kotlin
    override suspend fun getChapters(novel: Novel): List<Chapter> {
        val doc = fetchDocument(novel.url)
        return doc.select("ul.chapters li").mapIndexed { i, el ->
            Chapter(
                url = el.selectUrl("a"),
                title = el.selectText("a"),
                number = (i + 1).toFloat()
            )
        }
    }
```

5. **Implement getChapterContent:**

```kotlin
    override suspend fun getChapterContent(chapter: Chapter): List<String> {
        val doc = fetchDocument(chapter.url)
        return doc.selectFirst("div.content")
            ?.removeAds()
            ?.extractParagraphs()
            ?: emptyList()
    }
}
```

**That's it!** A complete source in ~40 lines of code.

### Selector Syntax Quick Reference

| Selector | Result |
|----------|--------|
| `"h1.title"` | Text content of h1.title |
| `"a@href"` | href attribute of a |
| `"img@src"` | src attribute of img |
| `"div@data-id"` | data-id attribute |
| `"a@href \| img@src"` | First match (fallback) |

### Helper Methods

| Method | Description |
|--------|-------------|
| `fetchDocument(url)` | Fetch and parse HTML |
| `selectText(selector)` | Get text from element |
| `selectUrl(selector)` | Get URL (handles relative) |
| `selectImage(selector)` | Get image URL |
| `selectTexts(selector)` | Get multiple texts |
| `exists(selector)` | Check if element exists |
| `removeAds()` | Remove common ad elements |
| `extractParagraphs()` | Get clean paragraphs |
| `parseStatus(text)` | Parse status string |
| `parseDate(text)` | Parse date string |


---

## DSL Quick Reference

### Creating a Source with DSL

```kotlin
val mySource = NovelSource.create("Site Name", deps) {
    baseUrl = "https://example.com"
    language = "en"
    hasCloudflare = true  // Optional
    
    search {
        url = "/search?q={query}&page={page}"
        selector {
            list = "div.novel-item"
            title = "h3.title"
            url = "a@href"
            cover = "img@src"
        }
        nextPage = "a.next"
    }
    
    details {
        selector {
            title = "h1"
            description = "div.desc"
            author = "span.author"
            genres = "div.tags a"
            status = "span.status"
        }
        statusMapping {
            "ongoing" to NovelStatus.ONGOING
            "completed" to NovelStatus.COMPLETED
        }
    }
    
    chapters {
        selector {
            list = "ul.chapters li"
            title = "a"
            url = "a@href"
            date = "span.date"
        }
        dateFormat = "MMM dd, yyyy"
        reverseOrder = true
    }
    
    content {
        selector = "div.chapter-content"
        removeSelectors = listOf(".ads", ".author-note")
    }
    
    listings {
        listing("Popular") { url = "/popular?page={page}" }
        listing("Latest") { url = "/latest?page={page}" }
    }
}
```

### Creating Filters with DSL

```kotlin
val myFilters = filters {
    title()
    author()
    
    select("Genre") {
        option("All")
        option("Action")
        option("Romance")
    }
    
    select("Status") {
        option("All")
        option("Ongoing")
        option("Completed")
    }
    
    sort("Sort By") {
        option("Latest")
        option("Popular")
        default(0)
    }
    
    checkbox("Completed Only")
    
    genres("Genres", "Action", "Adventure", "Comedy", "Drama")
}
```

### Building URLs with Filters

```kotlin
val url = buildUrl("$baseUrl/search") {
    param("q", query)
    param("page", page)
    applyFilters(filters, mapOf(
        "Genre" to "genre",
        "Status" to "status"
    ))
}
```

---

## Comparison: Old vs New API

### Old API (ParsedHttpSource) - ~150 lines

```kotlin
class MySource(deps: Dependencies) : ParsedHttpSource(deps) {
    override val name = "My Site"
    override val baseUrl = "https://mysite.com"
    override val lang = "en"
    
    override suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo {
        // Complex implementation...
    }
    
    override suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo {
        // Complex implementation...
    }
    
    override suspend fun getMangaDetails(manga: MangaInfo, commands: List<Command<*>>): MangaInfo {
        // Complex implementation...
    }
    
    override suspend fun getChapterList(manga: MangaInfo, commands: List<Command<*>>): List<ChapterInfo> {
        // Complex implementation...
    }
    
    override suspend fun getPageList(chapter: ChapterInfo, commands: List<Command<*>>): List<Page> {
        // Complex implementation...
    }
    
    override fun getFilters(): FilterList { ... }
    override fun getListings(): List<Listing> { ... }
    override fun getCommands(): CommandList { ... }
    
    override fun chaptersSelector(): String = "..."
    override fun chapterFromElement(element: Element): ChapterInfo { ... }
    override fun pageContentParse(document: Document): List<String> { ... }
    override fun detailParse(document: Document): MangaInfo { ... }
    
    // Plus many more helper methods...
}
```

### New API (SimpleNovelSource) - ~40 lines

```kotlin
class MySource(deps: Dependencies) : SimpleNovelSource(deps) {
    override val name = "My Site"
    override val baseUrl = "https://mysite.com"
    override val language = "en"
    
    override suspend fun searchNovels(query: String, page: Int) = 
        parseNovelList(fetchDocument("$baseUrl/search?q=$query&page=$page"), "div.novel")
    
    override suspend fun getNovelDetails(novel: Novel) = 
        fetchDocument(novel.url).let { doc ->
            novel.copy(
                title = doc.selectText("h1"),
                description = doc.selectText("div.desc")
            )
        }
    
    override suspend fun getChapters(novel: Novel) =
        fetchDocument(novel.url).select("ul li").map { el ->
            Chapter(url = el.selectUrl("a"), title = el.selectText("a"))
        }
    
    override suspend fun getChapterContent(chapter: Chapter) =
        fetchDocument(chapter.url).selectFirst("div.content")?.extractParagraphs() ?: emptyList()
}
```

### New API (DSL) - ~30 lines

```kotlin
val mySource = NovelSource.create("My Site", deps) {
    baseUrl = "https://mysite.com"
    language = "en"
    
    search {
        url = "/search?q={query}&page={page}"
        selector { list = "div.novel"; title = "h3"; url = "a@href" }
    }
    
    details {
        selector { title = "h1"; description = "div.desc" }
    }
    
    chapters {
        selector { list = "ul li"; title = "a"; url = "a@href" }
    }
    
    content {
        selector = "div.content"
    }
}
```

**Result: 80% reduction in code!**
