package org.ireader.common_extensions

import kotlinx.coroutines.*


fun CoroutineScope.launchUI(block: suspend CoroutineScope.() -> Unit): Job =
    launch(Dispatchers.Main, block = block)

fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit): Job =
    launch(Dispatchers.IO, block = block)

suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.Main, block)

suspend fun <T> withIOContext(block: suspend CoroutineScope.() -> T) =
    withContext(Dispatchers.IO, block)
