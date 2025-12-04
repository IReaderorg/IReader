package ireader.core.http

import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * JavaScript implementation of BrowserEngine.
 * 
 * Uses the browser's native fetch API for HTTP requests.
 * JavaScript rendering is inherently supported in the browser context.
 */
actual class BrowserEngine actual constructor() : BrowserEngineInterface {
    
    actual override suspend fun fetch(
        url: String,
        selector: String?,
        headers: Headers,
        timeout: Long,
        userAgent: String,
    ): BrowserResult {
        return try {
            val response = jsFetch(url, headers, userAgent, timeout).await()
            BrowserResult(
                responseBody = response.text,
                cookies = response.cookies.toList(),
                statusCode = response.status
            )
        } catch (e: Exception) {
            BrowserResult(
                responseBody = "",
                error = e.message ?: "Unknown error"
            )
        }
    }
    
    actual override fun isAvailable(): Boolean = true
    
    /**
     * Native JavaScript fetch wrapper.
     */
    private fun jsFetch(
        url: String,
        headers: Headers,
        userAgent: String,
        timeout: Long
    ): Promise<JsFetchResponse> {
        return js("""
            (function() {
                var controller = new AbortController();
                var timeoutId = setTimeout(function() { controller.abort(); }, timeout);
                
                var fetchHeaders = new Headers();
                for (var key in headers) {
                    if (headers.hasOwnProperty(key)) {
                        fetchHeaders.append(key, headers[key]);
                    }
                }
                if (userAgent) {
                    fetchHeaders.append('User-Agent', userAgent);
                }
                
                return fetch(url, {
                    method: 'GET',
                    headers: fetchHeaders,
                    signal: controller.signal,
                    credentials: 'include'
                }).then(function(response) {
                    clearTimeout(timeoutId);
                    return response.text().then(function(text) {
                        return {
                            text: text,
                            status: response.status,
                            cookies: []
                        };
                    });
                }).catch(function(error) {
                    clearTimeout(timeoutId);
                    throw error;
                });
            })()
        """) as Promise<JsFetchResponse>
    }
}

/**
 * Response from JavaScript fetch.
 */
external interface JsFetchResponse {
    val text: String
    val status: Int
    val cookies: Array<Cookie>
}
