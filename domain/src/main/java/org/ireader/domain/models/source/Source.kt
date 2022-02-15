package org.ireader.domain.models.source

import okhttp3.Headers
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.jsoup.nodes.Document


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
    suspend fun fetchLatest(page: Int): BooksPage


    /**
     * Returns a page with a list of popular books updates and weather is has next page or not.
     *
     * @param page the page number to retrieve.
     */
    suspend fun fetchPopular(page: Int): BooksPage

    /**
     * Returns a page with a list of books.
     *
     * @param page the page number to retrieve.
     * @param query the search query.
     */
    suspend fun fetchSearch(page: Int, query: String): BooksPage


    /**
     * Returns a page with a list of latest books updates and weather is has next page or not.
     *
     * @param chapter the chapter that it contents is going to be retrieve.
     */
    suspend fun fetchContent(chapter: Chapter): ContentPage


    /**
     * Returns a book with Complete information.
     *
     * @param book a book that contain need to be contain a bookName and a link
     */
    suspend fun fetchBook(book: Book): Book

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
    fun contentFromElementParse(document: Document): ContentPage

}

