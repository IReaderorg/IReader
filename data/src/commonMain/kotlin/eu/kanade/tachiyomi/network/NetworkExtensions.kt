package eu.kanade.tachiyomi.network

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable

/**
 * Minimal OkHttp extensions shim for tsundoku extension compatibility.
 *
 * Provides the same function signatures as tsundoku's OkHttpExtensions.kt
 * but with simpler implementations that work with our Observable shim.
 */

/**
 * Extension function that converts an OkHttp Call to an Observable.
 * Simplified version that executes synchronously and wraps in Observable.
 */
fun Call.asObservable(): Observable<Response> {
    return Observable.fromCallable {
        execute()
    }
}

/**
 * Extension function that converts an OkHttp Call to an Observable
 * that only emits successful responses.
 */
fun Call.asObservableSuccess(): Observable<Response> {
    return asObservable().map { response ->
        if (!response.isSuccessful) {
            response.close()
            throw HttpException(response.code)
        }
        response
    }
}

/**
 * Create a cacheless call with progress tracking.
 */
fun OkHttpClient.newCachelessCallWithProgress(request: Request, listener: ProgressListener): Call {
    val progressClient = newBuilder()
        .cache(null)
        .addNetworkInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body, listener))
                .build()
        }
        .build()
    return progressClient.newCall(request)
}

/**
 * GET request builder.
 */
fun GET(
    url: String,
    headers: okhttp3.Headers = okhttp3.Headers.Builder().build(),
): Request {
    return Request.Builder()
        .url(url)
        .headers(headers)
        .get()
        .build()
}

/**
 * POST request builder.
 */
fun POST(
    url: String,
    headers: okhttp3.Headers = okhttp3.Headers.Builder().build(),
    body: okhttp3.RequestBody = okhttp3.RequestBody.create(null, ByteArray(0)),
): Request {
    return Request.Builder()
        .url(url)
        .headers(headers)
        .post(body)
        .build()
}

/**
 * PUT request builder.
 */
fun PUT(
    url: String,
    headers: okhttp3.Headers = okhttp3.Headers.Builder().build(),
    body: okhttp3.RequestBody = okhttp3.RequestBody.create(null, ByteArray(0)),
): Request {
    return Request.Builder()
        .url(url)
        .headers(headers)
        .put(body)
        .build()
}

/**
 * DELETE request builder.
 */
fun DELETE(
    url: String,
    headers: okhttp3.Headers = okhttp3.Headers.Builder().build(),
    body: okhttp3.RequestBody? = null,
): Request {
    return Request.Builder()
        .url(url)
        .headers(headers)
        .apply { if (body != null) delete(body) else delete() }
        .build()
}
