package ireader.core.io

import okio.Path
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate

actual fun Path.setLastModified(epoch: Long) {
    val fileManager = NSFileManager.defaultManager
    val date = NSDate(timeIntervalSince1970 = epoch / 1000.0)
    val attributes = mapOf<Any?, Any?>(NSFileModificationDate to date)
    fileManager.setAttributes(attributes, ofItemAtPath = this.toString(), error = null)
}
