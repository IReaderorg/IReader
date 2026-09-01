package eu.kanade.tachiyomi.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

val jsonMime = "application/json; charset=utf-8".toMediaType()

fun Call.asObservable(): Observable<Response> = Observable.fromCallable { execute() }

fun Call.asObservableSuccess(): Observable<Response> {
    return asObservable().map { response ->
        if (!response.isSuccessful) {
            response.close()
            throw HttpException(response.code)
        }
        response
    }
}

suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        try {
            val response = execute()
            continuation.resume(response) { _, value, _ -> value.close() }
        } catch (e: Exception) {
            if (continuation.isCancelled) return@suspendCancellableCoroutine
            continuation.resumeWithException(e)
        }
    }
}

suspend fun Call.awaitSuccess(): Response {
    val response = await()
    if (!response.isSuccessful) {
        response.close()
        throw HttpException(response.code)
    }
    return response
}

fun OkHttpClient.newCachelessCallWithProgress(
    request: Request,
    listener: ProgressListener,
    existingSize: Long = 0L,
): Call {
    val progressClient = newBuilder()
        .cache(null)
        .addNetworkInterceptor { chain ->
            val req = chain.request()
                .newBuilder()
                .apply {
                    if (existingSize > 0 && chain.request().header("Range") == null) {
                        header("Range", "bytes=$existingSize-")
                    }
                }
                .build()

            val originalResponse = chain.proceed(req)
            val actualExistingSize = if (originalResponse.code == 206) existingSize else 0L
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body, listener, actualExistingSize))
                .build()
        }
        .build()

    return progressClient.newCall(request)
}
