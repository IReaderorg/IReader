package ir.kazemcodes.infinity.data.network.models

import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter

interface Source {

    val id: Long

    val lang: String

    val name: String

    val baseUrl: String


    val supportsLatest: Boolean

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
    suspend fun fetchChapters(book: Book): List<Chapter>


}

