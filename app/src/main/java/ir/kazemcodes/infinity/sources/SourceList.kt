package ir.kazemcodes.infinity.sources

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
            nextPageValue = "Older Posts",
            nextPageSelector = "div.nav-previous>a"
        ),
        popular = Popular(
            endpoint = "/manga-2/page/{page}/?m_orderby=trending",
            selector = "div.page-item-detail",
            linkSelector = "a",
            linkAtt = "href",
            nameSelector = "a",
            nameAtt = "title",
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "div.nav-previous>a"
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
        chapters = Chapters(
            isChapterStatsFromFirst = true,
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
            nextPageValue = "Next",
            nextPageSelector = "div.ul-list1"
        ),
        popular = Popular(
            endpoint = "/most-popular-novel/",
            selector = "div.ul-list1",
            linkAtt = "href",
            nameAtt = "title",
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "body > div.main > div > div.row-box > div.col-content > div.pages > ul > li > a:nth-child(14)"
        ),
        search = Search(
            endpoint = "/search?searchkey={query}",
            selector = "div.ul-list1 div.li",
            linkSelector = "div.txt a",
            linkAtt = "href",
            nameSelector = "div.txt a",
            nameAtt = "title",
            coverSelector = "div.pic img",
            coverAtt = "src",
            nextPageSelector = "body > div.main > div > div.row-box > div.col-content > div.pages > ul > li > a:nth-child(14)"
        ),
        detail = Detail(
            nameSelector = "div.m-desc h1.tit",
            descriptionSelector = "div.inner",
            authorBookSelector = "div.right a.a1",
            authorBookAtt = "title",
            categorySelector = "div.item div.right a.a1",
        ),
        chapters = Chapters(
            supportNextPagesList = true,
            isChapterStatsFromFirst = true,
            endpoint = "/{page}.html",
            chaptersEndpointWithoutPage = ".html",
            selector = "div.m-newest2 ul.ul-list5 li",
            linkSelector = "a",
            linkAtt = "href",
            nameSelector = "a",
            nameAtt = "title",
            nextPageSelector = "div.page a:nth-child(4)",
            nextPageValue = "Next",

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
            linkSelector = "a.list-title",
            linkAtt = "href",
            coverSelector = "amp-img.list-img",
            coverAtt = "src",
            nextPageSelector = "#pagination > a:nth-child(13)"
        ),
        popular = Popular(
            endpoint = "/monthly-rank/page/{page}/",
            selector = "div.box",
            coverSelector = "amp-img.list-img",
            coverAtt = "src",
            linkSelector = "a.list-title",
            linkAtt = "href",
            nameSelector = "aria-label",
            nameAtt = "aria-label",
            nextPageSelector = "#pagination > a:nth-child(13)"
        ),
        search = Search(
            endpoint = "/wp-admin/admin-ajax.php?action=autosuggest&q={query}&__amp_source_origin=https%3A%2F%2Fwww.mtlnovel.com",
            isGetRequestType = true,
            isHtmlType = false,
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
        chapters = Chapters(
            subStringSomethingAtEnd = "/chapter-list/",
            selector = "a.ch-link",
            nameSelector = "a",
            linkAtt = "href",
            nextPageSelector = "div.ch-list amp-list",
            nextPageAtt = "src",
            isChapterStatsFromFirst = false

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
        _supportsMostPopular = true,
        _supportsSearch = true,
        _supportsLatest = true,
        latest = Latest(
            endpoint = "/novel-list/page/{page}/",
            selector = "div.page-item-detail",
            nameSelector = "h3.h5 a",
            linkSelector = "h3.h5 a",
            linkAtt = "href",
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "div.nav-previous>a"
        ),
        detail = Detail(
            nameSelector = "div.post-title>h1",
            coverSelector = "div.summary_image a img",
            coverAtt = "src",
            descriptionSelector = "div.description-summary div.summary__content p",
            authorBookSelector = "div.author-content>a",
            categorySelector = "div.genres-content a",
        ),
        chapters = Chapters(
            subStringSomethingAtEnd = "ajax/chapters/",
            ajaxSelector = "ul.main>li:nth-child(1)>a",
            selector = "li.wp-manga-chapter",
            nameSelector = "a",
            linkSelector = "a",
            linkAtt = "href",
            isGetRequestType = false
        ), content = Content(
            ajaxSelector = "div.reading-content div.text-left p:nth-child(3)",
            selector = "div.read-container div.reading-content h3,p",
            pageTitleSelector = "div.text-left>h3",
            pageContentSelector = "div.text-left>p",
        ),
        popular = Popular(
            endpoint = "/novel-list/page/{page}/?m_orderby=views",
            selector = "div.page-item-detail",
            nameSelector = "h3.h5 a",
            linkSelector = "h3.h5 a",
            linkAtt = "href",
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "div.nav-previous>a"
        ), search = Search(
            endpoint = "/?s={query}&post_type=wp-manga&op=&author=&artist=&release=&adult=",
            selector = "div.c-tabs-item__content",
            nameSelector = "div.post-title h3.h4 a",
            linkSelector = "div.post-title h3.h4 a",
            linkAtt = "href",
            coverSelector = "img",
            coverAtt = "src",
        )
    )
    val myLoveNovel = SourceCreator(
        context = context,
        _name = "MyLoveNovel",
        _lang = "en",
        _supportsLatest = true,
        _baseUrl = "https://m.mylovenovel.com/",
        _supportsSearch = true,
        _supportsMostPopular = true,
        latest = Latest(
            endpoint = "/lastupdate-{page}.html",
            selector = "ul.list li a",
            nameSelector = "p.bookname",
            linkAtt = "href",
            addBaseUrlToLink = true,
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "div.pagelist>a",
            nextPageValue = "next"
        ),
        popular = Popular(
            endpoint = "/monthvisit-{page}.html",
            selector = "ul.list li a",
            nameSelector = "p.bookname",
            linkAtt = "href",
            addBaseUrlToLink = true,
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "div.pagelist>a",
        ),
        search = Search(
            endpoint = "/index.php?s=so&module=book&keyword={query}",
            selector = "ul.list li a",
            nameSelector = "p.bookname",
            linkAtt = "href",
            addBaseUrlToLink = true,
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "div.pagelist>a",
        ),
        detail = Detail(
            nameSelector = "div.detail img",
            nameAtt = "alt",
            coverSelector = "div.detail img",
            coverAtt = "src",
            authorBookSelector = "#info > div.main > div.detail > p:nth-child(3)",
            categorySelector = "div.detail p.line a",
            descriptionSelector = "div.intro",
        ),
        chapters = Chapters(
            selector = "#morelist ul.vlist li",
            nameSelector = "a",
            linkSelector = "a",
            linkAtt = "href",
            addBaseUrlToLink = true
        ),
        content = Content(
            pageTitleSelector = "h1.headline",
            pageContentSelector = "div.content"
        )
    )
    val koreanMtl = SourceCreator(
        context = context,
        _name = "KoreanMtl.Online",
        _lang = "en",
        _supportsLatest = true,
        _baseUrl = "https://www.koreanmtl.online/",
        _supportsSearch = false,
        _supportsMostPopular = false,
        latest = Latest(
            endpoint = "/p/novels-listing.html",
            selector = "ul.a li.b",
            nameSelector = "a",
            linkSelector = "a",
            linkAtt = "href",
            nextPageSelector = "#manga-item-460401 > a > img",
            nextPageValue = "#manga-item-460401 > a > img"
        ),
        search = Search(
            endpoint = "/index.php?s=so&module=book&keyword={query}",
            selector = "ul.list li a",
            nameSelector = "p.bookname",
            linkAtt = "href",
            addBaseUrlToLink = true,
            coverSelector = "img",
            coverAtt = "src",
        ),
        detail = Detail(
            descriptionSelector = "div.post-body p",
        ),
        chapters = Chapters(
            selector = "div.post-body ul.a li.a",
            nameSelector = "a",
            linkSelector = "a",
            linkAtt = "href",
        ),
        content = Content(
            pageTitleSelector = "h1",
            pageContentSelector = "p"
        )
    )
    val sourcesList = listOf<Source>(
        realwebnovel,
        freeWebNovel,
        mtl,
        wuxiaworld,
        myLoveNovel,
        koreanMtl
    )
}
