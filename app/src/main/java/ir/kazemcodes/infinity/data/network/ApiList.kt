package ir.kazemcodes.infinity.data.network

import android.content.Context
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.data.network.models.SourceCreator
import ir.kazemcodes.infinity.data.network.sources.WuxiaWorldApi


class Extensions(context: Context) {
    private val sources = mutableListOf<Source>(
        //FreeWebNovel(context),
        WuxiaWorldApi(context),
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
        _latestUpdateEndpoint = "/manga-2/page/{page}/?m_orderby=latest",
        _popularEndpoint = "/manga-2/page/{page}/?m_orderby=trending",
        _searchEndpoint = "/?s={query}&post_type=wp-manga&op=&author=&artist=&release=&adult=",
        _popularBookSelector = "div.page-item-detail",
        _popularNextBookSelector = "div.nav-previous",
        _linkPopularSelector = "a",
        _linkPopularAtt = "href",
        _namePopularSelector = "a",
        _namePopularAtt = "title",
        _coverPopularSelector = "img",
        _coverPopularAtt = "src",
        _latestBookSelector = "div.page-item-detail",
        _latestNextPageSelector = "div.nav-previous",
        _latestNextPageValue = "Older Posts",
        _linkLatestSelector = "a",
        _linkLatestAtt = "href",
        _nameLatestSelector = "a",
        _nameLatestAtt = "title",
        _coverLatestSelector = "img",
        _coverLatestAtt = "src",
        _nameDetailSelector = "div.post-title h1",
        _descriptionDetailSelector = "div.summary__content",
        _authorDetailBookSelector = "div.author-content a",
        _categoryDetailSelector = "div.genres-content a",
        _isChapterStatsFromFirst = true,
        _chapterListSelector = "li.wp-manga-chapter",
        _linkChapterSelector = "a",
        _nameChapterSelector = "a",
        _chapterPageContentSelector = "div.reading-content h4,p",
        _searchBookSelector = "div.c-tabs-item__content",
        _linkSearchedSelector = "div.tab-thumb a",
        _linkSearchedAtt = "href",
        _nameSearchedSelector = "h3.h4 a",
        _coverSearchedSelector = "div.tab-thumb a img",
        _coverSearchedAtt = "src",
    )
    val freeWebNovel = SourceCreator(
        context = context,
        _name = "FreeWebNovel",
        _lang = "en",
        _baseUrl = "https://freewebnovel.com",
        _supportsLatest = true,
        _supportsSearch = true,
        _supportsMostPopular = true,
        _popularBookSelector = "div.ul-list1",
        _popularEndpoint = "/most-popular-novel/",
        _linkPopularAtt = "href",
        _namePopularAtt = "title",
        _coverPopularSelector = "img",
        _coverPopularAtt = "src",
        _latestUpdateEndpoint = "/latest-release-novel/{page}/",
        _latestBookSelector = "div.ul-list1 div.li",
        _linkLatestSelector = "div.txt a",
        _linkLatestAtt = "href",
        _nameLatestSelector = "div.txt a",
        _nameLatestAtt = "title",
        _coverLatestSelector = "div.pic img",
        _coverLatestAtt = "src",
        _latestNextPageSelector = "div.ul-list1",
        _latestNextPageValue = "Next",
        _nameDetailSelector = "div.m-desc h1.tit",
        _descriptionDetailSelector = "div.inner",
        _authorDetailBookSelector = "div.right a.a1",
        _authorDetailBookAtt = "title",
        _categoryDetailSelector = "div.item div.right a.a1",
        _supportChapterPage = true,
        _chaptersEndpoint = "/{page}.html",
        _chaptersEndpointWithoutPage = ".html",
        _chapterListSelector = "div.m-newest2 ul.ul-list5 li",
        _nameChapterSelector = "a",
        _nameChapterAtt = "title",
        _linkChapterSelector = "a",
        _linkChapterAtt = "href",
        _hasNextChapterListSelector = "div.page a:nth-child(4)",
        _hasNextChapterListValue = "Next",
        _searchBookSelector = "div.ul-list1 div.li",
        _linkSearchedSelector = "div.txt a",
        _linkSearchedAtt = "href",
        _nameSearchedSelector = "div.txt a",
        _nameSearchedAtt = "title",
        _coverSearchedSelector = "div.pic img",
        _coverSearchedAtt = "src",
        _searchEndpoint = "/search?searchkey={query}",
        _chapterPageContentSelector = "div.txt h4,p"
    )
    val mtlnovel = SourceCreator(
        context = context,
        _lang = "en",
        _name = "MtlNovel",
        _baseUrl = "https://www.mtlnovel.com/",
        _supportsLatest = true,
        _latestUpdateEndpoint = "/novel-list/?orderby=date&order=desc&status=all&pg={page}",
        _latestBookSelector = "div.box",
        _coverLatestAtt = "src",
        _coverLatestSelector = "amp-img.list-img",
        _nameLatestSelector = "a.list-title",
        _nameLatestAtt = "aria-label",
        _linkLatestSelector = "a.list-title",
        _linkLatestAtt = "href",
        _popularEndpoint = "https://www.mtlnovel.com/monthly-rank/page/{page}/",
        _supportsMostPopular = true,
        _popularBookSelector = "div.box",
        _coverPopularSelector = "amp-img.list-img",
        _coverPopularAtt = "src",
        _linkPopularSelector = "a.list-title",
        _linkPopularAtt = "href",
        _namePopularSelector = "aria-label",
        _namePopularAtt = "aria-label",
        _supportsSearch = true,
        _searchEndpoint = "/wp-admin/admin-ajax.php?action=autosuggest&q={query}.&__amp_source_origin=https%3A%2F%2Fwww.mtlnovel.com",
        _searchIsGetRequest = true,
        _searchIsHTMLType = false,
        _nameSearchedSelector = "aria-label",
        _nameSearchedAtt = "aria-label",
        _coverSearchedSelector = "amp-img.list-img",
        _coverSearchedAtt = "src",
        _linkSearchedSelector = "a.list-title",
        _linkSearchedAtt = "href",



    )
    val sourcesList = listOf<Source>(
        realwebnovel,
        freeWebNovel,
        mtlnovel
        )
}
