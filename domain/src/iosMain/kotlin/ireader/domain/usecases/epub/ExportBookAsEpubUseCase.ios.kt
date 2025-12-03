package ireader.domain.usecases.epub

import ireader.domain.models.common.Uri
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

actual fun createPlatformTempEpubPath(): String {
    val tempDir = NSTemporaryDirectory()
    val uuid = NSUUID().UUIDString
    return "$tempDir$uuid.epub"
}

actual suspend fun copyPlatformTempFileToContentUri(tempPath: String, contentUri: Uri) {
    // TODO: Implement file copy using NSFileManager
}
