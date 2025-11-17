package ireader.presentation.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * IReader error screen component following Mihon's pattern.
 * Displays user-friendly error messages with random kaomoji faces and action buttons.
 */
@Composable
fun IReaderErrorScreen(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val face = remember { getRandomErrorFace() }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Random kaomoji face
        Text(
            text = face,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        // Error message
        Text(
            text = message,
            modifier = Modifier.padding(top = 24.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        // Action buttons
        if (onRetry != null || onDismiss != null) {
            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                onDismiss?.let {
                    OutlinedButton(onClick = it) {
                        Text("Dismiss")
                    }
                }
                
                onRetry?.let {
                    Button(onClick = it) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

/**
 * Get a random error face following Mihon's pattern
 */
private fun getRandomErrorFace(): String {
    val errorFaces = listOf(
        "(･o･;)", "Σ(ಠ_ಠ)", "ಥ_ಥ", "(˘･_･˘)", "(；￣Д￣)",
        "(･Д･。", "(╬ಠ益ಠ)", "(╥﹏╥)", "(⋟﹏⋞)", "Ò︵Ó", 
        "˙ᯅ˙)", "(¬_¬)", "(>_<)", "(×_×)", "(-_-;)",
        "(T_T)", "(>.<)", "orz", "(´･ω･`)", "(｡•́︿•̀｡)"
    )
    return errorFaces.random()
}