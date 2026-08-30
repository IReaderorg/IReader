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

        TranslationSuiteScreen(
            state = state,
            onNavigateUp = { navController.safePopBackStack() },
            onSelectEngine = { viewModel.setEngine(it) },
            onSelectLanguage = { viewModel.setTargetLanguage(it) },
            onApiKeyChange = { viewModel.setApiKey(it) },
            onToggleAutoTranslateChapters = { viewModel.toggleAutoTranslateChapters(it) },
            onToggleAutoTranslateNovelNames = { viewModel.toggleAutoTranslateNovelNames(it) },
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
