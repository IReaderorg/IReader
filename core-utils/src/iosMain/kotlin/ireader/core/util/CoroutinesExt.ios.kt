package ireader.core.util

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

actual val Dispatchers.IO: CoroutineDispatcher
    get() = kotlinx.coroutines.Dispatchers.IO

actual fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T = kotlinx.coroutines.runBlocking(context, block)

actual class ActorScope<E>(actual val channel: Channel<E>)

actual fun <E> CoroutineScope.actor(
    context: CoroutineContext,
    capacity: Int,
    start: CoroutineStart,
    onCompletion: CompletionHandler?,
    block: suspend ActorScope<E>.() -> Unit
): SendChannel<E> {
    val channel = Channel<E>(capacity)
    val job = launch(context, start) {
        try {
            ActorScope(channel).block()
        } finally {
            channel.close()
        }
    }
    onCompletion?.let { handler ->
        job.invokeOnCompletion(handler)
    }
    return channel
}
