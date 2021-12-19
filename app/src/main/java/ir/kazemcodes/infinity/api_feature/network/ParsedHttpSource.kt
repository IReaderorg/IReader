package ir.kazemcodes.infinity.api_feature.network

import ir.kazemcodes.infinity.api_feature.HttpSource
import ir.kazemcodes.infinity.api_feature.data.BookPage
import ir.kazemcodes.infinity.api_feature.data.ChapterPage
import ir.kazemcodes.infinity.base_feature.util.asJsoup
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class ParsedHttpSource : HttpSource() {

    /**
     * Returns a manga from the given [element]. Most sites only show the title and the url, it's
     * totally fine to fill only those two values.
     *
     * @param element an element obtained from [popularMangaSelector].
     */
    abstract fun popularBookFromElement(element: Element): Book


    override fun popularBookParse(response: Response): BookPage {
        val document = response.asJsoup()

        val books = document.select(popularMangaSelector()).map { element ->
            popularBookFromElement(element)
        }

        val hasNextPage = popularBookNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BookPage(books, hasNextPage)
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each manga.
     */
    abstract fun latestUpdatesSelector(): String

    /**
     * Returns a manga from the given [element]. Most sites only show the title and the url, it's
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

    override fun latestUpdatesParse(response: Response): BookPage {
        val document = response.asJsoup()

        val books = document.select(latestUpdatesSelector()).map { element ->
            latestUpdatesFromElement(element)
        }

        val hasNextPage = latestUpdatesNextPageSelector()?.let { selector ->
            document.select(selector).first()
        } != null

        return BookPage(books, hasNextPage)
    }

    /**
     * Returns the details of the manga from the given [document].
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

    override fun chapterListParse(response: Response): ChapterPage {
        val document = response.asJsoup()
        val chapters =  document.select(chapterListSelector()).map { chapterFromElement(it) }
        val hasNext = hasNextChaptersParse(document)

        return ChapterPage(chapters, hasNext)
    }





    override fun pageContentParse(response: Response): String {
        return pageContentParse(response.asJsoup())
    }

    abstract fun pageContentParse(document: Document): String

    override fun searchMangaSelector(): String {
        TODO("Not yet implemented")
    }

    override fun searchMangaFromElement(element: Element): Book {
        TODO("Not yet implemented")
    }

    override fun searchMangaNextPageSelector(): String? {
        TODO("Not yet implemented")
    }
}