package org.ireader.image_loader.coil.image_loaders

import coil.network.HttpException
import io.ktor.client.engine.mergeHeaders
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.util.InternalAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.io.IOException
import kotlin.coroutines.resumeWithException

/**
 * Converts a ktor request to okhttp. Note that it does not support sending a request body. If we
 * ever need it we could use reflection to call this other method instead:
 * https://github.com/ktorio/ktor/blob/1.6.4/ktor-client/ktor-client-okhttp/jvm/src/io/ktor/client/engine/okhttp/OkHttpEngine.kt#L180
 */

@OptIn(InternalAPI::class)
fun HttpRequestData.convertToOkHttpRequest(): Request {
    val builder = Request.Builder()

    with(builder) {
        url(url.toString())
        mergeHeaders(headers, body) { key, value ->
            if (key == HttpHeaders.ContentLength) return@mergeHeaders
            addHeader(key, value)
        }

        method(method.value, null)
    }

    return builder.build()
}
suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        enqueue(
            object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        continuation.resumeWithException(HttpException(response))
                        return
                    }

                    continuation.resume(response) {
                        response.body?.closeQuietly()
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    // Don't bother with resuming the continuation if it is already cancelled.
                    if (continuation.isCancelled) return
                    continuation.resumeWithException(e)
                }
            },
        )

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
                // Ignore cancel exception
            }
        }
    }
}
