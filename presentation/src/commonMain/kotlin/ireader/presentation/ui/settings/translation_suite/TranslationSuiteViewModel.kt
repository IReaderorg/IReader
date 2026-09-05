package ireader.presentation.ui.settings.translation_suite

import androidx.compose.runtime.Stable
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.domain.community.CommunityPreferences
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslateEngine
import ireader.domain.data.engines.TranslationContext
import ireader.domain.models.entities.TextReplacement
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.TranslationPreferences
import ireader.domain.usecases.reader.TextReplacementUseCase
import ireader.domain.usecases.translate.TranslationEngineSource
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.domain.usecases.translate.PluginTranslateEngineAdapter
import ireader.domain.usecases.translate.WebscrapingTranslateEngine
import ireader.domain.usecases.translate.OpenRouterTranslateEngine
import ireader.domain.usecases.translate.NvidiaTranslateEngine
import ireader.i18n.LocalizeHelper
import ireader.i18n.asString
import ireader.presentation.ui.core.viewmodel.StateViewModel
import ireader.presentation.ui.settings.general.MlKitInitState
import ireader.presentation.ui.settings.general.TestConnectionState
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Stable
data class GlossaryTermItem(
    val id: Long = 0L,
    val sourceTerm: String = "",
    val targetTerm: String = "",
    val notes: String = ""
)

@Stable
data class TextCleanupPreset(
    val id: String,
    val title: String,
    val description: String,
    val pattern: String,
    val isEnabled: Boolean = true
)

@Stable
data class TranslationSuiteState(
    val selectedEngineId: Long = 11L, // Default to Google Translate Free
    val selectedEngineName: String = "Google Translate (Free)",
    val availableEngines: List<TranslateEngine> = emptyList(),

    // Engine Credentials & Parameters
    val openAIApiKey: String = "",
    val openAIBaseUrl: String = "https://api.openai.com/v1",
    val openAIModel: String = "gpt-3.5-turbo",
    val deepSeekApiKey: String = "",
    val geminiApiKey: String = "",
    val geminiModel: String = "",
    val geminiModels: List<Pair<String, String>> = emptyList(),
    val isRefreshingGeminiModels: Boolean = false,

    val claudeApiKey: String = "",
    val claudeModel: String = "claude-3-5-sonnet-20241022",

    val openRouterApiKey: String = "",
    val openRouterModel: String = "",
    val openRouterModels: List<Pair<String, String>> = emptyList(),
    val isLoadingOpenRouterModels: Boolean = false,

    val nvidiaApiKey: String = "",
    val nvidiaModel: String = "",
    val nvidiaModels: List<Pair<String, String>> = emptyList(),
    val isLoadingNvidiaModels: Boolean = false,

    val ollamaUrl: String = "http://localhost:11434",
    val ollamaModel: String = "",

    val targetLanguage: String = "English",
    val availableLanguages: List<String> = listOf("English", "Spanish", "French", "German", "Russian", "Japanese", "Chinese", "Korean", "Arabic", "Portuguese"),

    // Status & Testing
    val testConnectionState: TestConnectionState = TestConnectionState.Idle,
    val mlKitInitState: MlKitInitState = MlKitInitState.Idle,
    val mlKitInitProgress: Int = 0,
    val modelRefreshMessage: String? = null,

    // Translation Context & Style
    val contentType: ContentType = ContentType.GENERAL,
    val toneType: ToneType = ToneType.NEUTRAL,
    val preserveStyle: Boolean = false,
    val customPrompt: String = "",

    // Automation & Community
    val autoTranslateChapters: Boolean = false,
    val autoTranslateNovelNames: Boolean = false,
    val autoShareTranslations: Boolean = false,
    val contributorName: String = "",

    // Glossary & Text Cleanup
    val glossaryTerms: List<GlossaryTermItem> = emptyList(),
    val glossarySearchQuery: String = "",
    val textReplacements: List<TextReplacement> = emptyList(),
    val activePresets: List<TextCleanupPreset> = listOf(
        TextCleanupPreset("nav_hints", "Strip Navigation Hints", "Removes 'Use arrow keys to navigate' hints", "Use arrow keys.*chapter", true),
        TextCleanupPreset("promo_ads", "Strip Promotional Ads", "Removes 'Visit site for more chapters' ads", "Visit.*for more chapters", true),
        TextCleanupPreset("watermarks", "Strip Source Watermarks", "Removes 'Read more at...' watermarks", "Read more at.*", true),
        TextCleanupPreset("brackets", "Remove Bracket Metadata", "Removes novel hosting bracket tags", "\\[(Updated|RAW|MTL)\\]", false)
    ),
    val showAddGlossaryDialog: Boolean = false,
    val showAddRuleDialog: Boolean = false
)

class TranslationSuiteViewModel(
    private val readerPreferences: ReaderPreferences,
    private val translationPreferences: TranslationPreferences,
    val translationEnginesManager: TranslationEnginesManager? = null,
    private val textReplacementUseCase: TextReplacementUseCase? = null,
    private val communityPreferences: CommunityPreferences? = null,
    private val localizeHelper: LocalizeHelper? = null,
    private val httpClient: HttpClients? = null
) : StateViewModel<TranslationSuiteState>(TranslationSuiteState()) {

    init {
        loadSettings()
        loadEngines()
        loadReplacements()
    }

    private fun loadSettings() {
        val engineId = readerPreferences.translatorEngine().get().let { if (it == -1L) 11L else it }
        val openAIKey = readerPreferences.openAIApiKey().get()
        val openAIBase = readerPreferences.openAIBaseUrl().get()
        val openAIM = readerPreferences.openAIModel().get()
        val deepSeekKey = readerPreferences.deepSeekApiKey().get()
        val geminiKey = readerPreferences.geminiApiKey().get()
        val geminiM = readerPreferences.geminiModel().get()
        val claudeKey = readerPreferences.claudeApiKey().get()
        val claudeM = readerPreferences.claudeModel().get()
        val openRouterKey = readerPreferences.openRouterApiKey().get()
        val openRouterM = readerPreferences.openRouterModel().get()
        val nvidiaKey = readerPreferences.nvidiaApiKey().get()
        val nvidiaM = readerPreferences.nvidiaModel().get()
        val ollamaServerUrl = readerPreferences.ollamaServerUrl().get()
        val ollamaM = readerPreferences.ollamaModel().get()

        val contentTypeInt = readerPreferences.translatorContentType().get()
        val toneTypeInt = readerPreferences.translatorToneType().get()
        val preserve = readerPreferences.translatorPreserveStyle().get()
        val prompt = readerPreferences.translationCustomPrompt().get()

        val autoNovel = translationPreferences.autoTranslateNovelNames().get()
        val autoShare = communityPreferences?.autoShareTranslations()?.get() ?: false
        val contributor = communityPreferences?.contributorName()?.get() ?: ""

        val resolvedContentType = ContentType.entries.getOrElse(contentTypeInt) { ContentType.GENERAL }
        val resolvedToneType = ToneType.entries.getOrElse(toneTypeInt) { ToneType.NEUTRAL }

        updateState {
            it.copy(
                selectedEngineId = engineId,
                openAIApiKey = openAIKey,
                openAIBaseUrl = if (openAIBase.isNotBlank()) openAIBase else "https://api.openai.com/v1",
                openAIModel = if (openAIM.isNotBlank()) openAIM else "gpt-3.5-turbo",
                deepSeekApiKey = deepSeekKey,
                geminiApiKey = geminiKey,
                geminiModel = geminiM,
                claudeApiKey = claudeKey,
                claudeModel = if (claudeM.isNotBlank()) claudeM else "claude-3-5-sonnet-20241022",
                openRouterApiKey = openRouterKey,
                openRouterModel = openRouterM,
                nvidiaApiKey = nvidiaKey,
                nvidiaModel = nvidiaM,
                ollamaUrl = if (ollamaServerUrl.isNotBlank()) ollamaServerUrl else "http://localhost:11434",
                ollamaModel = ollamaM,
                contentType = resolvedContentType,
                toneType = resolvedToneType,
                preserveStyle = preserve,
                customPrompt = prompt,
                autoTranslateNovelNames = autoNovel,
                autoShareTranslations = autoShare,
                contributorName = contributor
            )
        }
    }

    private fun loadEngines() {
        val manager = translationEnginesManager ?: return
        val engines = manager.getAvailableEngines().map { source ->
            when (source) {
                is TranslationEngineSource.BuiltIn -> source.engine
                is TranslationEngineSource.Plugin -> PluginTranslateEngineAdapter(source.plugin, manager)
            }
        }
        val currentEngine = manager.get()

        updateState {
            it.copy(
                availableEngines = engines,
                selectedEngineId = currentEngine.id,
                selectedEngineName = currentEngine.engineName
            )
        }
    }

    private fun loadReplacements() {
        scope.launch {
            val replacements = textReplacementUseCase?.getGlobalReplacements()
                ?.catch { }
                ?.firstOrNull() ?: emptyList()

            updateState { it.copy(textReplacements = replacements) }
        }
    }

    fun setEngineId(engineId: Long) {
        val manager = translationEnginesManager
        if (manager != null) {
            val source = manager.getAvailableEngines().find {
                when (it) {
                    is TranslationEngineSource.BuiltIn -> it.engine.id == engineId
                    is TranslationEngineSource.Plugin -> it.plugin.manifest.id.hashCode().toLong() == engineId
                }
            }
            if (source != null) {
                manager.setSelectedEngine(source)
            } else {
                readerPreferences.translatorEngine().set(engineId)
            }
        } else {
            readerPreferences.translatorEngine().set(engineId)
        }
        val engine = state.value.availableEngines.find { it.id == engineId }
        val name = engine?.engineName ?: "Engine #$engineId"
        updateState { it.copy(selectedEngineId = engineId, selectedEngineName = name) }
    }

    fun setTargetLanguage(language: String) {
        updateState { it.copy(targetLanguage = language) }
    }

    fun setOpenAIApiKey(key: String) {
        readerPreferences.openAIApiKey().set(key)
        updateState { it.copy(openAIApiKey = key) }
    }

    fun setOpenAIBaseUrl(url: String) {
        readerPreferences.openAIBaseUrl().set(url)
        updateState { it.copy(openAIBaseUrl = url) }
    }

    fun setOpenAIModel(model: String) {
        readerPreferences.openAIModel().set(model)
        updateState { it.copy(openAIModel = model) }
    }

    fun setDeepSeekApiKey(key: String) {
        readerPreferences.deepSeekApiKey().set(key)
        updateState { it.copy(deepSeekApiKey = key) }
    }

    fun setGeminiApiKey(key: String) {
        readerPreferences.geminiApiKey().set(key)
        updateState { it.copy(geminiApiKey = key) }
    }

    fun setGeminiModel(model: String) {
        readerPreferences.geminiModel().set(model)
        updateState { it.copy(geminiModel = model) }
    }

    fun setClaudeApiKey(key: String) {
        readerPreferences.claudeApiKey().set(key)
        updateState { it.copy(claudeApiKey = key) }
    }

    fun setClaudeModel(model: String) {
        readerPreferences.claudeModel().set(model)
        updateState { it.copy(claudeModel = model) }
    }

    fun setOpenRouterApiKey(key: String) {
        readerPreferences.openRouterApiKey().set(key)
        updateState { it.copy(openRouterApiKey = key) }
    }

    fun setOpenRouterModel(model: String) {
        readerPreferences.openRouterModel().set(model)
        updateState { it.copy(openRouterModel = model) }
    }

    fun setNvidiaApiKey(key: String) {
        readerPreferences.nvidiaApiKey().set(key)
        updateState { it.copy(nvidiaApiKey = key) }
    }

    fun setNvidiaModel(model: String) {
        readerPreferences.nvidiaModel().set(model)
        updateState { it.copy(nvidiaModel = model) }
    }

    fun setOllamaUrl(url: String) {
        readerPreferences.ollamaServerUrl().set(url)
        updateState { it.copy(ollamaUrl = url) }
    }

    fun setOllamaModel(model: String) {
        readerPreferences.ollamaModel().set(model)
        updateState { it.copy(ollamaModel = model) }
    }

    fun setContentType(type: ContentType) {
        readerPreferences.translatorContentType().set(type.ordinal)
        updateState { it.copy(contentType = type) }
    }

    fun setToneType(tone: ToneType) {
        readerPreferences.translatorToneType().set(tone.ordinal)
        updateState { it.copy(toneType = tone) }
    }

    fun setPreserveStyle(preserve: Boolean) {
        readerPreferences.translatorPreserveStyle().set(preserve)
        updateState { it.copy(preserveStyle = preserve) }
    }

    fun setCustomPrompt(prompt: String) {
        readerPreferences.translationCustomPrompt().set(prompt)
        updateState { it.copy(customPrompt = prompt) }
    }

    fun toggleAutoTranslateChapters(enabled: Boolean) {
        updateState { it.copy(autoTranslateChapters = enabled) }
    }

    fun toggleAutoTranslateNovelNames(enabled: Boolean) {
        translationPreferences.autoTranslateNovelNames().set(enabled)
        updateState { it.copy(autoTranslateNovelNames = enabled) }
    }

    fun toggleAutoShareTranslations(enabled: Boolean) {
        communityPreferences?.autoShareTranslations()?.set(enabled)
        updateState { it.copy(autoShareTranslations = enabled) }
    }

    fun setContributorName(name: String) {
        communityPreferences?.contributorName()?.set(name)
        updateState { it.copy(contributorName = name) }
    }

    fun refreshGeminiModels() {
        scope.launch {
            updateState { it.copy(isRefreshingGeminiModels = true, modelRefreshMessage = null) }
            try {
                val engines = translationEnginesManager?.getAvailableEngines() ?: emptyList()
                val geminiSource = engines.find { source ->
                    when (source) {
                        is TranslationEngineSource.BuiltIn -> source.engine.id == 8L
                        else -> false
                    }
                }
                val geminiEngine = (geminiSource as? TranslationEngineSource.BuiltIn)?.engine as? WebscrapingTranslateEngine
                if (geminiEngine != null && state.value.geminiApiKey.isNotBlank()) {
                    val result = geminiEngine.fetchAvailableGeminiModels(state.value.geminiApiKey)
                    if (result.isSuccess) {
                        val models = result.getOrNull() ?: emptyList()
                        updateState {
                            it.copy(
                                geminiModels = models,
                                isRefreshingGeminiModels = false,
                                modelRefreshMessage = "Successfully loaded ${models.size} models"
                            )
                        }
                    } else {
                        updateState {
                            it.copy(
                                isRefreshingGeminiModels = false,
                                modelRefreshMessage = "Failed to fetch models: ${result.exceptionOrNull()?.message}"
                            )
                        }
                    }
                } else {
                    updateState {
                        it.copy(
                            isRefreshingGeminiModels = false,
                            modelRefreshMessage = "Please enter your Gemini API key first"
                        )
                    }
                }
            } catch (e: Exception) {
                updateState {
                    it.copy(
                        isRefreshingGeminiModels = false,
                        modelRefreshMessage = "Error: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadOpenRouterModels() {
        scope.launch {
            updateState { it.copy(isLoadingOpenRouterModels = true) }
            try {
                val engines = translationEnginesManager?.getAvailableEngines() ?: emptyList()
                val openRouterSource = engines.find { source ->
                    when (source) {
                        is TranslationEngineSource.BuiltIn -> source.engine.id == 9L
                        else -> false
                    }
                }
                val openRouterEngine = (openRouterSource as? TranslationEngineSource.BuiltIn)?.engine as? OpenRouterTranslateEngine
                if (openRouterEngine != null && state.value.openRouterApiKey.isNotBlank()) {
                    val result = openRouterEngine.fetchAvailableModels()
                    if (result.isSuccess) {
                        val models = result.getOrNull() ?: emptyList()
                        updateState {
                            it.copy(
                                openRouterModels = models,
                                isLoadingOpenRouterModels = false
                            )
                        }
                    } else {
                        updateState { it.copy(isLoadingOpenRouterModels = false) }
                    }
                } else {
                    updateState { it.copy(isLoadingOpenRouterModels = false) }
                }
            } catch (e: Exception) {
                updateState { it.copy(isLoadingOpenRouterModels = false) }
            }
        }
    }

    fun loadNvidiaModels() {
        scope.launch {
            updateState { it.copy(isLoadingNvidiaModels = true) }
            try {
                val engines = translationEnginesManager?.getAvailableEngines() ?: emptyList()
                val nvidiaSource = engines.find { source ->
                    when (source) {
                        is TranslationEngineSource.BuiltIn -> source.engine.id == 10L
                        else -> false
                    }
                }
                val nvidiaEngine = (nvidiaSource as? TranslationEngineSource.BuiltIn)?.engine as? NvidiaTranslateEngine
                if (nvidiaEngine != null && state.value.nvidiaApiKey.isNotBlank()) {
                    val result = nvidiaEngine.fetchAvailableModels()
                    if (result.isSuccess) {
                        val models = result.getOrNull() ?: emptyList()
                        updateState {
                            it.copy(
                                nvidiaModels = models,
                                isLoadingNvidiaModels = false
                            )
                        }
                    } else {
                        updateState { it.copy(isLoadingNvidiaModels = false) }
                    }
                } else {
                    updateState { it.copy(isLoadingNvidiaModels = false) }
                }
            } catch (e: Exception) {
                updateState { it.copy(isLoadingNvidiaModels = false) }
            }
        }
    }


    fun testConnection() {
        scope.launch {
            updateState { it.copy(testConnectionState = TestConnectionState.Testing) }
            val manager = translationEnginesManager
            if (manager == null) {
                updateState { it.copy(testConnectionState = TestConnectionState.Error("Translation manager unavailable")) }
                return@launch
            }

            try {
                val engine = manager.get()
                val testText = listOf("Hello")
                manager.translateWithContext(
                    texts = testText,
                    source = "en",
                    target = "es",
                    contentType = state.value.contentType,
                    toneType = state.value.toneType,
                    preserveStyle = state.value.preserveStyle,
                    onProgress = { },
                    onSuccess = { result ->
                        if (result.isNotEmpty()) {
                            updateState { it.copy(testConnectionState = TestConnectionState.Success("Connection successful! Translated: ${result.first()}")) }
                        } else {
                            updateState { it.copy(testConnectionState = TestConnectionState.Error("Empty response received")) }
                        }
                    },
                    onError = { error ->
                        val msg = if (localizeHelper != null) error.asString(localizeHelper) else "Error"
                        updateState { it.copy(testConnectionState = TestConnectionState.Error(msg)) }
                    }
                )
            } catch (e: Exception) {
                updateState { it.copy(testConnectionState = TestConnectionState.Error(e.message ?: "Unknown error")) }
            }
        }
    }

    fun resetTestConnectionState() {
        updateState { it.copy(testConnectionState = TestConnectionState.Idle) }
    }

    fun initializeGoogleMlKit(sourceLanguage: String, targetLanguage: String) {
        scope.launch {
            updateState { it.copy(mlKitInitState = MlKitInitState.Initializing, mlKitInitProgress = 0) }
            try {
                val engines = translationEnginesManager?.getAvailableEngines() ?: emptyList()
                val mlKitSource = engines.find { source ->
                    when (source) {
                        is TranslationEngineSource.BuiltIn -> source.engine.id == 0L
                        else -> false
                    }
                }
                val mlKitEngine = (mlKitSource as? TranslationEngineSource.BuiltIn)?.engine
                if (mlKitEngine == null) {
                    updateState { it.copy(mlKitInitState = MlKitInitState.Error("Google ML Kit engine not found")) }
                    return@launch
                }

                mlKitEngine.initialize(
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage,
                    onProgress = { progress -> updateState { it.copy(mlKitInitProgress = progress) } },
                    onSuccess = { msg -> updateState { it.copy(mlKitInitState = MlKitInitState.Success(msg)) } },
                    onError = { error ->
                        val msg = if (localizeHelper != null) error.asString(localizeHelper) else "ML Kit error"
                        updateState { it.copy(mlKitInitState = MlKitInitState.Error(msg)) }
                    }
                )
            } catch (e: Exception) {
                updateState { it.copy(mlKitInitState = MlKitInitState.Error(e.message ?: "Unknown error")) }
            }
        }
    }

    fun resetMlKitInitState() {
        updateState { it.copy(mlKitInitState = MlKitInitState.Idle, mlKitInitProgress = 0) }
    }

    // ==================== Glossary & Text Cleanup ====================

    fun togglePreset(presetId: String, enabled: Boolean) {
        updateState {
            it.copy(
                activePresets = it.activePresets.map { preset ->
                    if (preset.id == presetId) preset.copy(isEnabled = enabled) else preset
                }
            )
        }
    }

    fun setGlossarySearch(query: String) {
        updateState { it.copy(glossarySearchQuery = query) }
    }

    fun addGlossaryTerm(source: String, target: String, notes: String = "") {
        if (source.isBlank() || target.isBlank()) return
        val newItem = GlossaryTermItem(
            id = ireader.core.util.currentTimeMillis(),
            sourceTerm = source.trim(),
            targetTerm = target.trim(),
            notes = notes.trim()
        )
        updateState {
            it.copy(
                glossaryTerms = it.glossaryTerms + newItem,
                showAddGlossaryDialog = false
            )
        }
    }

    fun deleteGlossaryTerm(id: Long) {
        updateState {
            it.copy(glossaryTerms = it.glossaryTerms.filterNot { term -> term.id == id })
        }
    }

    fun setShowAddGlossaryDialog(show: Boolean) {
        updateState { it.copy(showAddGlossaryDialog = show) }
    }

    fun setShowAddRuleDialog(show: Boolean) {
        updateState { it.copy(showAddRuleDialog = show) }
    }

    fun addCustomReplacement(findText: String, replaceText: String, isRegex: Boolean = false) {
        if (findText.isBlank()) return
        scope.launch {
            textReplacementUseCase?.addReplacement(
                name = findText,
                findText = findText,
                replaceText = replaceText
            )
            loadReplacements()
            updateState { it.copy(showAddRuleDialog = false) }
        }
    }

    fun deleteCustomReplacement(id: Long) {
        scope.launch {
            textReplacementUseCase?.deleteReplacement(id)
            loadReplacements()
        }
    }
}

