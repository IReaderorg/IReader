package org.ireader.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.ireader.core_api.http.AcceptAllCookiesStorage
import org.ireader.core_api.http.BrowseEngine
import org.ireader.core_api.http.HttpClients
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

@RunWith(AndroidJUnit4::class)
class ExtensionTests {

    lateinit var source: Source

    @Before
    fun prepare() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cookieJar = WebViewCookieJar(AcceptAllCookiesStorage())
        val httpClients = HttpClients(context, BrowseEngine(WebViewManger(context), cookieJar),AcceptAllCookiesStorage(),cookieJar)
        val androidCatalogLoader =  AndroidCatalogLoader(context,httpClients)
        source =  androidCatalogLoader.loadSystemCatalog("ireader.skynovel.en")?.source!!
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
            val book = source.getMangaDetails(MangaInfo(key = "https://skynovel.org/manga/the-genius-mage-novel/", title = "The Genius Mage Novel"), emptyList())
            Log.error { "TEST $book" }
            assertThat(true).isTrue()
        }
    }
    @Test
    fun getChapterInfo() {
        runBlocking {
            val chapters = source.getChapterList(MangaInfo(key = "https://skynovel.org/manga/the-genius-mage-novel/", title = "The Genius Mage Novel"), emptyList())
            Log.error { "TEST $chapters" }
            assertThat(chapters.isNotEmpty()).isTrue()
        }
    }


    @Test
    fun getContent() {
        runBlocking {
            val page = source.getPageList(ChapterInfo(key = "https://skynovel.org/manga/the-genius-mage-novel/chapter-442/", name = ""), emptyList())
            Log.error { "TEST $page" }
            assertThat(page.isNotEmpty()).isTrue()
        }
    }
}