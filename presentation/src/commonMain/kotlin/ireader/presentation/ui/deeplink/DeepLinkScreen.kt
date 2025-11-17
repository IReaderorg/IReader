package ireader.presentation.ui.deeplink

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.core.deeplink.DeepLink
import ireader.core.deeplink.DeepLinkType

/**
 * Screen for handling deep links and external URLs
 * Shows loading state while processing the deep link
 */
@Composable
fun DeepLinkScreen(
    deepLink: DeepLink,
    isProcessing: Boolean,
    error: String? = null,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                error != null -> {
                    DeepLinkError(
                        error = error,
                        onRetry = onRetry,
                        onCancel = onCancel
                    )
                }
                isProcessing -> {
                    DeepLinkProcessing(deepLink = deepLink)
                }
            }
        }
    }
}

@Composable
private fun DeepLinkProcessing(
    deepLink: DeepLink,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CircularProgressIndicator()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = getProcessingMessage(deepLink.type),
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = deepLink.url,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeepLinkError(
    error: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Unable to Open Link",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
            
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

private fun getProcessingMessage(type: DeepLinkType): String {
    return when (type) {
        DeepLinkType.BOOK -> "Opening book..."
        DeepLinkType.CHAPTER -> "Opening chapter..."
        DeepLinkType.SOURCE -> "Opening source..."
        DeepLinkType.BROWSE -> "Opening browse..."
        DeepLinkType.LIBRARY -> "Opening library..."
        DeepLinkType.SETTINGS -> "Opening settings..."
        DeepLinkType.EXTERNAL_URL -> "Processing link..."
        DeepLinkType.CONTENT_URI -> "Processing content..."
    }
}
