package ireader.core.system

import kotlin.time.ExperimentalTime

actual object SystemCompat {
    @OptIn(ExperimentalTime::class)
    actual fun currentTimeMillis(): Long {
        return kotlin.time.Clock.System.now().toEpochMilliseconds()
    }
    
    actual fun gc() {
        // No-op on iOS - ARC handles memory management
    }
    
    actual fun getProperty(key: String): String? {
        // System properties are JVM-specific, return null on iOS
        return null
    }
}

actual class RuntimeCompat {
    actual fun totalMemory(): Long {
        // TODO: Implement using mach_task_basic_info
        return 0L
    }
    
    actual fun freeMemory(): Long {
        // TODO: Implement using mach_task_basic_info
        return 0L
    }
    
    actual fun maxMemory(): Long {
        // TODO: Implement using NSProcessInfo.processInfo.physicalMemory
        return Long.MAX_VALUE
    }
    
    actual fun availableProcessors(): Int {
        // TODO: Implement using NSProcessInfo.processInfo.processorCount
        return 1
    }
    
    actual companion object {
        actual fun getRuntime(): RuntimeCompat = RuntimeCompat()
    }
}

actual object ThreadCompat {
    actual fun sleep(millis: Long) {
        platform.Foundation.NSThread.sleepForTimeInterval(millis / 1000.0)
    }
}
