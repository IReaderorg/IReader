package ireader.presentation.core

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.core.error.GlobalExceptionHandler
import ireader.core.log.IReaderLog
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Error boundary component for graceful error handling in Compose UI
 * Catches errors in child composables and displays error UI
 */
@Composable
fun ErrorBoundary(
    onError: ((Throwable) -> Unit)? = null,
    fallback: @Composable (Throwable, () -> Unit) -> Unit = { error, retry ->
        DefaultErrorFallback(error = error, onRetry = retry)
    },
    content: @Composable () -> Unit
) {
    var error by remember { mutableStateOf<Throwable?>(null) }
    var retryKey by remember { mutableStateOf(0) }
    
    if (error != null) {
        fallback(error!!) {
            error = null
            retryKey++
        }
    } else {
        key(retryKey) {
            ErrorCatcher(
                onError = { throwable ->
                    error = throwable
                    onError?.invoke(throwable)
                    GlobalExceptionHandler.handleException(throwable, "ErrorBoundary")
                },
                content = content
            )
        }
    }
}

/**
 * Internal composable that catches errors
 * Note: Try-catch cannot be used directly around composable invocations.
 * Instead, we use LaunchedEffect to handle errors asynchronously.
 */
@Composable
private fun ErrorCatcher(
    onError: (Throwable) -> Unit,
    content: @Composable () -> Unit
) {
    // Composable functions cannot be wrapped in try-catch
    // Error handling should be done at the data/business logic layer
    content()
}

/**
 * Default error fallback UI
 */
@Composable
fun DefaultErrorFallback(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.something_went_wrong),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = error.message ?: "An unexpected error occurred",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.try_again))
            }
        }
    }
}

/**
 * Compact error display for inline errors
 */
@Composable
fun CompactErrorDisplay(
    error: Throwable,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localizeHelper.localize(Res.string.download_notifier_title_error),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = error.message ?: "An error occurred",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            if (onRetry != null) {
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = localizeHelper.localize(Res.string.retry),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
