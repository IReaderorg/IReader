package ireader.presentation.ui.settings

import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.navigateTo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.unit.dp
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.setupUiComponent
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.PreferenceMutableState
import ireader.presentation.ui.settings.general.TestConnectionState
import ireader.presentation.ui.settings.general.TranslationSettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun TranslationSettingsScreen(
    modifier: Modifier = Modifier,
    translatorEngine: Long,
    openAIApiKey:
    PreferenceMutableState<String>,
    deepSeekApiKey: PreferenceMutableState<String>,
    geminiApiKey: PreferenceMutableState<String>,
    geminiModel: PreferenceMutableState<String>,
    ollamaUrl: PreferenceMutableState<String>,
    ollamaModel: PreferenceMutableState<String>,
    translatorContentType: PreferenceMutableState<Int>,
    translatorToneType: Int,
    translatorPreserveStyle: PreferenceMutableState<Boolean>,
    onTranslatorEngineChange: (Long) -> Unit,
    onOpenAIApiKeyChange: (String) -> Unit,
    onDeepSeekApiKeyChange: (String) -> Unit,
    onGeminiApiKeyChange: (String) -> Unit,
    onGeminiModelChange: (String) -> Unit,
    onOllamaUrlChange: (String) -> Unit,
    onOllamaModelChange: (String) -> Unit,
    onTranslatorContentTypeChange: (Int) -> Unit,
    onTranslatorToneTypeChange: (Int) -> Unit,
    onTranslatorPreserveStyleChange: (Boolean) -> Unit,
    translationEnginesManager: TranslationEnginesManager,
    viewModel: TranslationSettingsViewModel? = null,
) {
    // This state is used to force recomposition
    var recomposeCounter by remember { mutableStateOf(0) }
    val forceRecompose = { recomposeCounter += 1 }
    val navigator = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
    val scrollState = rememberScrollState()
    
    // Get all available engines including plugins
    // Requirements: 4.1, 4.2
    val engineSources = translationEnginesManager.getAvailableEngines()
    val engines = engineSources.mapNotNull { source ->
        when (source) {
            is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn -> source.engine
            is ireader.domain.usecases.translate.TranslationEngineSource.Plugin -> null
        }
    }
    val options = engines.map { it.id to it.engineName }
    
    // Add plugin options
    val pluginOptions = engineSources.mapNotNull { source ->
        when (source) {
            is ireader.domain.usecases.translate.TranslationEngineSource.Plugin -> 
                source.plugin.manifest.id to "${source.plugin.manifest.name} (Plugin)"
            else -> null
        }
    }
    
    var openAIKeyVisible by remember { mutableStateOf(false) }
    //Temp
    var deepSeekKeyVisible by remember { mutableStateOf(false) }
    var pluginKeyVisible by remember { mutableStateOf(false) }
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val contentTypes = ContentType.entries.map {
        it.ordinal to it.name.lowercase()
            .replaceFirstChar { c -> c.uppercase() }
    }
    val toneTypes = ToneType.entries.map {
        it.ordinal to it.name.lowercase()
            .replaceFirstChar { c -> c.uppercase() }
    }

    // Determine if the current engine is AI-powered
    val currentEngine = engines.find { it.id == translatorEngine }
    val isAiEngine = currentEngine?.supportsContextAwareTranslation == true

    val items = mutableListOf<Components>()

    // Info card about translation engines
    items.add(Components.Dynamic {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = localizeHelper.localize(Res.string.translation_settings),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Select your preferred translation engine and configure its settings. AI-powered engines (OpenAI, DeepSeek) require API keys and offer more advanced features.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    })

    // Engine Selection Section
    items.add(
        Components.Header(
            localizeHelper.localize(Res.string.translation_engine)
        )
    )

    items.add(
        Components.Chip(
            preference = options.map { it.second },
            title = localizeHelper.localize(Res.string.translation_engine),
            onValueChange = { value ->
                onTranslatorEngineChange(value.toLong())
                forceRecompose()
            },
            selected = translatorEngine.toInt(),
        )
    )

    // Plugin Translation Engines Section
    // Requirements: 4.2
    if (pluginOptions.isNotEmpty()) {
        items.add(Components.Space)
        items.add(
            Components.Header(
                "Plugin Translation Engines"
            )
        )
        
        items.add(Components.Dynamic {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Translation plugins extend IReader with additional translation services",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        })
        
        // Show plugin engines as options
        pluginOptions.forEach { (pluginId, pluginName) ->
            items.add(
                Components.Row(
                    title = pluginName,
                    icon = Icons.Default.Translate,
                    subtitle = "Plugin-based translation",
                    onClick = {
                        // Set plugin as selected engine
                        // This would need to be handled by the viewModel
                    }
                )
            )
        }
    }

    items.add(Components.Space)

    // API Keys Section (only show if an engine requiring API keys is selected)
    if (engines.find { it.id == translatorEngine }?.requiresApiKey == true) {
        items.add(
            Components.Header(
                localizeHelper.localize(Res.string.api_keys)
            )
        )

        if (translatorEngine == 2L) { // OpenAI
            items.add(
                Components.Row(
                    title = localizeHelper.localize(Res.string.openai_api_key),
                    icon = Icons.Default.Api,
                    subtitle = "Required for OpenAI translations",
                    onClick = {},
                    action = {
                        OutlinedTextField(
                            value = openAIApiKey.value,
                            onValueChange = {
                                onOpenAIApiKeyChange(it)
                                forceRecompose()
                            },
                            label = { Text(localizeHelper.localize(Res.string.openai_api_key)) },
                            visualTransformation = if (openAIKeyVisible) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                Components.Companion.VisibilityIcon(
                                    visible = openAIKeyVisible,
                                    onVisibilityChanged = { openAIKeyVisible = it }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            )
                        )
                    },
                )
            )
        }

        if (translatorEngine == 3L) { // DeepSeek
            items.add(
                Components.Row(
                    title = localizeHelper.localize(Res.string.deepseek_api_key),
                    icon = Icons.Default.Api,
                    subtitle = "Required for DeepSeek translations",
                    onClick = {},
                    action = {
                        OutlinedTextField(
                            value = deepSeekApiKey.value,
                            onValueChange = {
                                onDeepSeekApiKeyChange(it)
                                forceRecompose()
                            },
                            label = { Text(localizeHelper.localize(Res.string.deepseek_api_key)) },
                            visualTransformation = if (deepSeekKeyVisible) {
                                androidx.compose.ui.text.input.VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                Components.Companion.VisibilityIcon(
                                    visible = deepSeekKeyVisible,
                                    onVisibilityChanged = { deepSeekKeyVisible = it }
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            )
                        )
                    },
                )
            )
        }
        
        // Plugin API Key Configuration
        // Requirements: 4.2
        val selectedPluginEngine = engineSources.find { source ->
            when (source) {
                is ireader.domain.usecases.translate.TranslationEngineSource.Plugin -> {
                    // Check if this plugin is selected (would need to be tracked in preferences)
                    false // Placeholder - would need proper selection tracking
                }
                else -> false
            }
        }
        
        if (selectedPluginEngine is ireader.domain.usecases.translate.TranslationEngineSource.Plugin) {
            val plugin = selectedPluginEngine.plugin
            if (plugin.requiresApiKey()) {
                items.add(
                    Components.Row(
                        title = "${plugin.manifest.name} API Key",
                        icon = Icons.Default.Api,
                        subtitle = "Required for ${plugin.manifest.name} translations",
                        onClick = {},
                        action = {
                            var pluginApiKey by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = pluginApiKey,
                                onValueChange = {
                                    pluginApiKey = it
                                    plugin.configureApiKey(it)
                                    forceRecompose()
                                },
                                label = { Text("${plugin.manifest.name} API Key") },
                                visualTransformation = if (pluginKeyVisible) {
                                    androidx.compose.ui.text.input.VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    Components.Companion.VisibilityIcon(
                                        visible = pluginKeyVisible,
                                        onVisibilityChanged = { pluginKeyVisible = it }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password
                                )
                            )
                        },
                    )
                )
            }
        }

        // Add Test Connection button for API-based engines
        if (viewModel != null) {
            items.add(Components.Dynamic {
                val testState = viewModel.testConnectionState
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = { viewModel.testConnection() },
                        enabled = testState !is TestConnectionState.Testing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (testState) {
                            is TestConnectionState.Testing -> Text("Testing Connection...")
                            else -> Text("Test Connection")
                        }
                    }
                    
                    when (testState) {
                        is TestConnectionState.Success -> {
                            Text(
                                text = testState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        is TestConnectionState.Error -> {
                            Text(
                                text = testState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        else -> {}
                    }
                }
            })
        }
        
        items.add(Components.Space)
    }

    // Add Ollama settings section if that engine is selected
    if (translatorEngine == 5L) { // Ollama
        items.add(
            Components.Header(
                "Ollama Configuration"
            )
        )

        items.add(
            Components.Row(
                title = localizeHelper.localize(Res.string.ollama_url),
                icon = Icons.Default.Api,
                subtitle = "URL of your Ollama API server",
                onClick = {},
                action = {
                    OutlinedTextField(
                        value = ollamaUrl.value,
                        onValueChange = {
                            onOllamaUrlChange(it)
                            forceRecompose()
                        },
                        label = { Text(localizeHelper.localize(Res.string.ollama_url)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri
                        )
                    )
                },
            )
        )

        items.add(
            Components.Row(
                title = localizeHelper.localize(Res.string.ollama_model),
                icon = Icons.Default.Settings,
                subtitle = "Model to use for translation (e.g., mistral, llama2)",
                onClick = {},
                action = {
                    OutlinedTextField(
                        value = ollamaModel.value,
                        onValueChange = {
                            onOllamaModelChange(it)
                            forceRecompose()
                        },
                        label = { Text(localizeHelper.localize(Res.string.ollama_model)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        )
                    )
                },
            )
        )

        items.add(Components.Dynamic {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ollama Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Ollama is a local LLM server for translations. Make sure Ollama is installed and running on your device or network. You need to pull the model first with 'ollama pull modelname'.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        })

        items.add(Components.Space)
    }

    // Add LibreTranslate info section if that engine is selected
    if (translatorEngine == 4L) { // LibreTranslate
        items.add(
            Components.Header(
                "LibreTranslate Information"
            )
        )

        items.add(Components.Dynamic {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LibreTranslate",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.libretranslate_info),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        })

        items.add(Components.Space)
    }


    // Add Webscraping AI translation section if that engine is selected
    if (translatorEngine == 6L) { // WebscrapingTranslateEngine
        items.add(
            Components.Header(
                "ChatGPT WebView Translation"
            )
        )

        items.add(Components.Dynamic {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ChatGPT Login Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "This translation engine uses a WebView to access ChatGPT directly. You need to sign in to ChatGPT with your account to use this feature. The app will save your cookies for future translations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "If a CAPTCHA appears, you will need to complete it manually.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Button(
                        onClick = {
                            // Navigate to ChatGpt login screen
                            navigator.navigateTo(
                                ireader.presentation.core.ui.ChatGptLoginScreenSpec()
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    ) {
                        Text("Sign in to ChatGPT")
                    }
                }
            }
        })

        items.add(Components.Space)
    }

    // Add DeepSeek WebView translation section if that engine is selected
    if (translatorEngine == 7L) { // DeepSeek WebView
        items.add(
            Components.Header(
                "DeepSeek WebView Translation"
            )
        )

        items.add(Components.Dynamic {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DeepSeek Login Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "This translation engine uses a WebView to access DeepSeek directly. You need to sign in to DeepSeek with your account to use this feature. The app will save your cookies for future translations.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "If a CAPTCHA appears, you will need to complete it manually.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Button(
                        onClick = {
                            // Navigate to DeepSeek login screen
                            navigator.navigate(
                                ireader.presentation.core.ui.DeepSeekLoginScreenSpec()
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 16.dp)
                    ) {
                        Text("Sign in to DeepSeek")
                    }
                }
            }
        })

        items.add(Components.Space)
    }

    // Add Gemini API settings section if that engine is selected
    if (translatorEngine == 8L) { // Gemini API
        items.add(
            Components.Header(
                "Google Gemini API"
            )
        )

        var geminiKeyVisible by remember { mutableStateOf(false) }

        items.add(
            Components.Row(
                title = localizeHelper.localize(Res.string.gemini_api_key),
                icon = Icons.Default.Api,
                subtitle = "Required for Google Gemini translations",
                onClick = {},
                action = {
                    OutlinedTextField(
                        value = geminiApiKey.value,
                        onValueChange = {
                            onGeminiApiKeyChange(it)
                            forceRecompose()
                        },
                        label = { Text(localizeHelper.localize(Res.string.gemini_api_key)) },
                        visualTransformation = if (geminiKeyVisible) {
                            androidx.compose.ui.text.input.VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            Components.Companion.VisibilityIcon(
                                visible = geminiKeyVisible,
                                onVisibilityChanged = { geminiKeyVisible = it }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        )
                    )
                },
            )
        )

        items.add(Components.Dynamic {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gemini API Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Google's Gemini API provides high-quality translations through their AI platform. To use this feature, you need to obtain an API key from Google AI Studio (https://aistudio.google.com).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        })

        items.add(Components.Space)
    }

    // Advanced Settings Section
    if (isAiEngine) {
        items.add(
            Components.Header(
                localizeHelper.localize(Res.string.advanced_settings)
            )
        )

        items.add(
            Components.Chip(
            title = localizeHelper.localize(Res.string.content_type),
            icon = Icons.Default.Translate,
            subtitle = "Optimizes translation for specific content",
            onValueChange = {
                onTranslatorContentTypeChange(it)
                forceRecompose()
            },
            selected = translatorContentType.value,
            preference = contentTypes.map { it.second }
        ))

        items.add(
            Components.Chip(
            title = localizeHelper.localize(Res.string.tone_type),
            icon = Icons.Default.Translate,
            subtitle = "Sets the tone of translated text",
            onValueChange = {
                onTranslatorToneTypeChange(it)
                forceRecompose()
            },
            selected = translatorToneType,
            preference = toneTypes.map { it.second }
        ))

        items.add(
            Components.Switch(
                title = localizeHelper.localize(Res.string.preserve_style),
                icon = Icons.Default.Style,
                subtitle = "Keep the original writing style when translating",
                preference = translatorPreserveStyle,
            )
        )
    } else {
        items.add(Components.Dynamic {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Advanced Features",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Switch to an AI-powered engine (OpenAI or DeepSeek) to access advanced translation settings like content type, tone control, and style preservation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        })
    }

    // Gemini API section
    if (currentEngine?.id == 8L) { // Check if Gemini is the selected engine (ID 8)
        items.add(Components.Header("Gemini Settings"))

        items.add(Components.Dynamic {
            TextField(
                label = { Text("Gemini API Key") },
                value = geminiApiKey.value,
                onValueChange = { onGeminiApiKeyChange(it) },
                placeholder = { Text("Enter your Gemini API key") },
            )

        })

        // Add Gemini model selection dropdown
        items.add(Components.Dynamic {
            // Requirements: 6.4 - Load cached models on first display
            LaunchedEffect(Unit) {
                viewModel?.loadCachedGeminiModels()
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Gemini Model",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Select which Gemini model to use for translation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Refresh models button
                // Requirements: 6.3 - Add loading state during fetch
                var isRefreshing by remember { mutableStateOf(false) }
                var refreshMessage by remember { mutableStateOf<String?>(null) }
                val coroutineScope = rememberCoroutineScope()
                
                // Use ViewModel state if available
                val actualIsRefreshing = viewModel?.isRefreshingModels ?: isRefreshing
                val actualRefreshMessage = viewModel?.modelRefreshMessage ?: refreshMessage
                
                Button(
                    onClick = {
                        // Use ViewModel's refresh functionality if available
                        // Requirements: 6.4 - Cache fetched models in ViewModel
                        if (viewModel != null) {
                            viewModel.refreshGeminiModels()
                        } else {
                            // Fallback to inline implementation
                            isRefreshing = true
                            refreshMessage = null
                            coroutineScope.launch {
                                try {
                                    // Requirements: 6.1 - Use stable engine lookup
                                    val engines = translationEnginesManager.getAvailableEngines()
                                    val geminiEngineSource = engines.find { source ->
                                        when (source) {
                                            is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn -> 
                                                source.engine.id == 8L
                                            else -> false
                                        }
                                    }
                                    
                                    if (geminiEngineSource == null) {
                                        refreshMessage = "Gemini engine not found. Please restart the app."
                                        isRefreshing = false
                                        return@launch
                                    }
                                    
                                    if (geminiApiKey.value.isBlank()) {
                                        refreshMessage = "Please enter your Gemini API key first"
                                        isRefreshing = false
                                        return@launch
                                    }
                                    
                                    // Requirements: 6.2, 6.3 - Implement Gemini model refresh
                                    val geminiEngine = when (geminiEngineSource) {
                                        is ireader.domain.usecases.translate.TranslationEngineSource.BuiltIn -> 
                                            geminiEngineSource.engine as? ireader.domain.usecases.translate.WebscrapingTranslateEngine
                                        else -> null
                                    }
                                    
                                    if (geminiEngine != null) {
                                        val result = geminiEngine.fetchAvailableGeminiModels(geminiApiKey.value)
                                        refreshMessage = if (result.isSuccess) {
                                            val models = result.getOrNull()
                                            if (models.isNullOrEmpty()) {
                                                "No models found. Please check your API key."
                                            } else {
                                                "Successfully loaded ${models.size} model(s)"
                                            }
                                        } else {
                                            val error = result.exceptionOrNull()
                                            when {
                                                error?.message?.contains("401") == true || 
                                                error?.message?.contains("API key") == true -> 
                                                    "Invalid API key. Please check your key and try again."
                                                error?.message?.contains("403") == true -> 
                                                    "Access forbidden. Please check your API key permissions."
                                                error?.message?.contains("429") == true -> 
                                                    "Rate limit exceeded. Please try again later."
                                                error?.message?.contains("timeout") == true -> 
                                                    "Request timed out. Please check your internet connection."
                                                else -> 
                                                    "Failed to fetch models: ${error?.message ?: "Unknown error"}"
                                            }
                                        }
                                    } else {
                                        refreshMessage = "Unable to access Gemini engine"
                                    }
                                } catch (e: Exception) {
                                    refreshMessage = "Error: ${e.message ?: "Unknown error occurred"}"
                                } finally {
                                    isRefreshing = false
                                }
                            }
                        }
                    },
                    enabled = !actualIsRefreshing && geminiApiKey.value.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text(if (actualIsRefreshing) "Refreshing..." else "Refresh Available Models")
                }
                
                // Requirements: 6.5 - Add error handling with user-friendly messages
                actualRefreshMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.startsWith("Successfully")) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Requirements: 6.4 - Use cached models from ViewModel or fallback to static list
                val geminiModels = if (viewModel != null && viewModel.geminiModels.isNotEmpty()) {
                    viewModel.geminiModels
                } else {
                    ireader.domain.usecases.translate.WebscrapingTranslateEngine.AVAILABLE_GEMINI_MODELS
                }

                // Use a Column instead of LazyColumn to avoid nested scrolling containers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (geminiModels.isEmpty()) {
                        // Show message when no models are available
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "No models available",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Please click 'Refresh Available Models' above to load models from the Gemini API.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    } else {
                        geminiModels.forEachIndexed { index, model ->
                            Button(
                                onClick = { onGeminiModelChange(model.first) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (geminiModel.value == model.first)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = model.second,
                                    color = if (geminiModel.value == model.first)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        })

        // Add help text about models
        items.add(Components.Dynamic {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Note: If a model's quota is exceeded, the app will automatically try other models.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        })
    }

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        modifier = modifier.fillMaxSize()
    ) {
        setupUiComponent(list = items)
    }
} 