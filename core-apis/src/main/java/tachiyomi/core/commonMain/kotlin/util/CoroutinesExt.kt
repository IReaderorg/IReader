/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.util

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect val Dispatchers.IO: CoroutineDispatcher

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun <T> runBlocking(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T,
): T

/**
 * TODO(inorichi): this is temporary until actors are implemented in multiplatform:
 *   https://github.com/Kotlin/kotlinx.coroutines/issues/1851
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect class ActorScope<E> {
    val channel: Channel<E>
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect fun <E> CoroutineScope.actor(
    context: CoroutineContext,
    capacity: Int = 0,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onCompletion: CompletionHandler? = null,
    block: suspend ActorScope<E>.() -> Unit,
): SendChannel<E>
