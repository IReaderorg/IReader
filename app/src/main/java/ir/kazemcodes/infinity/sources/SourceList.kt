package ir.kazemcodes.infinity.data.network

import android.content.Context
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.sources.models.*


class Extensions(context: Context) {
    private val sources = mutableListOf<Source>(
        //FreeWebNovel(context),
       // WuxiaWorldApi(context),
    )

    fun getSources(): List<Source> {
        return sources
    }

    fun addSource(source: Source) {
        sources.add(source)
    }

    init {

        AvailableSources(context).sourcesList.forEach { source ->
            addSource(source)
        }

    }
}

class AvailableSources(context: Context) {

    val realwebnovel = SourceCreator(
        context = context,
        _baseUrl = "https://readwebnovels.net",
        _lang = "en",
        _name = "RealWebNovel",
        _supportsLatest = true,
        _supportsMostPopular = true,
        _supportsSearch = true,
        latest = Latest(
            endpoint = "/manga-2/page/{page}/?m_orderby=latest",
            selector = "div.page-item-detail",
            nameSelector = "a",
            nameAtt = "title",
            linkSelector = "a",
            linkAtt = "href",
            coverSelector = "img",
            coverAtt = "src",
            nextPageLinkSelector = "div.nav-previous",
            nextPageValue = "Older Posts",
        ),
        popular = Popular(
            endpoint = "/manga-2/page/{page}/?m_orderby=trending",
            selector = "div.page-item-detail",
            nextBookSelector = "div.nav-previous",
            linkSelector = "a",
            linkAtt = "href",
            nameSelector = "a",
            nameAtt = "title",
            coverSelector = "img",
            coverAtt = "src",
        ),
        search = Search(
            endpoint = "/?s={query}&post_type=wp-manga&op=&author=&artist=&release=&adult=",
            selector = "div.c-tabs-item__content",
            linkSelector = "div.tab-thumb a",
            linkAtt = "href",
            nameSelector = "h3.h4 a",
            coverSelector = "div.tab-thumb a img",
            coverAtt = "src",
        ),
        detail = Detail(
            nameSelector = "div.post-title h1",
            descriptionSelector = "div.summary__content",
            authorBookSelector = "div.author-content a",
            categorySelector = "div.genres-content a",
        ),
        chapterList = ChapterList(
            _isChapterStatsFromFirst = true,
            selector = "li.wp-manga-chapter",
            linkSelector = "a",
            linkAtt = "href",
            nameSelector = "a",
        ),
        content = Content(
            pageContentSelector = "div.reading-content h4,p",
        )
    )
    val freeWebNovel = SourceCreator(
        context = context,
        _name = "FreeWebNovel",
        _lang = "en",
        _baseUrl = "https://freewebnovel.com",
        _supportsLatest = true,
        _supportsSearch = true,
        _supportsMostPopular = true,
        latest = Latest(
            endpoint = "/latest-release-novel/{page}/",
            selector = "div.ul-list1 div.li",
            nameSelector = "div.txt a",
            nameAtt = "title",
            linkSelector = "div.txt a",
            linkAtt = "href",
            coverSelector = "div.pic img",
            coverAtt = "src",
            nextPageLinkSelector = "div.ul-list1",
            nextPageValue = "Next"
        ),
        popular = Popular(
            endpoint = "/most-popular-novel/",
            selector = "div.ul-list1",
            linkAtt = "href",
            nameAtt = "title",
            coverSelector = "img",
            coverAtt = "src",
        ),
        search = Search(
            endpoint = "/search?searchkey={query}",
            selector = "div.ul-list1 div.li",
            linkSelector = "div.txt a",
            linkAtt ="href",
            nameSelector = "div.txt a",
            nameAtt = "title",
            coverSelector = "div.pic img",
            coverAtt = "src",
        ),
        detail = Detail(
            nameSelector = "div.m-desc h1.tit",
            descriptionSelector = "div.inner",
            authorBookSelector = "div.right a.a1",
            authorBookAtt = "title",
            categorySelector = "div.item div.right a.a1",
        ),
        chapterList = ChapterList(
            supportNextPagesList = true,
            _isChapterStatsFromFirst = true,
            endpoint = "/{page}.html",
            chaptersEndpointWithoutPage = ".html",
            selector = "div.m-newest2 ul.ul-list5 li",
            linkSelector = "a",
            linkAtt = "href",
            nameSelector = "a",
            nameAtt = "title",
            hasNextChapterListSelector = "div.page a:nth-child(4)",
            hasNextChapterListValue = "Next",

        ),
        content = Content(
            pageContentSelector = "div.txt h4,p"
        )
    )

    val mtl = SourceCreator(
        context = context,
        _lang = "en",
        _name = "MtlNovel",
        _baseUrl = "https://www.mtlnovel.com",
        _supportsLatest = true,
        _supportsMostPopular = true,
        _supportsSearch = true,
        latest = Latest(
            endpoint = "/novel-list/?orderby=date&order=desc&status=all&pg={page}",
            selector = "div.box",
            nameSelector = "a.list-title",
            nameAtt = "aria-label",
            linkSelector ="a.list-title",
            linkAtt = "href",
            coverSelector = "amp-img.list-img",
            coverAtt = "src",
        ),
        popular = Popular(
            endpoint = "/monthly-rank/page/{page}/",
            selector = "div.box",
            coverSelector = "amp-img.list-img",
            coverAtt = "src",
            linkSelector = "a.list-title",
            linkAtt = "href",
            nameSelector = "aria-label",
            nameAtt =  "aria-label",
        ),
        search = Search(
            endpoint = "/wp-admin/admin-ajax.php?action=autosuggest&q={query}&__amp_source_origin=https%3A%2F%2Fwww.mtlnovel.com",
            isGetRequest = true,
            isHTMLType = false,
            selector = "$.items[0].results",
            nameSelector = "title",
            linkSelector = "permalink",
            coverSelector = "thumbnail"
        ),
        detail = Detail(
            nameSelector = "a.list-a",
            nameAtt = "aria-label",
            coverSelector = "amp-img.main-tmb img",
            coverAtt = "src",
            authorBookSelector = "#author a",
            categorySelector = "#currentgen a",
            descriptionSelector = "div.desc p"
        ),
        chapterList = ChapterList(
            _subStringSomethingAtEnd = "/chapter-list/",
            _shouldStringSomethingAtEnd = true,
            selector = "a.ch-link",
            nameSelector = "a",
            linkAtt = "href",
            hasNextChapterListSelector = "div.ch-list amp-list",
            hasNextChapterListAtt = "src",
            _isChapterStatsFromFirst = false

        ),
        content = Content(

            pageTitleSelector = "h1.main-title",
            pageContentSelector = "div.par p"
        )

    )
    val wuxiaworld = SourceCreator(
        context = context,
        _name = "Wuxiaworld",
        _lang = "en",
        _baseUrl = "https://wuxiaworld.site",
        latest = Latest(
            endpoint = "/novel-list/page/{page}/",
            selector = "div.page-item-detail",
            nameSelector = "h3.h5 a",
            linkSelector = "h3.h5 a",
            linkAtt = "href",
            coverSelector = "img",
            coverAtt = "src",
        ),
        detail = Detail(
            nameSelector = "h1",
            coverSelector = "div.summary_image a img",
            coverAtt = "src",
            descriptionSelector = "div.summary__content p",
            authorBookSelector = "div.author-content a",
            authorBookAtt = "rel",
            categorySelector = "div.genres-content a"
        ),
        chapterList = ChapterList(
            _shouldStringSomethingAtEnd = true,
            _subStringSomethingAtEnd = "ajax/chapters/",
            selector = "li.wp-manga-chapter",
            nameSelector = "a",
            linkSelector = "href",
            isGetRequest = false
        )
    )
    val sourcesList = listOf<Source>(
        realwebnovel,
        freeWebNovel,
        mtl,
        wuxiaworld
        )
}
