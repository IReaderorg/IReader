package org.ireader.core_api.http.main

import okhttp3.Headers
import org.ireader.core_api.http.impl.Result

interface BrowseEngine {
    suspend fun fetch(
        url: String,
        selector: String? = null,
        headers: Headers? = null,
        timeout: Long = 50000L,
        userAgent: String = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36",
    ): Result
}