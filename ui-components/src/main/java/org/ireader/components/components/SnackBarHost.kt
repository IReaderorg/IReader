package org.ireader.components.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ISnackBarHost(snackBarHostState: androidx.compose.material3.SnackbarHostState) {
    androidx.compose.material3.SnackbarHost(hostState = snackBarHostState) { data ->
        androidx.compose.material3.Snackbar(
            actionColor = MaterialTheme.colorScheme.primary,
            snackbarData = data,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    }
}
