package ireader.core.http

import okhttp3.Headers

actual class BrowserEngine() : BrowserEngineInterface {
    actual override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers?,
        timeout: Long,
        userAgent: String
    ): Result {
       return ireader.core.http.Result("")
    }
}