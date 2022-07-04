package org.ireader.core_api.http

import android.webkit.CookieManager
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Url
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebViewCookieJar @Inject constructor(private val cookiesStorage: CookiesStorage) : CookieJar {

    private val manager: CookieManager = CookieManager.getInstance()

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return get(url)
    }


    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {

        cookies.forEach { manager.setCookie(url.toString(), it.toString()) }
        scope.launch {
            cookies.forEach {  cookiesStorage.addCookie(Url(url.toString()),it.toCookies()) }
        }
    }

    fun get(url: HttpUrl): List<Cookie> {
        val cookies = manager.getCookie(url.toString())

        return if (cookies != null && cookies.isNotEmpty()) {
            cookies.split(";").mapNotNull { Cookie.parse(url, it) }
        } else {
            emptyList()
        }
    }

    fun remove(url: HttpUrl, cookieNames: List<String>? = null, maxAge: Int = -1) {
        val urlString = url.toString()
        val cookies = manager.getCookie(urlString) ?: return

        fun Sequence<String>.filterNames(): Sequence<String> {
            return if (cookieNames != null) {
                this.filter { it in cookieNames }
            } else {
                this
            }
        }

        cookies.splitToSequence(";")
            .map { it.substringBefore("=") }
            .filterNames()
            .onEach { manager.setCookie(urlString, "$it=;Max-Age=$maxAge") }
    }

    fun removeAll() {
        manager.removeAllCookies {}
    }
}

class AndroidCookieJar(private val cookiesStorage: CookiesStorage) : CookieJar {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val manager = CookieManager.getInstance()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        cookies.forEach { manager.setCookie(urlString, it.toString()) }
        scope.launch {
            cookies.forEach {  cookiesStorage.addCookie(Url(urlString),it.toCookies()) }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return get(url)
    }

    fun get(url: HttpUrl): List<Cookie> {
        val cookies = manager.getCookie(url.toString())

        return if (cookies != null && cookies.isNotEmpty()) {
            cookies.split(";").mapNotNull { Cookie.parse(url, it) }
        } else {
            emptyList()
        }
    }

    fun remove(url: HttpUrl, cookieNames: List<String>? = null, maxAge: Int = -1) {
        val urlString = url.toString()
        val cookies = manager.getCookie(urlString) ?: return
        fun List<String>.filterNames(): List<String> {
            return if (cookieNames != null) {
                this.filter { it in cookieNames }
            } else {
                this
            }
        }

        cookies.split(";")
            .map { it.substringBefore("=") }
            .filterNames()
            .onEach { manager.setCookie(urlString, "$it=;Max-Age=$maxAge") }
    }

    fun removeAll() {
        manager.removeAllCookies {}
    }
}

class MemoryCookieJar : CookieJar {
    private val cache = mutableSetOf<WrappedCookie>()

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookiesToRemove = mutableSetOf<WrappedCookie>()
        val validCookies = mutableSetOf<WrappedCookie>()

        cache.forEach { cookie ->
            if (cookie.isExpired()) {
                cookiesToRemove.add(cookie)
            } else if (cookie.matches(url)) {
                validCookies.add(cookie)
            }
        }

        cache.removeAll(cookiesToRemove)

        return validCookies.toList().map(WrappedCookie::unwrap)
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookiesToAdd = cookies.map { WrappedCookie.wrap(it) }

        cache.removeAll(cookiesToAdd)
        cache.addAll(cookiesToAdd)
    }

    @Synchronized
    fun clear() {
        cache.clear()
    }
}

class WrappedCookie private constructor(val cookie: Cookie) {
    fun unwrap() = cookie

    fun isExpired() = cookie.expiresAt < Calendar.getInstance().timeInMillis

    fun matches(url: HttpUrl) = cookie.matches(url)

    override fun equals(other: Any?): Boolean {
        if (other !is WrappedCookie) return false

        return other.cookie.name == cookie.name &&
            other.cookie.domain == cookie.domain &&
            other.cookie.path == cookie.path &&
            other.cookie.secure == cookie.secure &&
            other.cookie.hostOnly == cookie.hostOnly
    }

    override fun hashCode(): Int {
        var hash = 17
        hash = 31 * hash + cookie.name.hashCode()
        hash = 31 * hash + cookie.domain.hashCode()
        hash = 31 * hash + cookie.path.hashCode()
        hash = 31 * hash + if (cookie.secure) 0 else 1
        hash = 31 * hash + if (cookie.hostOnly) 0 else 1
        return hash
    }

    companion object {
        fun wrap(cookie: Cookie) = WrappedCookie(cookie)
    }
}


fun Cookie.toCookies() : io.ktor.http.Cookie {
    return io.ktor.http.Cookie(
        this.name,
        this.value,
        httpOnly = this.httpOnly,
        domain = this.domain,
        expires = GMTDate(this.expiresAt),
        path = this.path,
        secure = this.secure

    )
}