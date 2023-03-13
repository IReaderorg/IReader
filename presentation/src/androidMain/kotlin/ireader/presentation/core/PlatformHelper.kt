package ireader.presentation.core

import android.content.Context
import ireader.domain.utils.copyToClipboard

actual class PlatformHelper(
        private val context: Context
) {
    actual fun copyToClipboard(label: String, content: String) {
        context.copyToClipboard(label, content)
    }
}