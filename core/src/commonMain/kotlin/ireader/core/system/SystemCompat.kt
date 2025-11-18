package ireader.core.system

/**
 * Platform-specific system utilities
 * Provides cross-platform access to system-level functionality
 */
expect object SystemCompat {
    fun currentTimeMillis(): Long
    fun gc()
    fun getProperty(key: String): String?
}

expect class RuntimeCompat {
    fun totalMemory(): Long
    fun freeMemory(): Long
    fun maxMemory(): Long
    fun availableProcessors(): Int
    
    companion object {
        fun getRuntime(): RuntimeCompat
    }
}

expect object ThreadCompat {
    fun sleep(millis: Long)
}
