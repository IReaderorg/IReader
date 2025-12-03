package ireader.domain.utils.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Desktop uses Dispatchers.IO for IO operations
 */
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

actual suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T) {
    withContext(Dispatchers.Main, block)
}
