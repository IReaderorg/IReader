package ireader.core.system

import kotlinx.datetime.Clock

actual object SystemCompat {
    actual fun currentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
    
    actual fun gc() {
        // No-op on iOS - ARC handles memory management
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
}

actual object ThreadCompat {
    actual fun sleep(millis: Long) {
        platform.Foundation.NSThread.sleepForTimeInterval(millis / 1000.0)
    }
}
