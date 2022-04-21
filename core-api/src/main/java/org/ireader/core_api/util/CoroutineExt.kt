package org.ireader.core_api.util

/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */


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
