package ir.kazemcodes.infinity.core.presentation.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable

@Composable
fun ISnackBarHost(snackBarHostState: SnackbarHostState) {
    SnackbarHost(hostState = snackBarHostState) { data ->
        Snackbar(
            actionColor = MaterialTheme.colors.primary,
            snackbarData = data,
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.onBackground,
        )
    }

}