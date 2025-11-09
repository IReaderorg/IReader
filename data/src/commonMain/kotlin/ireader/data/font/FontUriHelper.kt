package ireader.data.font

import ireader.domain.models.common.Uri
import okio.Path

/**
 * Platform-specific helper for copying font files from Uri
 */
expect fun copyFontFromUri(uri: Uri, destPath: Path)
