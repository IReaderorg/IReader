package ireader.domain.utils.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * iOS uses Default dispatcher for IO operations since Dispatchers.IO is not available
 */
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default

actual suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T) {
    withContext(Dispatchers.Main, block)
}
