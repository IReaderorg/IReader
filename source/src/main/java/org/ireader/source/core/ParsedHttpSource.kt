package org.ireader.source.core

import org.ireader.source.models.ChapterInfo
import org.ireader.source.models.MangaInfo
import org.ireader.source.models.MangasPageInfo
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
    abstract fun popularFromElement(element: Element): MangaInfo

    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [latestSelector].
     */
    abstract fun latestFromElement(element: Element): MangaInfo

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
    abstract fun searchFromElement(element: Element): MangaInfo

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

    override fun popularParse(document: Document): MangasPageInfo {
        val books = document.select(popularSelector()).map { element ->
            popularFromElement(element)
        }

        val hasNextPage = popularNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return MangasPageInfo(books, hasNextPage)
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
    override fun latestParse(document: Document): MangasPageInfo {

        val books = document.select(latestSelector()).map { element ->
            latestFromElement(element)
        }

        val hasNextPage = latestNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return MangasPageInfo(books, hasNextPage)
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


    override fun searchParse(document: Document): MangasPageInfo {
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

        return MangasPageInfo(books,
            hasNextPage)
    }

    /****************************************************************************************************/


}