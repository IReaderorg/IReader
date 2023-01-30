package ireader.domain.utils

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.Calendar

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
