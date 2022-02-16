package ir.kazemcodes.infinity.core.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okio.IOException
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun OkHttpClient.call(request: Request): Response =
    suspendCancellableCoroutine { continuation ->
        val call = newCall(request).apply {
            enqueue(object : Callback {

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        continuation.resumeWithException(Exception("HTTP error ${response.code()}"))
                        return
                    }
                    continuation.resume(response) {
                        response.body()?.close()
                    }

                }


                override fun onFailure(call: Call, e: IOException) {
                    // Don't bother with resuming the continuation if continuation is already cancelled.
                    if (continuation.isCancelled) return
                    continuation.resumeWithException(e)
                }
            })
    }

        continuation.invokeOnCancellation {
            try {
                call.cancel()
            } catch (ex: Throwable) {
                //Ignore cancel exception
            }
        }
}
