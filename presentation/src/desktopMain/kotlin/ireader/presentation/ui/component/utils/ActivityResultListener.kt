package ireader.presentation.ui.component.utils

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri



actual class ActivityResultLauncher actual constructor(input: Any) {
    actual fun launch(intent: Any) {

    }
}

@Composable
actual fun ActivityResultListener(onSuccess: suspend (Uri) -> Unit, onError: (Throwable) -> Unit): ActivityResultLauncher {
    return ActivityResultLauncher(0)}