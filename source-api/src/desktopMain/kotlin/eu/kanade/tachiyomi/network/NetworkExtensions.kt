package eu.kanade.tachiyomi.network

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer
import rx.Observable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun GET(url: String, headers: okhttp3.Headers = okhttp3.Headers.Builder().build()): Request =
    Request.Builder().url(url).headers(headers).get().build()

fun POST(url: String, headers: okhttp3.Headers = okhttp3.Headers.Builder().build(),
         body: okhttp3.RequestBody = okhttp3.RequestBody.create(null, ByteArray(0))): Request =
    Request.Builder().url(url).headers(headers).post(body).build()

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

fun OkHttpClient.newCachelessCallWithProgress(request: Request, listener: ProgressListener): Call {
    return newBuilder()
        .cache(null)
        .addNetworkInterceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            originalResponse.newBuilder()
                .body(ProgressResponseBody(originalResponse.body, listener))
                .build()
        }
        .build()
        .newCall(request)
}

interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}

class ProgressResponseBody(
    private val responseBody: okhttp3.ResponseBody,
    private val progressListener: ProgressListener,
) : okhttp3.ResponseBody() {
    private val bufferedSource: BufferedSource by lazy {
        object : ForwardingSource(responseBody.source()) {
            var totalBytesRead = 0L
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1L)
                return bytesRead
            }
        }.buffer()
    }
    override fun contentType() = responseBody.contentType()
    override fun contentLength() = responseBody.contentLength()
    override fun source() = bufferedSource
}

class HttpException(val code: Int) : IllegalStateException("HTTP error $code")
