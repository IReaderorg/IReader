package ireader.core.http

import android.webkit.CookieManager
import ireader.core.prefs.PreferenceStore
import kotlinx.coroutines.runBlocking
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(preferencesStore: PreferenceStore) : CookieJar {

    val store = PersistentCookieStore()
    private val manager = CookieManager.getInstance()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        cookies.forEach { manager.setCookie(urlString, it.toString()) }
        val commonCookies = cookies.map { it.toCommonCookie() }
        runBlocking {
            store.addCookies(urlString, commonCookies)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return get(url)
    }

    fun get(url: HttpUrl): List<Cookie> {
        val cookies = manager.getCookie(url.toString())
        val storedCookies = runBlocking {
            store.getCookies(url.toString())
        }
        return if (storedCookies.isNotEmpty()) {
            storedCookies.mapNotNull { it.toOkHttpCookie(url) }
        } else if (cookies != null && cookies.isNotEmpty()) {
            cookies.split(";").mapNotNull { Cookie.parse(url, it) }
        } else {
            emptyList()
        }
    }

    fun remove(url: HttpUrl, cookieNames: List<String>? = null, maxAge: Int = -1): Int {
        val urlString = url.toString()
        val cookies = manager.getCookie(urlString) ?: return 0

        fun List<String>.filterNames(): List<String> {
            return if (cookieNames != null) {
                this.filter { it in cookieNames }
            } else {
                this
            }
        }

        return cookies.split(";")
            .map { it.substringBefore("=") }
            .filterNames()
            .onEach { manager.setCookie(urlString, "$it=;Max-Age=$maxAge") }
            .count()
    }

    private fun Cookie.toCommonCookie(): ireader.core.http.Cookie {
        return ireader.core.http.Cookie(
            name = name,
            value = value,
            domain = domain,
            path = path,
            expiresAt = expiresAt,
            secure = secure,
            httpOnly = httpOnly,
            persistent = persistent
        )
    }

    private fun ireader.core.http.Cookie.toOkHttpCookie(url: HttpUrl): Cookie? {
        return Cookie.Builder()
            .name(name)
            .value(value)
            .domain(domain)
            .path(path)
            .apply {
                if (expiresAt > 0) expiresAt(expiresAt)
                if (secure) secure()
                if (httpOnly) httpOnly()
            }
            .build()
    }
}
