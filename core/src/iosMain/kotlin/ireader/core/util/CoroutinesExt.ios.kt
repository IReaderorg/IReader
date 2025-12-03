package ireader.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
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
    block: suspend ActorScope<E>.() -> Unit
): Channel<E> {
    val channel = Channel<E>(capacity)
    kotlinx.coroutines.launch(context) {
        ActorScope(channel).block()
    }
    return channel
}
