package ireader.data.font

import ireader.domain.models.common.Uri
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * iOS implementation - Uri is a file path string
 * 
 * TODO: Handle iOS-specific URI schemes if needed
 */
actual fun copyFontFromUri(uri: Uri, destPath: Path) {
    val fileSystem = FileSystem.SYSTEM
    val sourcePath = uri.toString().toPath()
    fileSystem.copy(sourcePath, destPath)
}
