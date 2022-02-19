package org.ireader.domain.models.entities.model

import okhttp3.Headers
import org.ireader.source.core.HttpSource
import org.ireader.source.models.*
import org.jsoup.nodes.Document


open class TestSource : org.ireader.source.core.Source {
    override val id: Long = 1L
    override val lang: String = "en"
    override val name: String = "Test Source"
    override val baseUrl: String = ""
    override val creator: String = ""
    override val iconUrl: String = ""
    override val supportsLatest: Boolean = true
    override val supportsMostPopular: Boolean = true
    override val supportSearch: Boolean = true
    override val headers: Headers by lazy { headersBuilder().build() }

    protected open fun headersBuilder() = Headers.Builder().apply {
        add("User-Agent", HttpSource.DEFAULT_USER_AGENT)
    }

    override suspend fun getLatest(page: Int): BooksPage {
        TODO("Not yet implemented")
    }

    override suspend fun getPopular(page: Int): BooksPage {
        TODO("Not yet implemented")
    }

    override suspend fun getSearch(page: Int, query: String, filters: FilterList): BooksPage {
        TODO("Not yet implemented")
    }

    override suspend fun getContents(chapter: ChapterInfo): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getDetails(book: BookInfo): BookInfo {
        TODO("Not yet implemented")
    }

    override suspend fun getChapters(book: BookInfo): List<ChapterInfo> {
        TODO("Not yet implemented")
    }

    override fun popularParse(document: Document): BooksPage {
        TODO("Not yet implemented")
    }

    override fun latestParse(document: Document): BooksPage {
        TODO("Not yet implemented")
    }

    override fun detailParse(document: Document): BookInfo {
        TODO("Not yet implemented")
    }

    override fun chaptersParse(document: Document): List<ChapterInfo> {
        TODO("Not yet implemented")
    }

    override fun searchParse(document: Document): BooksPage {
        TODO("Not yet implemented")
    }

    override fun pageContentParse(document: Document): List<String> {
        TODO("Not yet implemented")
    }

    override fun getFilterList(): FilterList {
        TODO("Not yet implemented")
    }

    override fun getListings(): List<Listing> {
        TODO("Not yet implemented")
    }


}