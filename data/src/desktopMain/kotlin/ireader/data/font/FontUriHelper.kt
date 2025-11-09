package ireader.data.font

import ireader.domain.models.common.Uri
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Desktop implementation - Uri is just a file path string
 */
actual fun copyFontFromUri(uri: Uri, destPath: Path) {
    val fileSystem = FileSystem.SYSTEM
    val sourcePath = uri.toString().toPath()
    fileSystem.copy(sourcePath, destPath)
}
