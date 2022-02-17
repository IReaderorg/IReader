package org.ireader.domain.models.source

import okhttp3.Headers
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.FilterList
import org.ireader.domain.source.nec.Listing
import org.ireader.source.models.BookInfo
import org.jsoup.nodes.Document

/** Source : tachiyomi**/

interface Source {

    val sourceId: Long

    val lang: String

    val name: String

    val baseUrl: String

    val creator: String

    val iconUrl: String


    val supportsLatest: Boolean

    val supportsMostPopular: Boolean

    val supportSearch: Boolean



    val headers: Headers

    suspend fun getLatest(page: Int): BooksPage

    suspend fun getPopular(page: Int): BooksPage

    suspend fun getSearch(page: Int, query: String, filters: FilterList): BooksPage

    suspend fun getContents(chapter: Chapter): List<String>

    suspend fun getDetails(book: Book): BookInfo

    suspend fun fetchChapters(book: Book): List<Chapter>

    fun popularParse(document: Document): BooksPage
    fun latestParse(document: Document): BooksPage
    fun detailParse(document: Document): BookInfo
    fun chaptersParse(document: Document): List<Chapter>
    fun searchParse(document: Document): BooksPage
    fun pageContentParse(document: Document): List<String>

    /**
     * Returns the list of filters for the source.
     */
    fun getFilterList(): FilterList
    fun getListings(): List<Listing>
    fun getRegex(): Regex {
        return Regex("")
    }
}

