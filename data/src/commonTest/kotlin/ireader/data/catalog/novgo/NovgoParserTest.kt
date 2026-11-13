package ireader.data.catalog.novgo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NovgoParserTest {

    @Test
    fun popularListingReadsNovelCards() {
        val html = """
            <div class="li-row"><div class="li"><div class="con">
              <div class="pic"><a href="/hidden-marriage.html"><img src="/uploads/thumbs/hidden.jpg" alt="Hidden Marriage"></a></div>
              <div class="txt"><h3 class="tit"><a href="/hidden-marriage.html" title="Hidden Marriage">Hidden Marriage</a></h3></div>
            </div></div></div>
        """.trimIndent()

        val books = NovgoParser.parseListing(html)

        assertEquals(1, books.size)
        assertEquals("Hidden Marriage", books[0].title)
        assertEquals("https://novgo.net/hidden-marriage.html", books[0].key)
        assertEquals("https://novgo.net/uploads/thumbs/hidden.jpg", books[0].cover)
    }

    @Test
    fun bookDetailsComeFromJsonLd() {
        val html = """
            <script type="application/ld+json">
            {"@type":"Book","name":"Hidden Marriage","description":"After five years.","image":"https://novgo.net/cover.jpg","genre":["Romance"],"author":[{"@type":"Person","name":"Jiong Jiong You Yao"}]}
            </script>
        """.trimIndent()

        val book = NovgoParser.parseBook(html, "https://novgo.net/hidden-marriage.html")

        assertEquals("Hidden Marriage", book.title)
        assertEquals("After five years.", book.description)
        assertEquals("Jiong Jiong You Yao", book.author)
        assertEquals(listOf("Romance"), book.genres)
        assertEquals("https://novgo.net/cover.jpg", book.cover)
    }

    @Test
    fun chapterAjaxHtmlBecomesChapterList() {
        val json = """
            {"code":200,"html":"<li><a href=\"/hidden-marriage/chapter-1.html\" class=\"con\" title=\"Chapter 1\"><span class=\"chapter-text\">Chapter 1: Start</span></a></li>"}
        """.trimIndent()

        val chapters = NovgoParser.parseChapterPage(json)

        assertEquals(1, chapters.size)
        assertEquals("Chapter 1: Start", chapters[0].name)
        assertEquals("https://novgo.net/hidden-marriage/chapter-1.html", chapters[0].key)
        assertEquals(1f, chapters[0].number)
    }

    @Test
    fun chapterPageCountReadsNovelId() {
        val html = """<div id="list-chapter" data-novel-id="184" data-total-page="44"></div>"""
        val info = NovgoParser.chapterListInfo(html)
        assertEquals("184", info?.novelId)
        assertEquals(44, info?.pages)
    }

    @Test
    fun contentReadsParagraphs() {
        val html = """<div id="chapter-content"><p>First.</p><p>Second.</p></div>"""
        assertEquals(listOf("First.", "Second."), NovgoParser.parseContent(html))
    }

    @Test
    fun listingUrlUsesPopularOrSearch() {
        assertTrue(NovgoParser.listingUrl("", 1).contains("/most-popular?page=1"))
        assertTrue(NovgoParser.listingUrl("hidden marriage", 2).contains("keyword=hidden"))
    }
}
