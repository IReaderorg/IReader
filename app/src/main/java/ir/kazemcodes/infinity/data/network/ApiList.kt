package ir.kazemcodes.infinity.data.network

import android.content.Context
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.data.network.models.SourceCreator
import ir.kazemcodes.infinity.data.network.sources.FreeWebNovel
import ir.kazemcodes.infinity.data.network.sources.WuxiaWorldApi


class Extensions(context: Context) {
    private val sources = mutableListOf<Source>(
        FreeWebNovel(context),
        WuxiaWorldApi(context),
    )

    fun getSources(): List<Source> {
        return sources
    }

    fun addSource(source: Source) {
        sources.add(source)
    }

    init {
        val realwebnovel = SourceCreator(
            context = context,
            _name = "RealWebNovel",
            _baseUrl = "https://readwebnovels.net",
            _lang = "en",
            _supportsLatest = true,
            _supportsMostPopular = true,
            _supportsSearch = true,
            _popularBookSelector = "div.page-item-detail",
            _popularNextBookSelector = "div.nav-previous",
            _popularEndpoint = "/manga-2/page/{page}/?m_orderby=trending",
            _linkPopularSelector = "a",
            _linkPopularAtt = "href",
            _namePopularSelector = "a",
            _namePopularAtt = "title",
            _coverPopularSelector = "img",
            _coverPopularAtt = "src",
            _latestUpdateEndpoint = "/manga-2/page/{page}/?m_orderby=latest",
            _latestBookSelector = "div.page-item-detail",
            _nameLatestSelector = "a",
            _nameLatestAtt = "title",
            _linkLatestSelector = "a",
            _linkLatestAtt = "href",
            _coverLatestSelector = "img",
            _coverLatestAtt = "src",
            _latestNextPageSelector = "div.nav-previous",
            _latestNextPageValue = "Older Posts",
            _nameDetailSelector = "div.post-title h1",
            _descriptionDetailSelector = "div.summary__content",
            _authorDetailBookSelector = "div.author-content a",
            _categoryDetailSelector = "div.genres-content a",
            _isChapterStatsFromFirst = true,
            _chapterListSelector = "li.wp-manga-chapter",
            _nameChapterSelector = "a",
            _linkChapterSelector = "a",
            _chapterPageContentSelector = "div.reading-content h4,p",
            _searchBookSelector = "div.c-tabs-item__content",
            _linkSearchedSelector = "div.tab-thumb a",
            _linkSearchedAtt = "href",
            _nameSearchedSelector = "h3.h4 a",
            _coverSearchedSelector = "div.tab-thumb a img",
            _coverSearchedAtt = "src",
            _searchEndpoint = "/?s={query}&post_type=wp-manga&op=&author=&artist=&release=&adult=",
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
            _hasNextChapterListSelector = "div.page a:nth-child(4)",
            _hasNextChapterListValue = "Next",
            _searchBookSelector = "div.ul-list1 div.li",
            _linkSearchedSelector = "div.txt a",
            _linkSearchedAtt = "href",
            _nameSearchedSelector = "div.txt a",
            _nameSearchedAtt = "title",


        )
        addSource(realwebnovel)
    }
}