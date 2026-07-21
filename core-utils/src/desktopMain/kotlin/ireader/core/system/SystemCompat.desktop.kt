package ireader.core.system

actual object SystemCompat {
    actual fun currentTimeMillis(): Long = java.lang.System.currentTimeMillis()
    actual fun gc() = java.lang.System.gc()
    actual fun getProperty(key: String): String? = java.lang.System.getProperty(key)
}

actual class RuntimeCompat {
    private val jvmRuntime = java.lang.Runtime.getRuntime()
    
    actual fun totalMemory(): Long = jvmRuntime.totalMemory()
    actual fun freeMemory(): Long = jvmRuntime.freeMemory()
    actual fun maxMemory(): Long = jvmRuntime.maxMemory()
    actual fun availableProcessors(): Int = jvmRuntime.availableProcessors()
    
    actual companion object {
        actual fun getRuntime(): RuntimeCompat = RuntimeCompat()
    }
}

actual object ThreadCompat {
    actual fun sleep(millis: Long) = java.lang.Thread.sleep(millis)
}
