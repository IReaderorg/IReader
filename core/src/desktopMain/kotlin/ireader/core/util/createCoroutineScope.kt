package ireader.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

actual fun createCoroutineScope(coroutineContext: CoroutineContext): CoroutineScope = CoroutineScope(Job())
actual val DefaultDispatcher: CoroutineDispatcher = Dispatchers.Default