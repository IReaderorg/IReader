package ireader.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

expect fun createCoroutineScope() : CoroutineScope
expect val DefaultDispatcher : CoroutineDispatcher
fun createICoroutineScope(dispatcher: CoroutineContext = SupervisorJob() + DefaultDispatcher) : CoroutineScope = createCoroutineScope()