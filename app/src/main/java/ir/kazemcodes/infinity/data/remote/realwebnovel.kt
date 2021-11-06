package ir.kazemcodes.infinity.data.remote

import android.util.Log
import ir.kazemcodes.infinity.common.Constants.TAG
import ir.kazemcodes.infinity.data.remote.source.model.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class RealWebnovel : ParsedHttpSource() {
    override val baseUrl: String
        get() = "https://readwebnovels.net/"
    override val name: String
        get() = "RealWebNovel.net"


    override fun fetchBook(url: String): Book {
        TODO("Next Time")
    }

    override suspend fun fetchLatestBooksFromElement(page: Int): List<Book> {
        val books = mutableListOf<Book>()

        withContext(Dispatchers.IO) {

            val elements = Jsoup.connect("$baseUrl/page/$page/").get()

            Log.d(TAG, "fetchLatestBooksFromElement: $elements")

            val eTitle =
                elements.select("div.item-summary > div.post-title.font-title > h3 > a").eachText()
            val eImage = elements.select("div.site-content a img").eachAttr("src")
            val eLink =
                elements.select("div > div.item-summary > div.post-title.font-title > h3 > a")
                    .eachAttr("href")
            for (i in 0..11) {
                books.add(Book.create().apply {
                    title = eTitle[i].toString()
                    thumbnailUrl = eImage[i]
                    url = eLink[i]
                })
            }
        }
        return books
    }

    override fun fetchPopularBooksFromElement(page : Int): List<Book> {
        TODO("Not yet implemented")
    }

    override fun fetchBooksOrderByAlphabetFromElement(page : Int): List<Book> {
        TODO("Not yet implemented")
    }

    override fun searchBook() {
        TODO("Not yet implemented")
    }
}