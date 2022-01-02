package ir.kazemcodes.infinity.data.network.models

import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter


interface Source {

    val id: Long

    val lang: String

    val name: String

    val baseUrl: String


    val supportsLatest: Boolean

    val supportsMostPopular: Boolean
    val pageFormat : String
    val searchQueryFormat: String

    /**
     *return the end point for the fetch latest books updates feature,
     * if there is not endpoint just return null
     * note: use "##{page}##" in the endpoint instead of page number
     */
    fun fetchLatestUpdatesEndpoint(): String?

    /**
     *return the end point for the  fetch Popular books feature,
     * if there is not endpoint just return null
     * note: use "##{page}##" in the endpoint instead of page number
     */
    fun fetchPopularEndpoint() : String?
    /**
     *return the end point for the fetch Search feature,
     * if there is not endpoint just return null
     * note: use "##{page}##" in the endpoint instead of page number
     */
    fun fetchSearchBookEndpoint() : String?
    /**
     *return the end point for the fetch Chapters books feature,
     * if there is not endpoint just return null
     * note: use "##{page}##" in the endpoint instead of page number
     */
    fun fetchChaptersEndpoint() : String?
    /**
     *return the end point for the fetch Content  books feature,
     * if there is not endpoint just return null
     * note: use "##{page}##" in the endpoint instead of page number
     */
    fun fetchContentEndpoint() : String?

    /**
     * Returns a page with a list of latest books updates and weather is has next page or not.
     *
     * @param page the page number to retrieve.
     */
    suspend fun fetchLatestUpdates(page: Int): BooksPage


    /**
     * Returns a page with a list of popular books updates and weather is has next page or not.
     *
     * @param page the page number to retrieve.
     */
    suspend fun fetchPopular(page: Int): BooksPage

    /**
     * Returns a page with a list of manga.
     *
     * @param page the page number to retrieve.
     * @param query the search query.
     */
    suspend fun fetchSearchBook(page: Int, query: String): BooksPage


    /**
     * Returns a page with a list of latest manga updates and weather is has next page or not.
     *
     * @param chapter the chapter that it contents is going to be retrieve.
     */
    suspend fun fetchContent(chapter: Chapter): ChapterPage


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


}

