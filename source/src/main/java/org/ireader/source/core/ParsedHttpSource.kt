package org.ireader.source.core

import org.ireader.source.models.BookInfo
import org.ireader.source.models.BookPageInfo
import org.ireader.source.models.ChapterInfo
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
    abstract fun chapterFromElement(element: Element): ChapterInfo

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

    override fun popularParse(document: Document): BookPageInfo {
        val books = document.select(popularSelector()).map { element ->
            popularFromElement(element)
        }

        val hasNextPage = popularNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BookPageInfo(books, hasNextPage)
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
    override fun latestParse(document: Document): BookPageInfo {

        val books = document.select(latestSelector()).map { element ->
            latestFromElement(element)
        }

        val hasNextPage = latestNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BookPageInfo(books, hasNextPage)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract fun chaptersSelector(): String


    override fun chaptersParse(document: Document): List<ChapterInfo> {
        return document.select(chaptersSelector()).map { chapterFromElement(it) }
    }


    abstract override fun pageContentParse(
        document: Document,
    ): List<String>

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract fun searchSelector(): String

    protected abstract fun searchNextPageSelector(): String?


    override fun searchParse(document: Document): BookPageInfo {
        /**
         * I Add Filter Because sometimes this value contains null values
         * so the null book shows in search screen
         */
        val books = document.select(searchSelector()).map { element ->
            searchFromElement(element)
        }.filter {
            it.title.isNotBlank()
        }
        val hasNextPage = searchNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BookPageInfo(books,
            hasNextPage)
    }

    /****************************************************************************************************/
    /****************************************************************************************************/

    /**
     *return the end point for the fetch latest books updates feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     */
    abstract fun fetchLatestEndpoint(page: Int): String?

    /**
     *return the end point for the  fetch Popular books feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     */
    abstract fun fetchPopularEndpoint(page: Int): String?

    /**
     *return the end point for the fetch Search feature,
     * if there is not endpoint just return null
     * note: use "{page}" in the endpoint instead of page number
     * note: use "{query}" in the endpoint instead of query
     */
    abstract fun fetchSearchEndpoint(page: Int, query: String): String?


    /****************************************************************************************************/

}