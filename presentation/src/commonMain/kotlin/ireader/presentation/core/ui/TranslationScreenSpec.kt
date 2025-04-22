package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.TranslationSettingsScreen
import ireader.presentation.ui.settings.general.TranslationSettingsViewModel
import ireader.i18n.resources.MR

class TranslationScreenSpec : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = getIViewModel<TranslationSettingsViewModel>()
        val localizeHelper = LocalLocalizeHelper.currentOrThrow

        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localizeHelper.localize(MR.strings.translations),
                    scrollBehavior = scrollBehavior,
                    popBackStack = { navigator.pop() }
                )
            }
        ) { padding ->
            TranslationSettingsScreen(
                translatorEngine = viewModel.translatorEngine.value,
                onTranslatorEngineChange = { viewModel.updateTranslatorEngine(it) },
                openAIApiKey = viewModel.openAIApiKey,
                onOpenAIApiKeyChange = { viewModel.updateOpenAIApiKey(it) },
                deepSeekApiKey = viewModel.deepSeekApiKey,
                onDeepSeekApiKeyChange = { viewModel.updateDeepSeekApiKey(it) },
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