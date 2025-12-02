package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.general.TranslationSettingsViewModel
import ireader.presentation.ui.settings.translation.TranslationSettingsScreenV2

/**
 * Modern Translation Screen Specification
 * 
 * Features:
 * - Material3 design with proper theming
 * - Gemini-first engine selection
 * - Organized sections for API keys, model selection, and advanced settings
 * - Proper state management
 * - Snackbar for user feedback
 */
class TranslationScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel = getIViewModel<TranslationSettingsViewModel>()
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        
        // State management for snackbar feedback
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Collect test connection state for snackbar feedback
        val testState = viewModel.testConnectionState
        
        // Show snackbar on test connection result
        LaunchedEffect(testState) {
            when (testState) {
                is ireader.presentation.ui.settings.general.TestConnectionState.Success -> {
                    snackbarHostState.showSnackbar(testState.message)
                }
                is ireader.presentation.ui.settings.general.TestConnectionState.Error -> {
                    snackbarHostState.showSnackbar(testState.message)
                }
                else -> {}
            }
        }

        // Use the new improved Translation Settings Screen V2
        TranslationSettingsScreenV2(
            viewModel = viewModel,
            translationEnginesManager = viewModel.translationEnginesManager,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToLogin = { loginType ->
                // Handle navigation to login screens
                when (loginType) {
                    "chatgpt" -> {
                        // Navigate to ChatGPT login
                        navController.navigate(ChatGptLoginScreenSpec())
                    }
                    "deepseek" -> {
                        // Navigate to DeepSeek login
                        navController.navigate(DeepSeekLoginScreenSpec())
                    }
                }
            }
        )
    }
}
