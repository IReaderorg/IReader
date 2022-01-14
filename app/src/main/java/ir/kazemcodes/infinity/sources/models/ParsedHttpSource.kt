package ir.kazemcodes.infinity.data.network.models

import android.content.Context
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.presentation.book_detail.Constants.CLOUDFLARE_LOG
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class ParsedHttpSource(context: Context) : HttpSource(context) {

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


    override fun popularParse(document: Document, page: Int,isWebViewMode : Boolean): BooksPage {
        val isCloudflareEnable = document.body().allElements.text().contains(CLOUDFLARE_LOG)
        val books = document.select(popularSelector).map { element ->
            popularFromElement(element)
        }

        val hasNextPage = popularBookNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books, hasNextPage, isCloudflareEnable)
    }


    override fun latestParse(document: Document, page: Int,isWebViewMode : Boolean): BooksPage {
        val isCloudflareEnable = document.body().allElements.text().contains(CLOUDFLARE_LOG)

        val books = document.select(latestSelector).map { element ->
            latestFromElement(element)
        }

        val hasNextPage = latestUpdatesNextPageSelector?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books, hasNextPage, isCloudflareEnable, document.body().allElements.text())
    }

    override fun chaptersParse(document: Document,isWebViewMode : Boolean ): ChaptersPage {
        val isCloudflareEnable = document.body().allElements.text().contains(CLOUDFLARE_LOG)
        val chapters = document.select(chaptersSelector).map { chapterFromElement(it) }


        val hasNext = hasNextChaptersParse(document)


        return ChaptersPage(chapters, hasNext, isCloudflareEnabled = isCloudflareEnable)
    }


    abstract override fun contentParse(document: Document,isWebViewMode : Boolean ): ChapterPage


    override fun searchParse(document: Document, page: Int,isWebViewMode : Boolean): BooksPage {
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
        val hasNextPage = false

        return BooksPage(books, hasNextPage, isCloudflareEnable)
    }

    /****************************************************************************************************/


}