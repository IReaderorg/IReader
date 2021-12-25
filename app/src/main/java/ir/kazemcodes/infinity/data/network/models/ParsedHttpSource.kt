package ir.kazemcodes.infinity.data.network.models

import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.utils.asJsoup
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class ParsedHttpSource : HttpSource() {

    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [popularBookSelector].
     */
    abstract fun popularBookFromElement(element: Element): Book


    override fun popularBookParse(response: Response): BooksPage {
        val document = response.asJsoup()

        val books = document.select(popularBookSelector()).map { element ->
            popularBookFromElement(element)
        }

        val hasNextPage = popularBookNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books, hasNextPage)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each Book.
     */
    abstract fun latestUpdatesSelector(): String

    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [latestUpdatesSelector].
     */
    abstract fun latestUpdatesFromElement(element: Element): Book

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    abstract fun latestUpdatesNextPageSelector(): String?

    override fun latestUpdatesParse(response: Response): BooksPage {
        val document = response.asJsoup()

        val books = document.select(latestUpdatesSelector()).map { element ->
            latestUpdatesFromElement(element)
        }

        val hasNextPage = latestUpdatesNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books, hasNextPage)
    }

    /**
     * Returns the details of the Book from the given [document].
     *
     * @param document the parsed document.
     */
    abstract fun bookDetailsParse(document: Document): Book

    override fun bookDetailsParse(response: Response): Book {
        return bookDetailsParse(response.asJsoup())
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each chapter.
     */
    abstract fun chapterListSelector(): String

    /**
     * Returns a chapter from the given element.
     *
     * @param element an element obtained from [chapterListSelector].
     */
    abstract fun chapterFromElement(element: Element): Chapter

    abstract fun chapterListNextPageSelector(): String?

    override fun chapterListParse(response: Response): ChaptersPage {
        val document = response.asJsoup()
        val chapters = document.select(chapterListSelector()).map { chapterFromElement(it) }
        val hasNext = hasNextChaptersParse(document)

        return ChaptersPage(chapters, hasNext)
    }


    override fun pageContentParse(response: Response): ChapterPage {
        return pageContentParse(response.asJsoup())
    }

    abstract fun pageContentParse(document: Document): ChapterPage


    override fun searchBookParse(response: Response): BooksPage {
        val document = response.asJsoup()

        /**
         * I Add Filter Because sometimes this value contains null values
         * so the null book shows in search screen
         */
        val books = document.select(searchBookSelector()).map { element ->
            searchBookFromElement(element)
        }.filter {
            it.bookName.isNotBlank()
        }


        val hasNextPage = false

        return BooksPage(books, hasNextPage)
    }
}