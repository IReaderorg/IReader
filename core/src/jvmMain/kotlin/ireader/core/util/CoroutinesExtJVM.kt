/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.util

import ireader.core.util.ActorScope
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.channels.actor as jvmActor

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual val Dispatchers.IO
  get() = IO

actual fun <T> runBlocking(
  context: CoroutineContext,
  block: suspend CoroutineScope.() -> T
): T {
  return kotlinx.coroutines.runBlocking(context, block)
}

actual class ActorScope<E>(private val actorScope: kotlinx.coroutines.channels.ActorScope<E>) {
  actual val channel
    get() = actorScope.channel
}

actual fun <E> CoroutineScope.actor(
  context: CoroutineContext,
  capacity: Int,
  start: CoroutineStart,
  onCompletion: CompletionHandler?,
  block: suspend ActorScope<E>.() -> Unit
): SendChannel<E> {
  return jvmActor(context, capacity, start, onCompletion) {
    block(ActorScope(this))
  }
}
