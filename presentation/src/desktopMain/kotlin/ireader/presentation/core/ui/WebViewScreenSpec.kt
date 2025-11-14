package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.awt.Desktop
import java.net.URI

actual class WebViewScreenSpec actual constructor(
    internal val url: String?,
    internal val sourceId: Long?,
    internal val bookId: Long?,
    internal val chapterId: Long?,
    internal val enableBookFetch: Boolean,
    internal val enableChapterFetch: Boolean,
    internal val enableChaptersFetch: Boolean
) {
    @Composable
    actual fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val snackbarHostState = remember { SnackbarHostState() }
        
        LaunchedEffect(url) {
            if (url != null) {
                try {
                    val desktop = Desktop.getDesktop()
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(URI(url))
                    } else {
                        snackbarHostState.showSnackbar("Browser not supported on this system")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(
                        message = "Failed to open browser: ${e.message}"
                    )
                }
            }
            // Pop back after attempting to open browser
            navController.popBackStack()
        }
        
        // Show a brief loading screen while opening browser
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}