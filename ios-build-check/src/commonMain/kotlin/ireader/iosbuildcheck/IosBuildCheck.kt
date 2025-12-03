package ireader.iosbuildcheck

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.ExperimentalTime

/**
 * Simple class to verify iOS compilation works.
 * This uses common KMP APIs that should work on all platforms.
 */
class IosBuildCheck {
    
    @OptIn(ExperimentalTime::class)
    fun getMessage(): String {
        val timestamp = kotlin.time.Clock.System.now()
        return "iOS Build Check - Timestamp: $timestamp"
    }
    
    fun getFlow(): Flow<String> {
        return flowOf("iOS", "Build", "Check", "OK")
    }
}

/**
 * Platform-specific greeting
 */
expect fun getPlatformName(): String

/**
 * Combined greeting using expect/actual pattern
 */
fun greet(): String {
    return "Hello from ${getPlatformName()}!"
}
