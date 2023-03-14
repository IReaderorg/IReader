package ireader.domain.utils.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T) {
    withContext(Dispatchers.Main, block)
}
