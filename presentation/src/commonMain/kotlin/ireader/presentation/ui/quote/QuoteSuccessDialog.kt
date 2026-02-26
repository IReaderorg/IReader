package ireader.presentation.ui.quote

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Success dialog shown after saving or sharing a quote.
 * Auto-dismisses after 3 seconds.
 */
@Composable
fun QuoteSuccessDialog(
    message: String,
    onViewMyQuotes: () -> Unit,
    onCreateAnother: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }
    
    // Success animation
    val scale by rememberInfiniteTransition(label = "success").animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(64.dp)
                    .scale(scale)
            )
        },
        title = {
            Text(
                text = "Success!",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onViewMyQuotes) {
                Text("View in My Quotes")
            }
        },
        dismissButton = {
            TextButton(onClick = onCreateAnother) {
                Text("Create Another")
            }
        },
        modifier = modifier
    )
}
