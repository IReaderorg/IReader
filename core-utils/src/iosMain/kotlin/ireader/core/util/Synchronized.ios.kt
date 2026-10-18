package ireader.core.util

import platform.objc.objc_sync_enter
import platform.objc.objc_sync_exit

actual inline fun <R> synchronized(lock: Any, block: () -> R): R {
    objc_sync_enter(lock)
    try {
        return block()
    } finally {
        objc_sync_exit(lock)
    }
}
