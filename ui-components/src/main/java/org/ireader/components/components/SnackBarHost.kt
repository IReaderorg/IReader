package org.ireader.components.components

import androidx.compose.runtime.Composable

@Composable
fun ISnackBarHost(snackBarHostState: androidx.compose.material3.SnackbarHostState) {
    androidx.compose.material3.SnackbarHost(hostState = snackBarHostState) { data ->
        androidx.compose.material3.Snackbar(
            snackbarData = data,
        )
    }
}
