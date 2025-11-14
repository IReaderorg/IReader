package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.TranslationSettingsScreen
import ireader.presentation.ui.settings.general.TranslationSettingsViewModel

class TranslationScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel = getIViewModel<TranslationSettingsViewModel>()
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }

        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localizeHelper.localize(Res.string.translations),
                    scrollBehavior = scrollBehavior,
                    popBackStack = { navController.popBackStack() }
                )
            }
        ) { padding ->
            TranslationSettingsScreen(modifier = Modifier.padding(padding),
                translatorEngine = viewModel.translatorEngine.value,
                onTranslatorEngineChange = { viewModel.updateTranslatorEngine(it) },
                openAIApiKey = viewModel.openAIApiKey,
                onOpenAIApiKeyChange = { viewModel.updateOpenAIApiKey(it) },
                deepSeekApiKey = viewModel.deepSeekApiKey,
                onDeepSeekApiKeyChange = { viewModel.updateDeepSeekApiKey(it) },
                geminiApiKey = viewModel.geminiApiKey,
                onGeminiApiKeyChange = { viewModel.updateGeminiApiKey(it) },
                geminiModel = viewModel.geminiModel,
                onGeminiModelChange = { viewModel.updateGeminiModel(it) },
                translatorContentType = viewModel.translatorContentType,
                onTranslatorContentTypeChange = { viewModel.updateTranslatorContentType(it) },
                translatorToneType = viewModel.translatorToneType.value,
                onTranslatorToneTypeChange = { viewModel.updateTranslatorToneType(it) },
                translatorPreserveStyle = viewModel.translatorPreserveStyle,
                onTranslatorPreserveStyleChange = { viewModel.updateTranslatorPreserveStyle(it) },
                ollamaUrl = viewModel.ollamaUrl,
                onOllamaUrlChange = { viewModel.updateOllamaUrl(it) },
                ollamaModel = viewModel.ollamaModel,
                onOllamaModelChange = { viewModel.updateOllamaModel(it) },
                translationEnginesManager = viewModel.translationEnginesManager
            )
        }
    }
} 