package ireader.domain.utils.extensions

import kotlinx.coroutines.*

/**
 * IO Dispatcher - uses Dispatchers.IO on JVM, Dispatchers.Default on iOS
 */
expect val ioDispatcher: CoroutineDispatcher

fun CoroutineScope.launchUI(block: suspend CoroutineScope.() -> Unit): Job =
    launch(Dispatchers.Main, block = block)

fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit): Job =
    launch(ioDispatcher, block = block)

expect suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T)

suspend fun <T> withIOContext(block: suspend CoroutineScope.() -> T) =
    withContext(ioDispatcher, block)
