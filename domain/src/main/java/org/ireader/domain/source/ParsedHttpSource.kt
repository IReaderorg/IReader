package org.ireader.domain.source

import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.source.BooksPage
import org.ireader.domain.models.source.HttpSource
import org.ireader.source.models.BookInfo
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/** Taken from https://tachiyomi.org/ **/
abstract class ParsedHttpSource(dependencies: Dependencies) : HttpSource(dependencies) {

    /****************************************************************************************************/
    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [popularSelector].
     */
    abstract fun popularFromElement(element: Element): BookInfo

    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [latestSelector].
     */
    abstract fun latestFromElement(element: Element): BookInfo

    /**
     * Returns a chapter from the given element.
     *
     * @param element an element obtained from [chaptersSelector].
     */
    abstract fun chapterFromElement(element: Element): Chapter

    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [searchSelector].
     */
    abstract fun searchFromElement(element: Element): BookInfo

    /****************************************************************************************************/
    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract fun popularSelector(): String

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    protected abstract fun popularNextPageSelector(): String?

    override fun popularParse(document: Document): BooksPage {
        val books = document.select(popularSelector()).map { element ->
            popularFromElement(element)
        }

        val hasNextPage = popularNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books, hasNextPage)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract fun latestSelector(): String

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    protected abstract fun latestNextPageSelector(): String?
    override fun latestParse(document: Document): BooksPage {

        val books = document.select(latestSelector()).map { element ->
            latestFromElement(element)
        }

        val hasNextPage = latestNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books, hasNextPage)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract fun chaptersSelector(): String

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    protected abstract fun hasNextChapterSelector(): String

    override fun chaptersParse(document: Document): List<Chapter> {
        val chapters = document.select(chaptersSelector()).map { chapterFromElement(it) }

        val hasNext = hasNextChaptersParse(document)

        return chapters


    }

    abstract fun hasNextChaptersParse(document: Document): Boolean

    abstract override fun pageContentParse(
        document: Document,
    ): List<String>

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract fun searchSelector(): String

    protected abstract fun searchNextPageSelector(): String


    override fun searchParse(document: Document): BooksPage {
        /**
         * I Add Filter Because sometimes this value contains null values
         * so the null book shows in search screen
         */
        val books = document.select(searchSelector()).map { element ->
            searchFromElement(element)
        }.filter {
            it.title.isNotBlank()
        }
        val hasNextPage = searchNextPageSelector().let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books,
            hasNextPage)
    }

    /****************************************************************************************************/


}