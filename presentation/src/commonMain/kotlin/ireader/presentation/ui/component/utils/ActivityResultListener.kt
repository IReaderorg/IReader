package ireader.presentation.ui.component.utils

import androidx.compose.runtime.Composable
import ireader.domain.models.common.Uri

@Composable
expect fun ActivityResultListener(
        onSuccess: suspend (Uri) -> Unit,
        onError: (Throwable) -> Unit,
) : ActivityResultLauncher

expect class ActivityResultLauncher(input:Any) {
    fun launch(intent:Any)
}