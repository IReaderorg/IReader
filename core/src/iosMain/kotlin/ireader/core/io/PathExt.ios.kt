package ireader.core.io

import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.dateWithTimeIntervalSince1970

@OptIn(ExperimentalForeignApi::class)
actual fun Path.setLastModified(epoch: Long) {
    val fileManager = NSFileManager.defaultManager
    val date = NSDate.dateWithTimeIntervalSince1970(epoch / 1000.0)
    val attributes = mapOf<Any?, Any?>(NSFileModificationDate to date)
    fileManager.setAttributes(attributes, ofItemAtPath = this.toString(), error = null)
}
