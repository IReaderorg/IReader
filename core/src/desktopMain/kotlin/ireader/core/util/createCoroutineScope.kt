package ireader.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

actual fun createCoroutineScope(coroutineContext: CoroutineContext): CoroutineScope = CoroutineScope(SupervisorJob() + DefaultDispatcher)
actual val DefaultDispatcher: CoroutineDispatcher = Dispatchers.IO