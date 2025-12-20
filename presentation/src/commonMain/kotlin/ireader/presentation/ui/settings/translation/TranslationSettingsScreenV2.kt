package ireader.presentation.ui.settings.translation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.plugins.TranslationPlugin
import ireader.domain.usecases.translate.TranslationEngineSource
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.i18n.UiText
import ireader.presentation.ui.core.modifier.supportDesktopScroll
import ireader.presentation.ui.settings.general.TranslationSettingsViewModel

/**
 * Wrapper class to adapt TranslationPlugin to TranslateEngine interface
 * This allows plugin engines to be displayed alongside built-in engines
 */
class PluginTranslateEngineWrapper(
    private val plugin: TranslationPlugin
) : TranslateEngine() {
    override val id: Long = plugin.manifest.id.hashCode().toLong()
    override val engineName: String = plugin.manifest.name
    override val supportsAI: Boolean = plugin.supportsAI
    override val supportsContextAwareTranslation: Boolean = plugin.supportsContextAwareTranslation
    override val supportsStylePreservation: Boolean = plugin.supportsStylePreservation
    override val requiresApiKey: Boolean = plugin.requiresApiKey()
    override val maxCharsPerRequest: Int = plugin.maxCharsPerRequest
    override val rateLimitDelayMs: Long = plugin.rateLimitDelayMs
    override val isOffline: Boolean = plugin.isOffline
    
    val pluginId: String = plugin.manifest.id
    val manifest: ireader.plugin.api.PluginManifest = plugin.manifest
    
    /**
     * Get the underlying plugin for configuration
     */
    fun getPlugin(): TranslationPlugin = plugin
    
    override val supportedLanguages: List<Pair<String, String>> = plugin.getAvailableLanguages()
    
    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        try {
            val result = plugin.translateBatch(texts, source, target)
            result.onSuccess { translations ->
                onProgress(100)
                onSuccess(translations)
            }.onFailure { error ->
                onError(UiText.ExceptionString(error as Exception))
            }
        } catch (e: Exception) {
            onError(UiText.ExceptionString(e))
        }
    }
}

/**
 * Improved Translation Settings Screen with Gemini-first engine selection
 * Optimized for both mobile and desktop
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationSettingsScreenV2(
    viewModel: TranslationSettingsViewModel,
    translationEnginesManager: TranslationEnginesManager,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Get all engines (built-in + plugins)
    val engineSources = translationEnginesManager.getAvailableEngines()
    val engines = engineSources.mapNotNull { source ->
        when (source) {
            is TranslationEngineSource.BuiltIn -> source.engine
            is TranslationEngineSource.Plugin -> {
                // Create a wrapper engine for plugins
                PluginTranslateEngineWrapper(source.plugin)
            }
        }
    }

    val currentEngine = engines.find { it.id == viewModel.translatorEngine.value }
    val isAiEngine = currentEngine?.supportsContextAwareTranslation == true
    val isGeminiSelected = viewModel.translatorEngine.value == 8L
    val isOpenAISelected = viewModel.translatorEngine.value == 2L
    val isDeepSeekSelected = viewModel.translatorEngine.value == 3L
    
    // Check if current engine is a plugin
    val isPluginEngine = currentEngine is PluginTranslateEngineWrapper
    
    // Show API key section for engines that need it (Gemini, OpenAI, DeepSeek, or plugin engines that require API key)
    val showApiKeySection = isGeminiSelected || isOpenAISelected || isDeepSeekSelected || 
                           (currentEngine?.requiresApiKey == true)

    // Load cached models on first composition
    LaunchedEffect(Unit) {
        viewModel.loadCachedGeminiModels()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Translation Settings",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            state = scrollState,
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .supportDesktopScroll(scrollState, scope),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Current Engine Info
            item(key = "current_engine") {
                CurrentEngineCard(engineName = currentEngine?.engineName ?: "None")
            }

            // Engine Selection Section
            item(key = "engine_header") {
                SectionHeader(
                    title = "Translation Engine",
                    icon = Icons.Default.Translate
                )
            }

            item(key = "engine_selector") {
                TranslationEngineSelector(
                    engines = engines,
                    selectedEngineId = viewModel.translatorEngine.value,
                    onEngineSelected = { viewModel.updateTranslatorEngine(it) }
                )
            }

            // API Key Section - Show for Gemini, OpenAI, DeepSeek
            if (showApiKeySection) {
                item(key = "api_key_header") {
                    SectionHeader(
                        title = "API Key",
                        icon = Icons.Default.Key
                    )
                }

                item(key = "api_key_config") {
                    ApiKeyConfigSection(
                        engineId = viewModel.translatorEngine.value,
                        apiKey = getApiKeyForEngine(viewModel, viewModel.translatorEngine.value),
                        onApiKeyChange = { setApiKeyForEngine(viewModel, viewModel.translatorEngine.value, it) },
                        onTestConnection = { viewModel.testConnection() },
                        testState = viewModel.testConnectionState
                    )
                }
            }

            // Gemini Model Selection (when Gemini is selected)
            if (isGeminiSelected) {
                item(key = "gemini_model_header") {
                    SectionHeader(
                        title = "Gemini Model",
                        icon = Icons.Default.ModelTraining
                    )
                }

                item(key = "gemini_model_selector") {
                    GeminiModelSelector(
                        models = viewModel.geminiModels,
                        selectedModel = viewModel.geminiModel.value,
                        onModelSelected = { viewModel.updateGeminiModel(it) },
                        onRefreshModels = { viewModel.refreshGeminiModels() },
                        isRefreshing = viewModel.isRefreshingModels,
                        refreshMessage = viewModel.modelRefreshMessage,
                        apiKeySet = viewModel.geminiApiKey.value.isNotBlank()
                    )
                }
            }

            // Engine-Specific Configuration (Ollama, WebView logins, Plugin configs)
            item(key = "engine_specific_config") {
                EngineSpecificConfig(
                    engineId = viewModel.translatorEngine.value,
                    viewModel = viewModel,
                    onNavigateToLogin = onNavigateToLogin,
                    currentPluginEngine = currentEngine as? PluginTranslateEngineWrapper,
                    translationEnginesManager = translationEnginesManager
                )
            }

            // Advanced Settings Section (only for AI engines)
            if (isAiEngine) {
                item(key = "advanced_header") {
                    SectionHeader(
                        title = "Advanced",
                        icon = Icons.Default.Tune
                    )
                }

                item(key = "advanced_settings") {
                    AdvancedSettingsSection(
                        contentType = viewModel.translatorContentType.value,
                        toneType = viewModel.translatorToneType.value,
                        preserveStyle = viewModel.translatorPreserveStyle.value,
                        onContentTypeChange = { viewModel.updateTranslatorContentType(it) },
                        onToneTypeChange = { viewModel.updateTranslatorToneType(it) },
                        onPreserveStyleChange = { viewModel.updateTranslatorPreserveStyle(it) },
                        isAiEngine = isAiEngine
                    )
                }
            }

            // Bottom spacing
            item(key = "bottom_spacing") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CurrentEngineCard(engineName: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Active: $engineName",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1
        )
    }
}

private fun getApiKeyForEngine(viewModel: TranslationSettingsViewModel, engineId: Long): String {
    return when (engineId) {
        2L -> viewModel.openAIApiKey.value
        3L -> viewModel.deepSeekApiKey.value
        8L -> viewModel.geminiApiKey.value
        else -> ""
    }
}

private fun setApiKeyForEngine(viewModel: TranslationSettingsViewModel, engineId: Long, value: String) {
    when (engineId) {
        2L -> viewModel.updateOpenAIApiKey(value)
        3L -> viewModel.updateDeepSeekApiKey(value)
        8L -> viewModel.updateGeminiApiKey(value)
    }
}
