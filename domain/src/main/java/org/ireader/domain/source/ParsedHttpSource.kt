package org.ireader.domain.source

import org.ireader.core.utils.Constants.CLOUDFLARE_LOG
import org.ireader.core.utils.Constants.CLOUDFLARE_PROTECTION_ERROR
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.source.BooksPage
import org.ireader.domain.models.source.ChapterPage
import org.ireader.domain.models.source.ChaptersPage
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class ParsedHttpSource : HttpSource() {

    /****************************************************************************************************/
    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [popularSelector].
     */
    abstract fun popularFromElement(element: Element): Book

    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [latestSelector].
     */
    abstract fun latestFromElement(element: Element): Book

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
    abstract fun searchFromElement(element: Element): Book

    /****************************************************************************************************/
    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract val popularSelector: String

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    protected abstract val popularBookNextPageSelector: String?

    override fun popularParse(document: Document, page: Int, isWebViewMode: Boolean): BooksPage {
        val books = document.select(popularSelector).map { element ->
            popularFromElement(element)
        }

        val hasNextPage = popularBookNextPageSelector?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books, hasNextPage)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract val latestSelector: String

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    protected abstract val latestUpdatesNextPageSelector: String?
    override fun latestParse(document: Document, page: Int, isWebViewMode: Boolean): BooksPage {

        val books = document.select(latestSelector).map { element ->
            latestFromElement(element)
        }

        val hasNextPage = latestUpdatesNextPageSelector?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books, hasNextPage, document.body().allElements.text())
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract val chaptersSelector: String

    /**
     * Returns the Jsoup selector that returns the <a> tag linking to the next page, or null if
     * there's no next page.
     */
    protected abstract val hasNextChapterSelector: String

    override fun chaptersParse(document: Document, isWebViewMode: Boolean): ChaptersPage {

        val isCloudflareEnable = document.body().allElements.text().contains(CLOUDFLARE_LOG)

        val chapters = document.select(chaptersSelector).map { chapterFromElement(it) }

        val hasNext = hasNextChaptersParse(document)

        return ChaptersPage(chapters,
            hasNext,
            errorMessage = if (isCloudflareEnable) CLOUDFLARE_PROTECTION_ERROR else "")
    }

    abstract fun hasNextChaptersParse(document: Document, isWebViewMode: Boolean = false): Boolean

    abstract override fun contentFromElementParse(
        document: Document,
        isWebViewMode: Boolean,
    ): ChapterPage

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    protected abstract val searchSelector: String

    protected abstract val searchBookNextPageSelector: String


    override fun searchParse(document: Document, page: Int, isWebViewMode: Boolean): BooksPage {

        val isCloudflareEnable = document.body().allElements.text().contains(CLOUDFLARE_LOG)

        /**
         * I Add Filter Because sometimes this value contains null values
         * so the null book shows in search screen
         */
        val books = document.select(searchSelector).map { element ->
            searchFromElement(element)
        }.filter {
            it.bookName.isNotBlank()
        }
        val hasNextPage = searchBookNextPageSelector.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books,
            hasNextPage,
            errorMessage = if (isCloudflareEnable) CLOUDFLARE_PROTECTION_ERROR else "")
    }

    /****************************************************************************************************/


}