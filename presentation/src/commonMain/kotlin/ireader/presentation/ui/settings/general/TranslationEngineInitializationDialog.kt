package ireader.presentation.ui.settings.general

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * State for translation engine initialization
 */
@Stable
data class TranslationEngineInitState(
    val isInitializing: Boolean = false,
    val progress: Int = 0,
    val isComplete: Boolean = false,
    val error: String? = null,
    val engineName: String = "",
    val sourceLanguage: String = "",
    val targetLanguage: String = ""
)

/**
 * Dialog to prompt user to initialize translation engine
 * Similar to RequiredPluginScreen but as a dialog for better UX
 */
@Composable
fun TranslationEngineInitializationDialog(
    state: TranslationEngineInitState,
    onInitialize: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    AlertDialog(
        onDismissRequest = { if (!state.isInitializing) onDismiss() },
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = localizeHelper.localize(Res.string.translation_engine_initialization),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when {
                    state.isComplete -> {
                        // Success state
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.initialization_complete),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.translation_engine_ready),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    state.error != null -> {
                        // Error state
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.initialization_failed),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    state.isInitializing -> {
                        // Initializing state
                        CircularProgressIndicator(
                            progress = { state.progress / 100f },
                            modifier = Modifier.size(64.dp),
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.downloading_language_model),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${state.progress}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${state.sourceLanguage} → ${state.targetLanguage}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    else -> {
                        // Initial prompt state
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.engine_requires_initialization),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = localizeHelper.localize(
                                Res.string.engine_initialization_description,
                                state.engineName
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        // Language info card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = localizeHelper.localize(Res.string.language_pair),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${state.sourceLanguage} → ${state.targetLanguage}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when {
                state.isComplete -> {
                    Button(onClick = onDismiss) {
                        Text(localizeHelper.localize(Res.string.done))
                    }
                }
                state.error != null -> {
                    Button(onClick = onRetry) {
                        Text(localizeHelper.localize(Res.string.retry))
                    }
                }
                state.isInitializing -> {
                    // No button while initializing
                }
                else -> {
                    Button(onClick = onInitialize) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizeHelper.localize(Res.string.initialize))
                    }
                }
            }
        },
        dismissButton = {
            if (!state.isInitializing && !state.isComplete) {
                TextButton(onClick = onDismiss) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        }
    )
}
