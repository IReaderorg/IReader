package ireader.core.http

import ireader.core.prefs.PreferenceStore
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(preferencesStore: PreferenceStore) : CookieJar {

    val store = PersistentCookieStore(preferencesStore)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        store.addAll(url, cookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val data = store.get(url)
        return data
    }

}
