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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.presentation.ui.settings.general.MlKitInitState
import ireader.presentation.ui.settings.general.TestConnectionState
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
    currentPluginEngine: PluginTranslateEngineWrapper? = null,
    translationEnginesManager: TranslationEnginesManager? = null,
    modifier: Modifier = Modifier
) {
    // Check if this is a plugin engine
    if (currentPluginEngine != null) {
        PluginEngineConfig(
            pluginEngine = currentPluginEngine,
            translationEnginesManager = translationEnginesManager,
            modifier = modifier
        )
        return
    }
    
    // Built-in engine configurations
    when (engineId) {
        0L -> GoogleMlKitConfig(
            initState = viewModel.mlKitInitState,
            initProgress = viewModel.mlKitInitProgress,
            onInitialize = { source, target -> viewModel.initializeGoogleMlKit(source, target) },
            onResetState = { viewModel.resetMlKitInitState() },
            modifier = modifier
        )
        5L -> OllamaConfig(
            ollamaUrl = viewModel.ollamaUrl.value,
            ollamaModel = viewModel.ollamaModel.value,
            onUrlChange = { viewModel.updateOllamaUrl(it) },
            onModelChange = { viewModel.updateOllamaModel(it) },
            onTestConnection = { viewModel.testConnection() },
            testState = viewModel.testConnectionState,
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

/**
 * Configuration UI for plugin-based translation engines
 */
@Composable
private fun PluginEngineConfig(
    pluginEngine: PluginTranslateEngineWrapper,
    translationEnginesManager: TranslationEnginesManager?,
    modifier: Modifier = Modifier
) {
    val pluginId = pluginEngine.pluginId
    
    // Determine plugin type and show appropriate config
    when {
        pluginId.contains("ollama") -> OllamaPluginConfig(
            pluginEngine = pluginEngine,
            modifier = modifier
        )
        pluginId.contains("libretranslate") -> LibreTranslatePluginInfo(modifier = modifier)
        pluginId.contains("huggingface") -> HuggingFacePluginConfig(
            pluginEngine = pluginEngine,
            modifier = modifier
        )
        pluginId.contains("openai") -> OpenAIPluginConfig(
            pluginEngine = pluginEngine,
            modifier = modifier
        )
        pluginId.contains("deepseek") -> DeepSeekPluginConfig(
            pluginEngine = pluginEngine,
            modifier = modifier
        )
        else -> GenericPluginConfig(
            pluginEngine = pluginEngine,
            modifier = modifier
        )
    }
}

/**
 * Ollama plugin configuration
 */
@Composable
private fun OllamaPluginConfig(
    pluginEngine: PluginTranslateEngineWrapper,
    modifier: Modifier = Modifier
) {
    val plugin = pluginEngine.getPlugin()
    
    // Load saved values from plugin
    var serverUrl by remember { mutableStateOf("http://localhost:11434") }
    var model by remember { mutableStateOf("mistral") }
    
    // Load saved values from plugin on first composition
    LaunchedEffect(pluginEngine) {
        // Get current values from plugin
        val savedUrl = plugin.getConfigValue("server_url") as? String
        val savedModel = plugin.getConfigValue("model") as? String
        
        if (!savedUrl.isNullOrBlank()) {
            serverUrl = savedUrl
        }
        if (!savedModel.isNullOrBlank()) {
            model = savedModel
        }
    }
    
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
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Ollama Configuration",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // URL Input
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { newUrl ->
                    serverUrl = newUrl
                    // Save to plugin config
                    plugin.onConfigChanged("server_url", newUrl)
                },
                label = { Text("Server URL", maxLines = 1) },
                placeholder = { Text("http://localhost:11434", maxLines = 1) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            // Model Input
            OutlinedTextField(
                value = model,
                onValueChange = { newModel ->
                    model = newModel
                    // Save to plugin config as custom model
                    plugin.onConfigChanged("custom_model", newModel)
                },
                label = { Text("Model", maxLines = 1) },
                placeholder = { Text("mistral, llama2, gemma", maxLines = 1) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )

            // Info
            Text(
                text = "Install Ollama from ollama.ai. Make sure the server is running.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * LibreTranslate plugin info
 */
@Composable
private fun LibreTranslatePluginInfo(
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
            Column {
                Text(
                    text = "LibreTranslate - Ready to use",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Free and open-source. No API key required.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * HuggingFace plugin configuration
 */
@Composable
private fun HuggingFacePluginConfig(
    pluginEngine: PluginTranslateEngineWrapper,
    modifier: Modifier = Modifier
) {
    val plugin = pluginEngine.getPlugin()
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    
    // Load saved values from plugin
    LaunchedEffect(pluginEngine) {
        val savedApiKey = plugin.getConfigValue("api_key") as? String
        if (!savedApiKey.isNullOrBlank()) {
            apiKey = savedApiKey
        }
    }
    
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
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "HuggingFace Configuration",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // API Key Input
            OutlinedTextField(
                value = apiKey,
                onValueChange = { newKey ->
                    apiKey = newKey
                    plugin.onConfigChanged("api_key", newKey)
                    plugin.configureApiKey(newKey)
                },
                label = { Text("API Key (optional)", maxLines = 1) },
                placeholder = { Text("hf_...", maxLines = 1) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "Hide" else "Show"
                        )
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium
            )

            // Info
            Text(
                text = "Get your API key from huggingface.co/settings/tokens",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * OpenAI plugin configuration
 */
@Composable
private fun OpenAIPluginConfig(
    pluginEngine: PluginTranslateEngineWrapper,
    modifier: Modifier = Modifier
) {
    val plugin = pluginEngine.getPlugin()
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    
    // Load saved values from plugin
    LaunchedEffect(pluginEngine) {
        val savedApiKey = plugin.getConfigValue("api_key") as? String
        if (!savedApiKey.isNullOrBlank()) {
            apiKey = savedApiKey
        }
    }
    
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
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "OpenAI Configuration",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // API Key Input
            OutlinedTextField(
                value = apiKey,
                onValueChange = { newKey ->
                    apiKey = newKey
                    plugin.onConfigChanged("api_key", newKey)
                    plugin.configureApiKey(newKey)
                },
                label = { Text("API Key", maxLines = 1) },
                placeholder = { Text("sk-...", maxLines = 1) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "Hide" else "Show"
                        )
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium
            )

            // Info
            Text(
                text = "Get your API key from platform.openai.com/api-keys",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * DeepSeek plugin configuration
 */
@Composable
private fun DeepSeekPluginConfig(
    pluginEngine: PluginTranslateEngineWrapper,
    modifier: Modifier = Modifier
) {
    val plugin = pluginEngine.getPlugin()
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    
    // Load saved values from plugin
    LaunchedEffect(pluginEngine) {
        val savedApiKey = plugin.getConfigValue("api_key") as? String
        if (!savedApiKey.isNullOrBlank()) {
            apiKey = savedApiKey
        }
    }
    
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
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "DeepSeek Configuration",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // API Key Input
            OutlinedTextField(
                value = apiKey,
                onValueChange = { newKey ->
                    apiKey = newKey
                    plugin.onConfigChanged("api_key", newKey)
                    plugin.configureApiKey(newKey)
                },
                label = { Text("API Key", maxLines = 1) },
                placeholder = { Text("sk-...", maxLines = 1) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "Hide" else "Show"
                        )
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium
            )

            // Info
            Text(
                text = "Get your API key from platform.deepseek.com",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Generic plugin configuration for unknown plugins
 */
@Composable
private fun GenericPluginConfig(
    pluginEngine: PluginTranslateEngineWrapper,
    modifier: Modifier = Modifier
) {
    val plugin = pluginEngine.getPlugin()
    val requiresApiKey = pluginEngine.requiresApiKey
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    
    // Load saved values from plugin
    LaunchedEffect(pluginEngine) {
        if (requiresApiKey) {
            val savedApiKey = plugin.getConfigValue("api_key") as? String
            if (!savedApiKey.isNullOrBlank()) {
                apiKey = savedApiKey
            }
        }
    }
    
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
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = pluginEngine.engineName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Description from manifest
            Text(
                text = pluginEngine.manifest.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // API Key Input if required
            if (requiresApiKey) {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { newKey ->
                        apiKey = newKey
                        plugin.onConfigChanged("api_key", newKey)
                        plugin.configureApiKey(newKey)
                    },
                    label = { Text("API Key", maxLines = 1) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showApiKey) "Hide" else "Show"
                            )
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "No API key required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun OllamaConfig(
    ollamaUrl: String,
    ollamaModel: String,
    onUrlChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onTestConnection: () -> Unit,
    testState: TestConnectionState,
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

            // Test Connection Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test Button
                FilledTonalButton(
                    onClick = onTestConnection,
                    enabled = ollamaUrl.isNotBlank() && ollamaModel.isNotBlank() && testState !is TestConnectionState.Testing,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (testState is TestConnectionState.Testing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Testing...", style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test", style = MaterialTheme.typography.labelMedium)
                    }
                }

                // Status indicator
                when (testState) {
                    is TestConnectionState.Success -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "OK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    is TestConnectionState.Error -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Failed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {}
                }
            }

            // Error message
            if (testState is TestConnectionState.Error) {
                Text(
                    text = testState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2
                )
            }

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


@Composable
private fun GoogleMlKitConfig(
    initState: MlKitInitState,
    initProgress: Int,
    onInitialize: (String, String) -> Unit,
    onResetState: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sourceLanguage by remember { mutableStateOf("en") }
    var targetLanguage by remember { mutableStateOf("es") }
    var expanded by remember { mutableStateOf(false) }
    
    // All supported languages for Google ML Kit
    val languages = listOf(
        "af" to "Afrikaans",
        "sq" to "Albanian",
        "ar" to "Arabic",
        "be" to "Belarusian",
        "bn" to "Bengali",
        "bg" to "Bulgarian",
        "ca" to "Catalan",
        "zh" to "Chinese",
        "hr" to "Croatian",
        "cs" to "Czech",
        "da" to "Danish",
        "nl" to "Dutch",
        "en" to "English",
        "eo" to "Esperanto",
        "et" to "Estonian",
        "fi" to "Finnish",
        "fr" to "French",
        "gl" to "Galician",
        "ka" to "Georgian",
        "de" to "German",
        "el" to "Greek",
        "gu" to "Gujarati",
        "ht" to "Haitian Creole",
        "he" to "Hebrew",
        "hi" to "Hindi",
        "hu" to "Hungarian",
        "is" to "Icelandic",
        "id" to "Indonesian",
        "ga" to "Irish",
        "it" to "Italian",
        "ja" to "Japanese",
        "kn" to "Kannada",
        "ko" to "Korean",
        "lv" to "Latvian",
        "lt" to "Lithuanian",
        "mk" to "Macedonian",
        "mr" to "Marathi",
        "ms" to "Malay",
        "mt" to "Maltese",
        "no" to "Norwegian",
        "fa" to "Persian",
        "pl" to "Polish",
        "pt" to "Portuguese",
        "ro" to "Romanian",
        "ru" to "Russian",
        "sr" to "Serbian",
        "sk" to "Slovak",
        "sl" to "Slovenian",
        "es" to "Spanish",
        "sw" to "Swahili",
        "sv" to "Swedish",
        "tl" to "Tagalog",
        "ta" to "Tamil",
        "te" to "Telugu",
        "th" to "Thai",
        "tr" to "Turkish",
        "uk" to "Ukrainian",
        "ur" to "Urdu",
        "vi" to "Vietnamese",
        "cy" to "Welsh"
    )
    
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
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Download Language Models",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Info text
            Text(
                text = "Google ML Kit requires downloading language models for offline translation. Select your language pair and initialize.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Language selection row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source language dropdown
                LanguageDropdown(
                    label = "From",
                    selectedCode = sourceLanguage,
                    languages = languages,
                    onLanguageSelected = { sourceLanguage = it },
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                
                // Target language dropdown
                LanguageDropdown(
                    label = "To",
                    selectedCode = targetLanguage,
                    languages = languages,
                    onLanguageSelected = { targetLanguage = it },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Initialize button and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { onInitialize(sourceLanguage, targetLanguage) },
                    enabled = initState !is MlKitInitState.Initializing && sourceLanguage != targetLanguage,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (initState is MlKitInitState.Initializing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Downloading...", style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Initialize", style = MaterialTheme.typography.labelMedium)
                    }
                }
                
                // Status indicator
                when (initState) {
                    is MlKitInitState.Success -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Ready",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    is MlKitInitState.Error -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Failed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {}
                }
            }
            
            // Progress bar during initialization
            if (initState is MlKitInitState.Initializing && initProgress > 0) {
                LinearProgressIndicator(
                    progress = { initProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            
            // Success/Error message
            when (initState) {
                is MlKitInitState.Success -> {
                    Text(
                        text = initState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2
                    )
                }
                is MlKitInitState.Error -> {
                    Text(
                        text = initState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 3
                    )
                }
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    label: String,
    selectedCode: String,
    languages: List<Pair<String, String>>,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = languages.find { it.first == selectedCode }?.second ?: selectedCode
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, maxLines = 1) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = { Text(name, style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        onLanguageSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}
