package ireader.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

actual fun createCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob())
actual val DefaultDispatcher: CoroutineDispatcher = Dispatchers.IO