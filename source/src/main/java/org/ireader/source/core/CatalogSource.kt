package org.ireader.source.core

import okhttp3.Headers
import org.ireader.source.models.*
import org.jsoup.nodes.Document

interface CatalogSource : Source {

    val baseUrl: String

    val headers: Headers

    override val lang: String

    //TODO remove this later
    val iconUrl: String

    suspend fun getBookList(sort: Listing?, page: Int): BookPageInfo

    suspend fun getBookList(filters: FilterList, page: Int): BookPageInfo

    suspend fun getSearch(query: String, filters: FilterList, page: Int): BookPageInfo

    fun chaptersParse(document: Document): List<ChapterInfo>

    fun detailParse(document: Document): BookInfo

    fun pageContentParse(
        document: Document,
    ): List<String>

    fun getListings(): List<Listing>

    fun getFilters(): FilterList


}
