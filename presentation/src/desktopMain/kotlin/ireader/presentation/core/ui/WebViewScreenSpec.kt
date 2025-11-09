package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.os.openInBrowser
import ireader.presentation.core.VoyagerScreen

actual class WebViewScreenSpec actual constructor(
    private val url: String?,
    sourceId: Long?,
    bookId: Long?,
    chapterId: Long?,
    enableBookFetch: Boolean,
    enableChapterFetch: Boolean,
    enableChaptersFetch: Boolean
) : VoyagerScreen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val snackbarHostState = remember { SnackbarHostState() }
        
        LaunchedEffect(url) {
            if (url != null) {
                val result = openInBrowser(url)
                result.onFailure { error ->
                    snackbarHostState.showSnackbar(
                        message = error.message ?: "Failed to open browser"
                    )
                }
            }
            // Pop back after attempting to open browser
            navigator.pop()
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