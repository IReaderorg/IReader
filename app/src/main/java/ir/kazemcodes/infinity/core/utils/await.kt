package ir.kazemcodes.infinity.core.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okio.IOException
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun OkHttpClient.call(request: Request): Response = suspendCancellableCoroutine {
    val call = newCall(request).apply {
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                it.resume(response) { e->
                    it.resumeWithException(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (it.isCancelled) return
                it.resumeWithException(e)
            }
        })
    }

    it.invokeOnCancellation {
        try {
            call.cancel()
        } catch (ex: Throwable) {
            //Ignore cancel exception
        }
    }
}
