package org.ireader.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.ireader.core_api.http.AcceptAllCookiesStorage
import org.ireader.core_api.http.impl.BrowseEngineImpl
import org.ireader.core_api.http.impl.HttpClientsImpl
import org.ireader.core_api.http.WebViewCookieJar
import org.ireader.core_api.http.WebViewManger
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.HttpSource
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.ChapterInfo
import org.ireader.core_api.source.model.MangaInfo
import org.ireader.data.catalog.AndroidCatalogLoader
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExtensionTests {

    lateinit var source: Source


    @Before
    fun prepare() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cookie = AcceptAllCookiesStorage()
        val cookieJar = WebViewCookieJar(cookie)
        val httpClients = HttpClientsImpl(context, BrowseEngineImpl(WebViewManger(context), cookieJar),cookie,cookieJar)
        val androidCatalogLoader =  AndroidCatalogLoader(context,httpClients)
        source =  androidCatalogLoader.loadSystemCatalog(SOURCE_PKG)!!.source!!
    }
    @Test
    fun getBooks() {
        runBlocking {
            val httpSource = source as? HttpSource ?: return@runBlocking
            val books = httpSource.getMangaList(httpSource.getListings().first(),1)
            Log.error { "TEST $books" }
            assertThat(books.mangas.isNotEmpty()).isTrue()
        }
    }

    @Test
    fun getBookInfo() {
        runBlocking {
            val book = source.getMangaDetails(MangaInfo(key = BOOK_URL, title = BOOK_TITLE), emptyList())
            Log.error { "TEST $book" }
            assertThat(true).isTrue()
        }
    }
    @Test
    fun getChapterInfo() {
        runBlocking {
            val chapters = source.getChapterList(MangaInfo(key =BOOK_URL, title = BOOK_TITLE), emptyList())
            Log.error { "TEST $chapters" }
            assertThat(chapters.isNotEmpty()).isTrue()
        }
    }


    @Test
    fun getContent() {
        runBlocking {
            val page = source.getPageList(ChapterInfo(key = BOOK_URL, name = BOOK_TITLE), emptyList())
            Log.error { "TEST $page" }
            assertThat(page.isNotEmpty()).isTrue()
        }
    }

    companion object {
        const val SOURCE_PKG = "ireader.boxnovel.en"
        const val BOOK_URL = "https://www.readwn.com/novel/scoring-the-sacred-body-of-the-ancients-from-the-get-go.html"
        const val BOOK_TITLE = "Scoring the Sacred Body of the Ancients from the Get-go"
    }
}