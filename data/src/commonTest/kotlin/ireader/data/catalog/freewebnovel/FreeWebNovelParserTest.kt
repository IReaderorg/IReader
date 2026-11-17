package ireader.data.catalog.freewebnovel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FreeWebNovelParserTest {

    @Test
    fun latestListingReadsCards() {
        val html = """
            <div class="ul-list1">
              <div class="li-row">
                <div class="li">
                  <div class="pic"><img src="/uploads/dao.jpg"></div>
                  <div class="txt"><h3 class="tit"><a href="/novel/dao" title="Follow the path of Dao from infancy">Follow the path of Dao from infancy</a></h3></div>
                </div>
              </div>
            </div>
            <div class="pages"><a href="/sort/latest-release/2">2</a></div>
        """.trimIndent()

        val page = FreeWebNovelParser.parseLatest(html)

        assertEquals(1, page.mangas.size)
        assertEquals("Follow the path of Dao from infancy", page.mangas[0].title)
        assertEquals("https://freewebnovel.com/novel/dao", page.mangas[0].key)
        assertEquals("https://freewebnovel.com/uploads/dao.jpg", page.mangas[0].cover)
        assertTrue(page.hasNextPage)
        assertEquals("https://freewebnovel.com/sort/latest-release", FreeWebNovelParser.latestUrl(1))
        assertEquals("https://freewebnovel.com/sort/latest-release/2", FreeWebNovelParser.latestUrl(2))
    }

    @Test
    fun popularListingReadsRows() {
        val html = """
            <div class="ul-list1">
              <div class="li-row">
                <a href="/novel/dao" title="Follow the path of Dao from infancy"></a>
                <img src="/uploads/dao.jpg">
              </div>
            </div>
        """.trimIndent()

        val page = FreeWebNovelParser.parsePopular(html)

        assertEquals("https://freewebnovel.com/novel/dao", page.mangas[0].key)
        assertEquals("Follow the path of Dao from infancy", page.mangas[0].title)
        assertFalse(page.hasNextPage)
    }

    @Test
    fun detailsReadBookPage() {
        val html = """
            <div class="m-desc"><h1 class="tit">Follow the path of Dao from infancy</h1></div>
            <div class="m-book1"><div class="pic"><img src="/uploads/dao.jpg"></div></div>
            <div class="inner"><p>A long life.</p><p>More.</p></div>
            <div class="right"><a class="a1" title="Ancient Xi" href="/author/Ancient-Xi"></a></div>
            <span title="Genre"></span><span>Xianxia, Action</span>
            <span title="Status"></span><span>Complete</span>
            <div id="indexListPage" data-total-page="3"></div>
        """.trimIndent()

        val book = FreeWebNovelParser.parseBook(html, "https://freewebnovel.com/novel/dao")

        assertEquals("Follow the path of Dao from infancy", book.title)
        assertEquals("Ancient Xi", book.author)
        assertEquals("A long life.\nMore.", book.description)
        assertEquals(listOf("Xianxia", "Action"), book.genres)
        assertEquals(2L, book.status)
        assertEquals("https://freewebnovel.com/novel/dao", book.key)
        assertEquals(3, FreeWebNovelParser.chapterPageCount(html))
    }

    @Test
    fun chaptersAndContent() {
        val chapters = FreeWebNovelParser.parseChapters(
            """
            <div class="m-newest2"><ul class="ul-list5">
              <li><a href="/novel/dao/chapter-1575" title="Chapter 1575" class="con">Chapter 1575</a></li>
            </ul></div>
            """.trimIndent()
        )
        assertEquals("Chapter 1575", chapters[0].name)
        assertEquals("https://freewebnovel.com/novel/dao/chapter-1575", chapters[0].key)

        val ajax = FreeWebNovelParser.parseChapterAjax(
            """{"code":200,"html":"<li><a href=\"/novel/dao/chapter-41\" title=\"Chapter 41\" class=\"con\">Chapter 41</a></li>"}"""
        )
        assertEquals("https://freewebnovel.com/novel/dao/chapter-41", ajax[0].key)

        val paragraphs = FreeWebNovelParser.parseContent(
            """<div id="article"><p>First line.</p></div>"""
        )
        assertEquals(listOf("First line."), paragraphs)
    }
}
