package ireader.presentation.ui.settings.translation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import ireader.domain.plugins.PluginPreferences
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.i18n.resources.Res
import ireader.i18n.resources.deepseek_configuration
import ireader.i18n.resources.download_language_models
import ireader.i18n.resources.enter_gemini_api_key
import ireader.i18n.resources.failed
import ireader.i18n.resources.free_and_open_source_no_api_key_required
import ireader.i18n.resources.from
import ireader.i18n.resources.gemini_api_key
import ireader.i18n.resources.gemini_model
import ireader.i18n.resources.get_your_api_key_from_huggingfacecosettingstokens
import ireader.i18n.resources.get_your_api_key_from_platformdeepseekcom
import ireader.i18n.resources.get_your_api_key_from_platformopenaicomapi_keys
import ireader.i18n.resources.google_ml_kit_requires_downloading
import ireader.i18n.resources.huggingface_configuration
import ireader.i18n.resources.install_ollama_from_ollamaai
import ireader.i18n.resources.install_ollama_from_ollamaai_make
import ireader.i18n.resources.libretranslate_no_api_key_required
import ireader.i18n.resources.libretranslate_ready_to_use
import ireader.i18n.resources.no_api_key_required
import ireader.i18n.resources.nvidia_api_key
import ireader.i18n.resources.nvidia_fetch_models
import ireader.i18n.resources.nvidia_info
import ireader.i18n.resources.nvidia_model
import ireader.i18n.resources.nvidia_nim
import ireader.i18n.resources.ok
import ireader.i18n.resources.ollama_configuration
import ireader.i18n.resources.openai_configuration
import ireader.i18n.resources.openrouter_api_key
import ireader.i18n.resources.openrouter_fetch_models
import ireader.i18n.resources.openrouter_info
import ireader.i18n.resources.openrouter_model
import ireader.i18n.resources.ready
import ireader.i18n.resources.refresh_models
import ireader.i18n.resources.sign_in_required
import ireader.i18n.resources.to
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.general.MlKitInitState
import ireader.presentation.ui.settings.general.TestConnectionState
import ireader.presentation.ui.settings.general.TranslationSettingsViewModel
import org.koin.compose.koinInject

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
        8L -> GeminiConfig(
            apiKey = viewModel.geminiApiKey.value,
            selectedModel = viewModel.geminiModel.value,
            onApiKeyChange = { viewModel.updateGeminiApiKey(it) },
            onModelChange = { viewModel.updateGeminiModel(it) },
            onLoadModels = { viewModel.loadGeminiModels() },
            availableModels = viewModel.geminiModels,
            isLoadingModels = viewModel.isLoadingGeminiModels,
            modifier = modifier
        )
        9L -> OpenRouterConfig(
            apiKey = viewModel.openRouterApiKey.value,
            selectedModel = viewModel.openRouterModel.value,
            onApiKeyChange = { viewModel.updateOpenRouterApiKey(it) },
            onModelChange = { viewModel.updateOpenRouterModel(it) },
            onLoadModels = { viewModel.loadOpenRouterModels() },
            availableModels = viewModel.openRouterModels,
            isLoadingModels = viewModel.isLoadingOpenRouterModels,
            modifier = modifier
        )
        10L -> NvidiaConfig(
            apiKey = viewModel.nvidiaApiKey.value,
            selectedModel = viewModel.nvidiaModel.value,
            onApiKeyChange = { viewModel.updateNvidiaApiKey(it) },
            onModelChange = { viewModel.updateNvidiaModel(it) },
            onLoadModels = { viewModel.loadNvidiaModels() },
            availableModels = viewModel.nvidiaModels,
            isLoadingModels = viewModel.isLoadingNvidiaModels,
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
    // Use dynamic config renderer for all plugins
    DynamicPluginConfigRenderer(
        pluginEngine = pluginEngine,
        modifier = modifier
    )
}

/**
 * Ollama plugin configuration
 */
@Composable
private fun OllamaPluginConfig(
    pluginEngine: PluginTranslateEngineWrapper,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                    text = localizeHelper.localize(Res.string.ollama_configuration),
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
                text = localizeHelper.localize(Res.string.install_ollama_from_ollamaai_make),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                    text = localizeHelper.localize(Res.string.libretranslate_ready_to_use),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = localizeHelper.localize(Res.string.free_and_open_source_no_api_key_required),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val pluginPreferences: PluginPreferences = koinInject()
    val plugin = pluginEngine.getPlugin()
    val pluginId = pluginEngine.pluginId
    
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    
    // Load saved API key from PluginPreferences (persistent storage)
    LaunchedEffect(pluginId) {
        val savedApiKey = pluginPreferences.getPluginApiKey(pluginId).get()
        if (!savedApiKey.isNullOrBlank()) {
            apiKey = savedApiKey
            // Also configure the plugin with the saved key
            plugin.configureApiKey(savedApiKey)
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
                    text = localizeHelper.localize(Res.string.huggingface_configuration),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (saveSuccess) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Saved",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // API Key Input
            OutlinedTextField(
                value = apiKey,
                onValueChange = { newKey ->
                    apiKey = newKey
                },
                label = { Text("API Key", maxLines = 1) },
                placeholder = { Text("hf_...", maxLines = 1) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Row {
                        if (apiKey.isNotBlank()) {
                            IconButton(onClick = {
                                // Save to persistent storage
                                pluginPreferences.setPluginApiKey(pluginId, apiKey)
                                // Configure the plugin
                                plugin.configureApiKey(apiKey)
                                plugin.onConfigChanged("api_key", apiKey)
                                saveSuccess = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save"
                                )
                            }
                        }
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showApiKey) "Hide" else "Show"
                            )
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            // Save button for clarity
            if (apiKey.isNotBlank()) {
                Button(
                    onClick = {
                        pluginPreferences.setPluginApiKey(pluginId, apiKey)
                        plugin.configureApiKey(apiKey)
                        plugin.onConfigChanged("api_key", apiKey)
                        saveSuccess = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save API Key")
                }
            }

            // Info
            Text(
                text = localizeHelper.localize(Res.string.get_your_api_key_from_huggingfacecosettingstokens),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val pluginPreferences: PluginPreferences = koinInject()
    val plugin = pluginEngine.getPlugin()
    val pluginId = pluginEngine.pluginId
    
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    
    // Load saved API key from PluginPreferences (persistent storage)
    LaunchedEffect(pluginId) {
        val savedApiKey = pluginPreferences.getPluginApiKey(pluginId).get()
        if (!savedApiKey.isNullOrBlank()) {
            apiKey = savedApiKey
            plugin.configureApiKey(savedApiKey)
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
                    text = localizeHelper.localize(Res.string.openai_configuration),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (saveSuccess) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Saved",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // API Key Input
            OutlinedTextField(
                value = apiKey,
                onValueChange = { newKey ->
                    apiKey = newKey
                },
                label = { Text("API Key", maxLines = 1) },
                placeholder = { Text("sk-...", maxLines = 1) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Row {
                        if (apiKey.isNotBlank()) {
                            IconButton(onClick = {
                                pluginPreferences.setPluginApiKey(pluginId, apiKey)
                                plugin.configureApiKey(apiKey)
                                plugin.onConfigChanged("api_key", apiKey)
                                saveSuccess = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save"
                                )
                            }
                        }
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showApiKey) "Hide" else "Show"
                            )
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            // Save button
            if (apiKey.isNotBlank()) {
                Button(
                    onClick = {
                        pluginPreferences.setPluginApiKey(pluginId, apiKey)
                        plugin.configureApiKey(apiKey)
                        plugin.onConfigChanged("api_key", apiKey)
                        saveSuccess = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save API Key")
                }
            }

            // Info
            Text(
                text = localizeHelper.localize(Res.string.get_your_api_key_from_platformopenaicomapi_keys),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val pluginPreferences: PluginPreferences = koinInject()
    val plugin = pluginEngine.getPlugin()
    val pluginId = pluginEngine.pluginId
    
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    
    // Load saved API key from PluginPreferences (persistent storage)
    LaunchedEffect(pluginId) {
        val savedApiKey = pluginPreferences.getPluginApiKey(pluginId).get()
        if (!savedApiKey.isNullOrBlank()) {
            apiKey = savedApiKey
            plugin.configureApiKey(savedApiKey)
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
                    text = localizeHelper.localize(Res.string.deepseek_configuration),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (saveSuccess) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Saved",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // API Key Input
            OutlinedTextField(
                value = apiKey,
                onValueChange = { newKey ->
                    apiKey = newKey
                },
                label = { Text("API Key", maxLines = 1) },
                placeholder = { Text("sk-...", maxLines = 1) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Row {
                        if (apiKey.isNotBlank()) {
                            IconButton(onClick = {
                                pluginPreferences.setPluginApiKey(pluginId, apiKey)
                                plugin.configureApiKey(apiKey)
                                plugin.onConfigChanged("api_key", apiKey)
                                saveSuccess = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save"
                                )
                            }
                        }
                        IconButton(onClick = { showApiKey = !showApiKey }) {
                            Icon(
                                imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showApiKey) "Hide" else "Show"
                            )
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            // Save button
            if (apiKey.isNotBlank()) {
                Button(
                    onClick = {
                        pluginPreferences.setPluginApiKey(pluginId, apiKey)
                        plugin.configureApiKey(apiKey)
                        plugin.onConfigChanged("api_key", apiKey)
                        saveSuccess = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save API Key")
                }
            }

            // Info
            Text(
                text = localizeHelper.localize(Res.string.get_your_api_key_from_platformdeepseekcom),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val pluginPreferences: PluginPreferences = koinInject()
    val plugin = pluginEngine.getPlugin()
    val pluginId = pluginEngine.pluginId
    val requiresApiKey = pluginEngine.requiresApiKey
    
    var apiKey by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    
    // Load saved API key from PluginPreferences (persistent storage)
    LaunchedEffect(pluginId) {
        if (requiresApiKey) {
            val savedApiKey = pluginPreferences.getPluginApiKey(pluginId).get()
            if (!savedApiKey.isNullOrBlank()) {
                apiKey = savedApiKey
                plugin.configureApiKey(savedApiKey)
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
                if (saveSuccess) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Saved",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
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
                    },
                    label = { Text("API Key", maxLines = 1) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row {
                            if (apiKey.isNotBlank()) {
                                IconButton(onClick = {
                                    pluginPreferences.setPluginApiKey(pluginId, apiKey)
                                    plugin.configureApiKey(apiKey)
                                    plugin.onConfigChanged("api_key", apiKey)
                                    saveSuccess = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = "Save"
                                    )
                                }
                            }
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showApiKey) "Hide" else "Show"
                                )
                            }
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                
                // Save button
                if (apiKey.isNotBlank()) {
                    Button(
                        onClick = {
                            pluginPreferences.setPluginApiKey(pluginId, apiKey)
                            plugin.configureApiKey(apiKey)
                            plugin.onConfigChanged("api_key", apiKey)
                            saveSuccess = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save API Key")
                    }
                }
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
                        text = localizeHelper.localize(Res.string.no_api_key_required),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                                text = localizeHelper.localize(Res.string.ok),
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
                                text = localizeHelper.localize(Res.string.failed),
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
                text = localizeHelper.localize(Res.string.install_ollama_from_ollamaai),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                text = localizeHelper.localize(Res.string.libretranslate_no_api_key_required),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                    text = localizeHelper.localize(Res.string.sign_in_required),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                    text = localizeHelper.localize(Res.string.download_language_models),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Info text
            Text(
                text = localizeHelper.localize(Res.string.google_ml_kit_requires_downloading),
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
                    label = localizeHelper.localize(Res.string.from),
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
                    label = localizeHelper.localize(Res.string.to),
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
                                text = localizeHelper.localize(Res.string.ready),
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
                                text = localizeHelper.localize(Res.string.failed),
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
    val selectedName = remember(languages, selectedCode) { 
        languages.find { it.first == selectedCode }?.second ?: selectedCode 
    }
    
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

/**
 * Gemini API configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeminiConfig(
    apiKey: String,
    selectedModel: String,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onLoadModels: () -> Unit,
    availableModels: List<Pair<String, String>>,
    isLoadingModels: Boolean,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showApiKey by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    
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
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Google Gemini",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // API Key Input
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = { Text(localizeHelper.localize(Res.string.gemini_api_key), maxLines = 1) },
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
            
            // Model Selection
            ExposedDropdownMenuBox(
                expanded = modelExpanded,
                onExpandedChange = { modelExpanded = it }
            ) {
                OutlinedTextField(
                    value = availableModels.find { it.first == selectedModel }?.second ?: selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(localizeHelper.localize(Res.string.gemini_model), maxLines = 1) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                
                ExposedDropdownMenu(
                    expanded = modelExpanded,
                    onDismissRequest = { modelExpanded = false }
                ) {
                    availableModels.forEach { (modelId, modelName) ->
                        DropdownMenuItem(
                            text = { Text(modelName, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                onModelChange(modelId)
                                modelExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Load Models Button
            FilledTonalButton(
                onClick = onLoadModels,
                enabled = apiKey.isNotBlank() && !isLoadingModels,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoadingModels) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading models...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.refresh_models))
                }
            }
            
            // Info
            Text(
                text = localizeHelper.localize(Res.string.enter_gemini_api_key),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * OpenRouter API configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpenRouterConfig(
    apiKey: String,
    selectedModel: String,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onLoadModels: () -> Unit,
    availableModels: List<Pair<String, String>>,
    isLoadingModels: Boolean,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showApiKey by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    
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
                    imageVector = Icons.Default.Hub,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "OpenRouter AI",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // API Key Input
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = { Text(localizeHelper.localize(Res.string.openrouter_api_key), maxLines = 1) },
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
            
            // Model Selection
            ExposedDropdownMenuBox(
                expanded = modelExpanded,
                onExpandedChange = { modelExpanded = it }
            ) {
                OutlinedTextField(
                    value = availableModels.find { it.first == selectedModel }?.second ?: selectedModel.ifEmpty { "Auto (Best Value)" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(localizeHelper.localize(Res.string.openrouter_model), maxLines = 1) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                
                ExposedDropdownMenu(
                    expanded = modelExpanded,
                    onDismissRequest = { modelExpanded = false }
                ) {
                    availableModels.forEach { (modelId, modelName) ->
                        DropdownMenuItem(
                            text = { Text(modelName, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                onModelChange(modelId)
                                modelExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Load Models Button
            FilledTonalButton(
                onClick = onLoadModels,
                enabled = apiKey.isNotBlank() && !isLoadingModels,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoadingModels) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading models...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.openrouter_fetch_models))
                }
            }
            
            // Info
            Text(
                text = localizeHelper.localize(Res.string.openrouter_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * NVIDIA NIM configuration UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NvidiaConfig(
    apiKey: String,
    selectedModel: String,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onLoadModels: () -> Unit,
    availableModels: List<Pair<String, String>>,
    isLoadingModels: Boolean,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showApiKey by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    
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
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = localizeHelper.localize(Res.string.nvidia_nim),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // API Key Input
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = { Text(localizeHelper.localize(Res.string.nvidia_api_key), maxLines = 1) },
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
            
            // Model Selection
            ExposedDropdownMenuBox(
                expanded = modelExpanded,
                onExpandedChange = { modelExpanded = it }
            ) {
                OutlinedTextField(
                    value = availableModels.find { it.first == selectedModel }?.second ?: selectedModel.ifEmpty { "Llama 3.1 8B" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(localizeHelper.localize(Res.string.nvidia_model), maxLines = 1) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                
                ExposedDropdownMenu(
                    expanded = modelExpanded,
                    onDismissRequest = { modelExpanded = false }
                ) {
                    availableModels.forEach { (modelId, modelName) ->
                        DropdownMenuItem(
                            text = { Text(modelName, style = MaterialTheme.typography.bodySmall) },
                            onClick = {
                                onModelChange(modelId)
                                modelExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Load Models Button
            FilledTonalButton(
                onClick = onLoadModels,
                enabled = apiKey.isNotBlank() && !isLoadingModels,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoadingModels) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading models...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.nvidia_fetch_models))
                }
            }
            
            // Info
            Text(
                text = localizeHelper.localize(Res.string.nvidia_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}


// ==================== Dynamic Plugin Config Renderer ====================

/**
 * Dynamic renderer that builds UI from plugin's getConfigFields()
 * Supports all PluginConfig types defined in the API
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DynamicPluginConfigRenderer(
    pluginEngine: PluginTranslateEngineWrapper,
    modifier: Modifier = Modifier
) {
    val plugin = pluginEngine.getPlugin()
    // Use a refresh key to force recomposition when config changes
    var refreshKey by remember { mutableStateOf(0) }
    val configFields = remember(plugin, refreshKey) { plugin.getConfigFields() }
    
    if (configFields.isEmpty()) {
        // No config fields - show simple info card
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
                    text = "Ready to use - no configuration needed",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            configFields.forEach { config ->
                RenderConfigField(config, plugin, refreshKey) {
                    // Trigger refresh when action is performed
                    refreshKey++
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenderConfigField(
    config: ireader.plugin.api.PluginConfig<*>,
    plugin: ireader.plugin.api.TranslationPlugin,
    refreshKey: Int = 0,
    onRefresh: () -> Unit = {}
) {
    // Force state reload after plugin initialization or refresh
    var reloadTrigger by remember { mutableStateOf(0) }
    
    LaunchedEffect(plugin, refreshKey) {
        // Give plugin time to initialize and load preferences
        delay(50)
        reloadTrigger++
    }
    
    when (config) {
        is ireader.plugin.api.PluginConfig.Header -> {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                config.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        is ireader.plugin.api.PluginConfig.Text -> {
            var textValue by remember(plugin, config.key, reloadTrigger) { 
                mutableStateOf(plugin.getConfigValue(config.key) as? String ?: config.defaultValue) 
            }
            
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    textValue = newValue
                    plugin.onConfigChanged(config.key, newValue)
                },
                label = { Text(config.name) },
                placeholder = config.placeholder?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = config.description?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
                keyboardOptions = when (config.inputType) {
                    ireader.plugin.api.TextInputType.URL -> KeyboardOptions(keyboardType = KeyboardType.Uri)
                    ireader.plugin.api.TextInputType.EMAIL -> KeyboardOptions(keyboardType = KeyboardType.Email)
                    ireader.plugin.api.TextInputType.NUMBER -> KeyboardOptions(keyboardType = KeyboardType.Number)
                    ireader.plugin.api.TextInputType.PHONE -> KeyboardOptions(keyboardType = KeyboardType.Phone)
                    else -> KeyboardOptions.Default
                }
            )
        }
        
        is ireader.plugin.api.PluginConfig.Password -> {
            var passwordValue by remember(plugin, config.key, reloadTrigger) { 
                mutableStateOf(plugin.getConfigValue(config.key) as? String ?: config.defaultValue) 
            }
            var passwordVisible by remember { mutableStateOf(false) }
            
            OutlinedTextField(
                value = passwordValue,
                onValueChange = { newValue ->
                    passwordValue = newValue
                    plugin.onConfigChanged(config.key, newValue)
                },
                label = { Text(config.name) },
                placeholder = config.placeholder?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = config.description?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )
        }
        
        is ireader.plugin.api.PluginConfig.TextArea -> {
            var textValue by remember(plugin, config.key, reloadTrigger) { 
                mutableStateOf(plugin.getConfigValue(config.key) as? String ?: config.defaultValue) 
            }
            
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    textValue = newValue
                    plugin.onConfigChanged(config.key, newValue)
                },
                label = { Text(config.name) },
                placeholder = config.placeholder?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = config.maxLines,
                supportingText = config.description?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } }
            )
        }
        
        is ireader.plugin.api.PluginConfig.Select -> {
            var expanded by remember { mutableStateOf(false) }
            // Include refreshKey in the key to force recomposition when models are refreshed
            var selectedIndex by remember(plugin, config.key, reloadTrigger, refreshKey) { 
                mutableStateOf(plugin.getConfigValue(config.key) as? Int ?: config.defaultValue) 
            }
            
            // For "model" key, try to get options from plugin preferences
            // Use refreshKey to force reload when refresh button is clicked
            val dynamicOptions = remember(plugin, config.key, refreshKey, reloadTrigger) {
                if (config.key == "model") {
                    // Get models list from a special config value
                    (plugin.getConfigValue("cached_models_list") as? List<String>) ?: emptyList()
                } else {
                    config.options
                }
            }
            
            // Update selected index if it's out of bounds
            LaunchedEffect(dynamicOptions.size, refreshKey) {
                if (dynamicOptions.isNotEmpty() && selectedIndex >= dynamicOptions.size) {
                    selectedIndex = 0
                    plugin.onConfigChanged(config.key, 0)
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.labelMedium
                )
                
                if (dynamicOptions.isEmpty()) {
                    // Show placeholder when no models are available
                    OutlinedTextField(
                        value = "Click 'Refresh Models' above",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = dynamicOptions.getOrNull(selectedIndex) ?: dynamicOptions.firstOrNull() ?: "",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            dynamicOptions.forEachIndexed { index, option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedIndex = index
                                        plugin.onConfigChanged(config.key, index)
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }
                
                config.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        is ireader.plugin.api.PluginConfig.Toggle -> {
            var toggleValue by remember(plugin, config.key, reloadTrigger) { 
                mutableStateOf(plugin.getConfigValue(config.key) as? Boolean ?: config.defaultValue) 
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = config.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    config.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = toggleValue,
                    onCheckedChange = { newValue ->
                        toggleValue = newValue
                        plugin.onConfigChanged(config.key, newValue)
                    }
                )
            }
        }
        
        is ireader.plugin.api.PluginConfig.Number -> {
            var numberValue by remember(plugin, config.key, reloadTrigger) { 
                mutableStateOf((plugin.getConfigValue(config.key) as? Int ?: config.defaultValue).toString()) 
            }
            
            OutlinedTextField(
                value = numberValue,
                onValueChange = { newValue ->
                    numberValue = newValue
                    newValue.toIntOrNull()?.let { intValue ->
                        val minValue = config.min
                        val maxValue = config.max
                        val clamped = when {
                            minValue != null && intValue < minValue -> minValue
                            maxValue != null && intValue > maxValue -> maxValue
                            else -> intValue
                        }
                        plugin.onConfigChanged(config.key, clamped)
                    }
                },
                label = { Text(config.name) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = config.description?.let { { Text(it, style = MaterialTheme.typography.bodySmall) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        
        is ireader.plugin.api.PluginConfig.Slider -> {
            var sliderValue by remember(plugin, config.key, reloadTrigger) { 
                mutableStateOf(plugin.getConfigValue(config.key) as? Float ?: config.defaultValue) 
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = config.name,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = config.valueFormat?.let { formatValue(it, sliderValue) } ?: sliderValue.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Slider(
                    value = sliderValue,
                    onValueChange = { newValue ->
                        sliderValue = newValue
                        plugin.onConfigChanged(config.key, newValue)
                    },
                    valueRange = config.min..config.max,
                    steps = config.steps
                )
                
                config.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        is ireader.plugin.api.PluginConfig.Note -> {
            val icon = when (config.noteType) {
                ireader.plugin.api.NoteType.INFO -> Icons.Default.CheckCircle
                ireader.plugin.api.NoteType.WARNING -> Icons.Default.Error
                ireader.plugin.api.NoteType.ERROR -> Icons.Default.Error
                ireader.plugin.api.NoteType.SUCCESS -> Icons.Default.Check
                ireader.plugin.api.NoteType.TIP -> Icons.Default.AutoAwesome
            }
            
            val color = when (config.noteType) {
                ireader.plugin.api.NoteType.INFO -> MaterialTheme.colorScheme.primary
                ireader.plugin.api.NoteType.WARNING -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                ireader.plugin.api.NoteType.ERROR -> MaterialTheme.colorScheme.error
                ireader.plugin.api.NoteType.SUCCESS -> MaterialTheme.colorScheme.primary
                ireader.plugin.api.NoteType.TIP -> MaterialTheme.colorScheme.tertiary
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = config.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = color
                    )
                    config.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        is ireader.plugin.api.PluginConfig.Link -> {
            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
            
            FilledTonalButton(
                onClick = { uriHandler.openUri(config.url) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(config.name)
                    config.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        is ireader.plugin.api.PluginConfig.Action -> {
            var isLoading by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            
            Button(
                onClick = {
                    // Trigger action callback
                    isLoading = true
                    plugin.onConfigChanged(config.key, Unit)
                    // Delay refresh to allow async operations to complete
                    // Increased delay for model fetching operations
                    coroutineScope.launch {
                        delay(2000) // Increased from 1000ms to 2000ms for model fetching
                        isLoading = false
                        onRefresh()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = when (config.actionType) {
                            ireader.plugin.api.ActionType.TEST_CONNECTION -> Icons.Default.NetworkCheck
                            ireader.plugin.api.ActionType.DANGER -> Icons.Default.Error
                            else -> Icons.Default.Refresh
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLoading) "Loading..." else (config.buttonText ?: config.name))
            }
            config.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        is ireader.plugin.api.PluginConfig.Divider -> {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        
        else -> {
            // Unsupported config type - show placeholder
            Text(
                text = "Unsupported config type: ${config::class.simpleName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * KMP-compatible value formatting function
 * Replaces String.format() which is JVM-only
 * 
 * Supports common format patterns:
 * - "%.1f" -> 1 decimal place
 * - "%.2f" -> 2 decimal places
 * - "%d%%" -> integer with % suffix
 */
private fun formatValue(format: String, value: Float): String {
    return when {
        // Float with decimal places: %.1f, %.2f, etc.
        format.matches(Regex("%\\.(\\d+)f")) -> {
            val decimals = format.substringAfter(".").substringBefore("f").toIntOrNull() ?: 1
            val multiplier = pow(10.0, decimals)
            val rounded = kotlin.math.round(value * multiplier) / multiplier
            
            // Manual decimal formatting
            val intPart = rounded.toInt()
            val fracPart = ((rounded - intPart) * multiplier).toInt()
            
            if (decimals == 0) {
                intPart.toString()
            } else {
                val fracStr = fracPart.toString().padStart(decimals, '0')
                "$intPart.$fracStr"
            }
        }
        // Integer with percentage: %d%%
        format == "%d%%" -> "${value.toInt()}%"
        // Integer: %d
        format == "%d" -> value.toInt().toString()
        // Default: just show the value
        else -> value.toString()
    }
}

private fun pow(base: Double, exponent: Int): Double {
    var result = 1.0
    repeat(exponent) {
        result *= base
    }
    return result
}
