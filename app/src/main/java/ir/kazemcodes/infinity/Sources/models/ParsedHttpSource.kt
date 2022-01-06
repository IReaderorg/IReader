package ir.kazemcodes.infinity.data.network.models

import android.content.Context
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.presentation.book_detail.Constants.CLOUDFLARE_LOG
import ir.kazemcodes.infinity.util.asJsoup
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class ParsedHttpSource(context: Context) : HttpSource(context) {


    /**
     * Returns a Book from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [popularBookSelector].
     */
    abstract fun popularBookFromElement(element: Element): Book


    override fun popularBookParse(document: Document,page: Int): BooksPage {
        val isCloudflareEnable = document.body().allElements.text().contains(CLOUDFLARE_LOG)
        val books = document.select(popularBookSelector()).map { element ->
            popularBookFromElement(element)
        }

        val hasNextPage = popularBookNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books, hasNextPage,isCloudflareEnable)
    }


    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each Book.
     */
    abstract fun latestUpdatesSelector(): String?

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

    override fun latestUpdatesParse(document: Document,page:Int): BooksPage {
        val isCloudflareEnable = document.body().allElements.text().contains(CLOUDFLARE_LOG)

        val books = document.select(latestUpdatesSelector()).map { element ->
            latestUpdatesFromElement(element)
        }

        val hasNextPage = latestUpdatesNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BooksPage(books, hasNextPage,isCloudflareEnable, document.body().allElements.text())
    }

    override fun latestUpdatesParse(response: Response,page: Int): BooksPage {
        return  latestUpdatesParse(response.asJsoup(), page = page)
    }

    /**
     * Returns the details of the Book from the given [document].
     *
     * @param document the parsed document.
     */
    abstract override fun bookDetailsParse(document: Document): BookPage


    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each chapter.
     */
    abstract fun chapterListSelector(): String?

    /**
     * Returns a chapter from the given element.
     *
     * @param element an element obtained from [chapterListSelector].
     */
    abstract fun chapterFromElement(element: Element): Chapter


    override fun chapterListParse(document: Document): ChaptersPage {
        val chapters = document.select(chapterListSelector()).map { chapterFromElement(it) }

        val hasNext = hasNextChaptersParse(document)


        return ChaptersPage(chapters, hasNext)
    }



    abstract override fun pageContentParse(document: Document): ChapterPage


    override fun searchBookParse(document: Document,page: Int): BooksPage {
        val isCloudflareEnable = document.body().allElements.text().contains(CLOUDFLARE_LOG)
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

        return BooksPage(books, hasNextPage,isCloudflareEnable)
    }

    open fun searchBookNextValuePageSelector(): String? = null
}