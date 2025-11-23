package ireader.presentation.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun PlatformModalSheets(
    modifier: Modifier,
    state: Any,
    backgroundColor: Color,
    contentColor: Color,
    sheetContent: @Composable (modifier: Modifier) -> Unit,
    content: @Composable () -> Unit
) {
    val sheetState = state as SheetState
    val scope = rememberCoroutineScope()
    
    Box {
        content()
        
        if (sheetState.isVisible) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch {
                        sheetState.hide()
                    }
                },
                sheetState = sheetState,
                containerColor = backgroundColor,
                contentColor = contentColor
            ) {
                Box(modifier.defaultMinSize(minHeight = 1.dp)) {
                    sheetContent(modifier.fillMaxWidth().fillMaxHeight(.9f))
                }
            }
        }
    }
}