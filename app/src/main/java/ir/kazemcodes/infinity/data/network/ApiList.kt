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
        //RealWebNovelApi(context)
    )
    fun getSources() : List<Source>{
        return sources
    }
    fun addSource(source: Source) {
        sources.add(source)
    }
    init {
        val source = SourceCreator(
            context = context,
            _name = "RealWebNovel",
            _baseUrl = "https://readwebnovels.net",
            _lang = "en",
            _supportsLatest = true,
            _supportsMostPopular = true,
            _popularBookSelector = "div.page-item-detail",
            _popularNextBookSelector = "div.nav-previous",
            _popularEndpoint = "/manga-2/page/{page}/?m_orderby=trending",
            _linkPopularSelector = "a",
            _linkAttPopularSelector = "href",
            _namePopularBookSelector = "a",
            _nameAttPopularBookSelector = "title",
            _bookCoverPopularBookSelector = "img",
            _bookAttCoverPopularBookSelector = "src",
            _latestUpdateEndpoint = "/manga-2/page/{page}/?m_orderby=latest",
            _latestBookSelector = "div.page-item-detail",
            _nameLatestBookSelector = "a",
            _nameAttLatestBookSelector = "title",
            _linkLatestSelector = "a",
            _linkAttLatestSelector = "href",
            _bookCoverLatestBookSelector = "img",
            _bookCoverAttLatestBookSelector = "src",
            _latestNextBookSelector = "div.nav-previous",
            _bookNameBookDetailSelector = "div.post-title h1",
            _descriptionBookDetailBookSelector = "div.summary__content",
            _authorBookDetailBookSelector = "div.author-content a",
            _categoryBookDetailBookSelector = "div.genres-content a",
            _chapterListSelector = "li.wp-manga-chapter",
            _nameChaptersBookSelector = "a",
            _linkChaptersSelector =  "a",
            _chapterPageContentSelector = "div.reading-content h4,p",
            _searchBookItemSelector = "div.c-tabs-item__content",
            _linkSearchedBookSelector = "div.tab-thumb a",
            _linkAttChaptersSelector = "href",
            _nameSearchedBookSelector = "h4",
            _bookCoverSearchedBookSelector = "div.tab-thumb a img",
            _bookCoverAttSearchedBookSelector = "src",
            _searchEndpoint = "/?s=##{query}##&post_type=wp-manga",


        )
        addSource(source)
    }
}