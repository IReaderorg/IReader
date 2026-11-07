package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.settings.translation_suite.TranslationSuiteScreen
import ireader.presentation.ui.settings.translation_suite.TranslationSuiteViewModel

/**
 * Modern Translation & Text Suite Screen Specification
 */
class TranslationScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: TranslationSuiteViewModel = getIViewModel()
        val state by viewModel.state.collectAsState()

        // Android 17+ requires ACCESS_LOCAL_NETWORK (Nearby Devices) for LAN
        // endpoints such as http://192.168.x.x:1234/v1 (issue #295). Request
        // it lazily only when a local URL is configured; guarded by SDK check
        // inside the Android actual, no-op elsewhere.
        val requestLocalNetworkPermission = rememberLocalNetworkPermissionRequest(
            url = state.openAIBaseUrl.ifBlank { state.ollamaUrl }
        )

        TranslationSuiteScreen(
            state = state,
            onNavigateUp = { navController.safePopBackStack() },
            onSelectEngineId = { viewModel.setEngineId(it) },
            onSelectLanguage = { viewModel.setTargetLanguage(it) },
            onRequestLocalNetworkPermission = requestLocalNetworkPermission,
            onOpenAIApiKeyChange = { viewModel.setOpenAIApiKey(it) },
            onOpenAIBaseUrlChange = { viewModel.setOpenAIBaseUrl(it) },
            onOpenAIModelChange = { viewModel.setOpenAIModel(it) },
            onDeepSeekApiKeyChange = { viewModel.setDeepSeekApiKey(it) },
            onGeminiApiKeyChange = { viewModel.setGeminiApiKey(it) },
            onGeminiModelChange = { viewModel.setGeminiModel(it) },
            onRefreshGeminiModels = { viewModel.refreshGeminiModels() },
            onClaudeApiKeyChange = { viewModel.setClaudeApiKey(it) },
            onClaudeModelChange = { viewModel.setClaudeModel(it) },
            onOpenRouterApiKeyChange = { viewModel.setOpenRouterApiKey(it) },
            onOpenRouterModelChange = { viewModel.setOpenRouterModel(it) },
            onLoadOpenRouterModels = { viewModel.loadOpenRouterModels() },
            onNvidiaApiKeyChange = { viewModel.setNvidiaApiKey(it) },
            onNvidiaModelChange = { viewModel.setNvidiaModel(it) },
            onLoadNvidiaModels = { viewModel.loadNvidiaModels() },
            onOllamaUrlChange = { viewModel.setOllamaUrl(it) },
            onOllamaModelChange = { viewModel.setOllamaModel(it) },
            onTestConnection = { viewModel.testConnection() },
            onResetTestConnectionState = { viewModel.resetTestConnectionState() },
            onInitializeGoogleMlKit = { src, tgt -> viewModel.initializeGoogleMlKit(src, tgt) },
            onNavigateToLogin = { loginType ->
                when (loginType) {
                    "chatgpt" -> navController.navigate(ChatGptLoginScreenSpec())
                    "deepseek" -> navController.navigate(DeepSeekLoginScreenSpec())
                }
            },
            onContentTypeChange = { viewModel.setContentType(it) },
            onToneTypeChange = { viewModel.setToneType(it) },
            onPreserveStyleChange = { viewModel.setPreserveStyle(it) },
            onCustomPromptChange = { viewModel.setCustomPrompt(it) },
            onContextSizeChange = { viewModel.setTranslationContextSize(it) },
            onParagraphChunkSizeChange = { viewModel.setTranslationParagraphChunkSize(it) },
            onToggleAutoTranslateChapters = { viewModel.toggleAutoTranslateChapters(it) },
            onToggleAutoTranslateNovelNames = { viewModel.toggleAutoTranslateNovelNames(it) },
            onToggleAutoShareTranslations = { viewModel.toggleAutoShareTranslations(it) },
            onContributorNameChange = { viewModel.setContributorName(it) },
            onTogglePreset = { id, enabled -> viewModel.togglePreset(id, enabled) },
            onGlossarySearch = { viewModel.setGlossarySearch(it) },
            onAddGlossaryTerm = { src, tgt, notes -> viewModel.addGlossaryTerm(src, tgt, notes) },
            onDeleteGlossaryTerm = { viewModel.deleteGlossaryTerm(it) },
            onSetShowAddGlossaryDialog = { viewModel.setShowAddGlossaryDialog(it) },
            onSetShowAddRuleDialog = { viewModel.setShowAddRuleDialog(it) },
            onAddCustomReplacement = { find, replace, regex -> viewModel.addCustomReplacement(find, replace, regex) },
            onDeleteCustomReplacement = { viewModel.deleteCustomReplacement(it) }
        )
    }
}

