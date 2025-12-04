package ireader.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * JavaScript implementation of coroutine utilities.
 */
actual fun createCoroutineScope(coroutineContext: CoroutineContext): CoroutineScope {
    return CoroutineScope(coroutineContext)
}

/**
 * Default dispatcher for JS - uses Dispatchers.Default
 */
actual val DefaultDispatcher: CoroutineDispatcher = Dispatchers.Default
