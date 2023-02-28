package ireader.presentation.ui.component.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable

@Composable
fun ISnackBarHost(snackBarHostState: androidx.compose.material3.SnackbarHostState)  : SnackbarHostState {
    androidx.compose.material3.SnackbarHost(hostState = snackBarHostState) { data ->
        androidx.compose.material3.Snackbar(
            snackbarData = data,
        )
    }
    return  snackBarHostState
}
