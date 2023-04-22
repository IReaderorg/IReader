package ireader.presentation.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
actual fun PlatformModalSheets(
    modifier: Modifier,
    state: Any,
    sheetContent: @Composable (modifier: Modifier) -> Unit,
    content: @Composable () -> Unit
) {
    val bottomSheetState = state as ModalBottomSheetState
    ModalBottomSheetLayout(
        sheetContent = {
            Box(modifier.defaultMinSize(minHeight = 1.dp)) {
                sheetContent(modifier.fillMaxWidth().fillMaxHeight(.9f))
            }
        },
        sheetState = bottomSheetState,
        sheetBackgroundColor = MaterialTheme.colorScheme.background,
        sheetContentColor = MaterialTheme.colorScheme.onBackground,
        content = content,

        )
}