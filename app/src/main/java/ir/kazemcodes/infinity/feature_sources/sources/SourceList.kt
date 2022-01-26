package ir.kazemcodes.infinity.feature_sources.sources

import android.content.Context
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.feature_sources.sources.models.*


class Extensions(
    private val context: Context,
    ) {



    private val sources = mutableListOf<Source>(
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

    val baseSample = SourceTower(
        name = "",
        baseUrl = "",
        lang = "",
        supportsLatest = false,
        supportsMostPopular = false,
        supportSearch = false,
        creator = "@Kazem",
        latest = Latest(
            endpoint = null,
            ajaxSelector = null,
            isGetRequestType = true,
            isHtmlType = true,
            selector = null,
            addBaseUrlToLink = false,
            openInWebView = false,
            nextPageSelector = null,
            nextPageAtt = null,
            nextPageValue = null,
            linkSelector = null,
            linkAtt = null,
            nameSelector = null,
            nameAtt = null,
            coverSelector = null,
            coverAtt = null,
            supportPageList = false,
            maxPageIndex = null,
            addToStringEnd = null,
            idSelector = null,
        ),
        popular = Popular(
            endpoint = null,
            ajaxSelector = null,
            isGetRequestType = true,
            isHtmlType = true,
            selector = null,
            addBaseUrlToLink = false,
            openInWebView = false,
            nextPageSelector = null,
            nextPageAtt = null,
            nextPageValue = null,
            linkSelector = null,
            linkAtt = null,
            nameSelector = null,
            nameAtt = null,
            coverSelector = null,
            coverAtt = null,
            addToStringEnd = null,
            idSelector = null,

        ),
        search = Search(
            endpoint = null,
            ajaxSelector = null,
            isGetRequestType = true,
            isHtmlType = true,
            selector = null,
            addBaseUrlToLink = false,
            openInWebView = false,
            nextPageSelector = null,
            nextPageAtt = null,
            nextPageValue = null,
            linkSelector = null,
            linkAtt = null,
            nameSelector = null,
            nameAtt = null,
            coverSelector = null,
            coverAtt = null,
            addToStringEnd = null,
            idSelector = null,
        ),
        detail = Detail(
            endpoint = null,
            ajaxSelector = null,
            isGetRequestType = true,
            isHtmlType = true,
            selector = null,
            addBaseUrlToLink = false,
            openInWebView = false,
            nextPageSelector = null,
            nextPageAtt = null,
            nextPageValue = null,
            nameSelector = null,
            nameAtt = null,
            coverSelector = null,
            coverAtt = null,
            descriptionSelector = null,
            descriptionBookAtt = null,
            authorBookSelector = null,
            authorBookAtt = null,
            categorySelector = null,
            categoryAtt = null,
            addToStringEnd = null,
            idSelector = null,

        ),
        chapters = Chapters(
            endpoint = null,
            ajaxSelector = null,
            isGetRequestType = true,
            isHtmlType = true,
            selector = null,
            addBaseUrlToLink = false,
            openInWebView = false,
            nextPageSelector = null,
            nextPageAtt = null,
            nextPageValue = null,
            nameSelector = null,
            nameAtt = null,
            linkSelector = null,
            linkAtt = null,
            supportNextPagesList = false,
            chaptersEndpointWithoutPage = null,
            isChapterStatsFromFirst = true,
            isDownloadable = false,
            subStringSomethingAtEnd = null,
            addToStringEnd = null,
            idSelector = null,

        ),
        content = Content(
            endpoint = null,
            ajaxSelector = null,
            isGetRequestType = true,
            isHtmlType = true,
            selector = null,
            addBaseUrlToLink = false,
            openInWebView = false,
            nextPageSelector = null,
            nextPageAtt = null,
            nextPageValue = null,
            pageContentSelector = null,
            pageTitleAtt = null,
            pageTitleSelector = null,
            pageContentAtt = null,
            addToStringEnd = null,
            idSelector = null,
        ),
    )


    val realwebnovel = SourceTower(
        baseUrl = "https://readwebnovels.net",
        lang = "en",
        name = "RealWebNovel",
        supportsLatest = true,
        supportsMostPopular = true,
        supportSearch = true,
        creator = "@Kazem",
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
            isChapterStatsFromFirst = false,
            selector = "li.wp-manga-chapter",
            linkSelector = "a",
            linkAtt = "href",
            nameSelector = "a",
        ),
        content = Content(
            pageContentSelector = "div.reading-content h4,p",
        )
    )
    val freeWebNovel = SourceTower(
        name = "FreeWebNovel",
        lang = "en",
        baseUrl = "https://freewebnovel.com",
        supportsLatest = true,
        supportSearch = true,
        supportsMostPopular = true,
        creator = "@Kazem",
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

    val mtl = SourceTower(
        lang = "en",
        name = "MtlNovel",
        baseUrl = "https://www.mtlnovel.com",
        supportsLatest = true,
        supportsMostPopular = true,
        supportSearch = true,
        creator = "@Kazem",
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
    val wuxiaworld = SourceTower(
        name = "Wuxiaworld.Site",
        lang = "en",
        baseUrl = "https://wuxiaworld.site",
        supportsMostPopular = true,
        supportSearch = true,
        supportsLatest = true,
        creator = "@Kazem",
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
            ajaxSelector = "ul.main>li:nth-child(1)>a",
            selector = "li.wp-manga-chapter",
            isChapterStatsFromFirst = false,
            nameSelector = "a",
            linkSelector = "a",
            linkAtt = "href",
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
    val myLoveNovel = SourceTower(
        name = "MyLoveNovel",
        lang = "en",
        supportsLatest = true,
        baseUrl = "https://m.mylovenovel.com/",
        supportSearch = true,
        supportsMostPopular = true,
        creator = "@Kazem",
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
            addBaseUrlToLink = true,
            isChapterStatsFromFirst = true,
        ),
        content = Content(
            pageTitleSelector = "h1.headline",
            pageContentSelector = "div.content"
        )
    )
    val koreanMtl = SourceTower(
        name = "KoreanMtl.Online",
        lang = "en",
        supportsLatest = true,
        baseUrl = "https://www.koreanmtl.online/",
        supportSearch = false,
        supportsMostPopular = false,
        creator = "@Kazem",
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

    val mtlNation = SourceTower(
        name = "MtlNation",
        baseUrl = "https://mtlnation.com/",
        supportSearch = true,
        supportsMostPopular = true,
        supportsLatest = true,
        creator = "@Kazem",
        lang = "en",
        latest = Latest(
            endpoint = "/novel/page/{page}/?m_orderby=latest",
            selector = "div.page-item-detail",
            nameSelector = "h3.h5 a",
            linkSelector = "h3.h5 a",
            linkAtt = "href",
            coverSelector = "div.item-thumb a img",
            coverAtt = "src"
        ),
        popular = Popular(
            endpoint = "/novel/page/{page}/?m_orderby=views",
            selector = "div.page-item-detail",
            nameSelector = "h3.h5 a",
            linkSelector = "h3.h5 a",
            linkAtt = "href",
            coverSelector = "div.item-thumb a img",
            coverAtt = "src"
        ), search = Search(
            endpoint = "/page/{page}/?s={query}&post_type=wp-manga&op&author&artist&release&adult",
            selector = "div.c-tabs-item div.row",
            coverSelector = "div.c-image-hover a img",
            coverAtt = "src",
            linkSelector = "div.c-image-hover a",
            linkAtt = "href",
            nameSelector = "h3.h4 a",
        ),
        content = Content(
            selector = "div.c-blog-post",
            pageContentSelector = "p"
        ),
        chapters = Chapters(
            ajaxSelector = "a",
            selector = "ul.main li",
            nameSelector = "a",
            linkSelector = "a",
            linkAtt = "href",
            isChapterStatsFromFirst = false,
        ),
        detail = Detail(
            nameSelector = "div.post-title h1",
            coverSelector = "div.summary_image a img",
            coverAtt = "src",
            descriptionSelector = "div.summary__content p",
            categorySelector = "div.genres-content a",
            authorBookSelector = "div.author-content a"
        )
    )
    val realLightWebNovel = SourceTower(
        name = "RealLightWebNovel",
        baseUrl = "https://readlightnovels.net",
        supportSearch = true,
        supportsMostPopular = false,
        supportsLatest = true,
        creator = "@Kazem",
        lang = "en",
        latest = Latest(
            endpoint = "/latest/page/{page}",
            selector = "div.row div.home-truyendecu",
            nameSelector = "h3",
            linkSelector = "a",
            linkAtt = "href",
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "ul.pagination>li:nth-child(6)>a",
            nextPageValue = "Last"

        ),
        content = Content(
            selector = "div.chapter-content",
            pageTitleSelector = "a.chapter-title",
            pageContentSelector = "p"
        ),
        chapters = Chapters(
            isGetRequestType = false,
            selector = "div.col-xs-12 ul.list-chapter li",
            nameSelector = "span.chapter-text",
            linkSelector = "a",
            linkAtt = "href",
            supportNextPagesList = true,
            isChapterStatsFromFirst = true,
            endpoint = "action=tw_ajax&type=pagination&id=390165&page={page}",
            chaptersEndpointWithoutPage = ".html",
        ),
        detail = Detail(
            nameSelector = "h2.single_title",
            coverSelector = "div.book img",
            coverAtt = "src",
            descriptionSelector = "div.desc-text p",
            categorySelector = "div.info>div:nth-child(2)",
            authorBookSelector = "div.info>div:nth-child(1)>a"
        ),
        search = Search(
            endpoint = "/page/{page}?s={query}",
            selector = "div.row div.home-truyendecu",
            nameSelector = "h3",
            linkSelector = "a",
            linkAtt = "href",
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "ul.pagination>li:nth-child(6)>a",
            nextPageValue = "Last"

        )
    )
    val wuxiaworldsiteco = SourceTower(
        name = "Wuxiaworldsite.co",
        baseUrl = "https://wuxiaworldsite.co",
        supportSearch = true,
        supportsMostPopular = true,
        supportsLatest = true,
        creator = "@Kazem",
        lang = "en",
        latest = Latest(
            isGetRequestType = false,
            endpoint = "/my-library?page={page}&keyword=&count=6&genres_include=",
            selector = "div.one_item",
            nameSelector = "a",
            nameAtt = "title",
            linkSelector = "a",
            linkAtt = "href",
            addBaseUrlToLink = true,
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "div.paging_section > div > span:nth-child(4)",
            nextPageValue = ">|",
            addBaseurlToCoverLink = true

        ),
        popular = Popular(
            isGetRequestType = false,
            endpoint = "/power-ranking?page={page}&keyword=&count=18&genres_include=&limit=18&order_type=DESC&order_by=real_time",
            selector = "div.one_item",
            nameSelector = "a",
            nameAtt = "title",
            linkSelector = "a",
            linkAtt = "href",
            addBaseUrlToLink = true,
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "no-more-page",
            addBaseurlToCoverLink = true
        ),
        content = Content(
            selector = "div.content-story",
            pageTitleSelector = "a.chapter-title",
            pageContentSelector = "p:not(:first-child)"
        ),
        chapters = Chapters(
            selector = "div.chapter_wrapper a",
            nameSelector = "a",
            linkSelector = "a",
            linkAtt = "href",
            addBaseUrlToLink = true,
            isChapterStatsFromFirst = true,
            addBaseurlToCoverLink = true
        ),
        detail = Detail(
            nameSelector = "h1.heading_read",
            coverSelector = "div.img-read img",
            coverAtt = "src",
            descriptionSelector = "div.story-introduction-content p",
            categorySelector = "div.content-reading div.d-flex a",
            categoryAtt = "title",
            authorBookSelector = "div.content-reading i",
            addBaseurlToCoverLink = true
        ),
        search = Search(
            isGetRequestType = false,
            endpoint = "/search/{query}?page={page}&keyword={query}&count=18&genres_include=&limit=18&order_type=DESC&order_by=real_time",
            selector = "div.content_game div.item",
            nameSelector = "a",
            nameAtt = "title",
            linkSelector = "a",
            linkAtt = "href",
            coverSelector = "img",
            coverAtt = "src",
            nextPageSelector = "div.paging_section > div > span:nth-child(5)",
            nextPageValue = ">|",
            addBaseurlToCoverLink = true

        )
    )


    val sourcesList = listOf<Source>(
        freeWebNovel,
        koreanMtl,
        myLoveNovel,
        mtl,
        realwebnovel,
        wuxiaworld,
        mtlNation,
        realLightWebNovel,
        wuxiaworldsiteco
    )
}
