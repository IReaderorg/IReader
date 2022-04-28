package org.ireader.core_api.util

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.channels.actor as jvmActor

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
val Dispatchers.IO
    get() = IO

fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T,
): T {
    return kotlinx.coroutines.runBlocking(context, block)
}
@kotlinx.coroutines.ObsoleteCoroutinesApi
class ActorScope<E>(private val actorScope: kotlinx.coroutines.channels.ActorScope<E>) {
    val channel
        get() = actorScope.channel
}
@kotlinx.coroutines.ObsoleteCoroutinesApi
fun <E> CoroutineScope.actor(
    context: CoroutineContext,
    capacity: Int,
    start: CoroutineStart,
    onCompletion: CompletionHandler?,
    block: suspend ActorScope<E>.() -> Unit,
): SendChannel<E> {
    return jvmActor(context, capacity, start, onCompletion) {
        block(ActorScope(this))
    }
}
