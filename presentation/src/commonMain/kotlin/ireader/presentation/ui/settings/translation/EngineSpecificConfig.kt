package ireader.presentation.ui.settings.translation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.settings.general.TranslationSettingsViewModel

/**
 * Compact engine-specific configuration sections
 * Optimized for mobile screens
 */
@Composable
fun EngineSpecificConfig(
    engineId: Long,
    viewModel: TranslationSettingsViewModel,
    onNavigateToLogin: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (engineId) {
        5L -> OllamaConfig(
            ollamaUrl = viewModel.ollamaUrl.value,
            ollamaModel = viewModel.ollamaModel.value,
            onUrlChange = { viewModel.updateOllamaUrl(it) },
            onModelChange = { viewModel.updateOllamaModel(it) },
            modifier = modifier
        )
        4L -> LibreTranslateInfo(modifier = modifier)
        6L -> WebViewLoginConfig(
            engineName = "ChatGPT",
            onLoginClick = { onNavigateToLogin?.invoke("chatgpt") },
            modifier = modifier
        )
        7L -> WebViewLoginConfig(
            engineName = "DeepSeek",
            onLoginClick = { onNavigateToLogin?.invoke("deepseek") },
            modifier = modifier
        )
    }
}

@Composable
private fun OllamaConfig(
    ollamaUrl: String,
    ollamaModel: String,
    onUrlChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // URL Input
            OutlinedTextField(
                value = ollamaUrl,
                onValueChange = onUrlChange,
                label = { Text("Server URL", maxLines = 1) },
                placeholder = { Text("http://localhost:11434", maxLines = 1) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            // Model Input
            OutlinedTextField(
                value = ollamaModel,
                onValueChange = onModelChange,
                label = { Text("Model", maxLines = 1) },
                placeholder = { Text("mistral, llama2, gemma", maxLines = 1) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )

            // Info
            Text(
                text = "Install Ollama from ollama.ai",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LibreTranslateInfo(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "LibreTranslate - No API key required",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WebViewLoginConfig(
    engineName: String,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$engineName WebView",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "Sign in required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            
            FilledTonalButton(
                onClick = onLoginClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sign in", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
