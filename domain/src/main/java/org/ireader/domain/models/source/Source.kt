package org.ireader.domain.models.source

import okhttp3.Headers
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.FilterList
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

    /**
     * This is false when the content is viewed in web view not in  the app
     */
    val supportContentAppView: Boolean


    val headers: Headers

    /**
     * Returns a page with a list of latest books updates and weather is has next page or not.
     *
     * @param page the page number to retrieve.
     */
    suspend fun getLatest(page: Int): BooksPage


    /**
     * Returns a page with a list of popular books updates and weather is has next page or not.
     *
     * @param page the page number to retrieve.
     */
    suspend fun getPopular(page: Int): BooksPage

    /**
     * Returns a page with a list of books.
     *
     * @param page the page number to retrieve.
     * @param query the search query.
     */
    suspend fun getSearch(page: Int, query: String, filters: FilterList): BooksPage


    /**
     * Returns a page with a list of latest books updates and weather is has next page or not.
     *
     * @param chapter the chapter that it contents is going to be retrieve.
     */
    suspend fun getContentList(chapter: Chapter): ContentPage


    /**
     * Returns a book with Complete information.
     *
     * @param book a book that contain need to be contain a bookName and a link
     */
    suspend fun getDetails(book: Book): Book

    /**
     * Returns a list of chapter.
     *
     * @param book a book that contain need to be contain a bookName and a link
     */
    suspend fun fetchChapters(book: Book, page: Int): ChaptersPage

    fun popularParse(document: Document, page: Int = 0): BooksPage
    fun latestParse(document: Document, page: Int = 0): BooksPage
    fun detailParse(document: Document): Book
    fun chaptersParse(document: Document): ChaptersPage
    fun searchParse(document: Document, page: Int = 0): BooksPage
    fun pageContentParse(document: Document): ContentPage

    /**
     * Returns the list of filters for the source.
     */
    fun getFilterList(): FilterList
    fun getRegex(): Regex {
        return Regex("")
    }
}

