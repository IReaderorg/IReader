package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.TranslationSettingsScreen
import ireader.presentation.ui.settings.general.TranslationSettingsViewModel
import kotlinx.coroutines.launch

/**
 * Modern Translation Screen Specification
 * 
 * Features:
 * - Material3 design with proper theming
 * - Pull-to-refresh for reloading translation engines
 * - Animated transitions
 * - Proper state management with StateFlow
 * - Snackbar for user feedback
 */
class TranslationScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel = getIViewModel<TranslationSettingsViewModel>()
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val scope = rememberCoroutineScope()
        
        // State management
        val snackbarHostState = remember { SnackbarHostState() }
        var isRefreshing by remember { mutableStateOf(false) }
        val pullToRefreshState = rememberPullToRefreshState()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        
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

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = localizeHelper.localize(Res.string.translations),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        TopAppBarBackButton(onClick = { navController.popBackStack() })
                    },
                    actions = {
                        // Refresh button to reload translation engines
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isRefreshing = true
                                    // Trigger refresh of translation engines
                                    viewModel.refreshEngines()
                                    isRefreshing = false
                                    snackbarHostState.showSnackbar(
                                        localizeHelper.localize(Res.string.translation_engines_refreshed)
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = localizeHelper.localize(Res.string.refresh)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            }
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        viewModel.refreshEngines()
                        isRefreshing = false
                    }
                },
                state = pullToRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { -it / 4 },
                    exit = fadeOut() + slideOutVertically { -it / 4 }
                ) {
                    TranslationSettingsScreen(
                        modifier = Modifier.fillMaxSize(),
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
                        translationEnginesManager = viewModel.translationEnginesManager,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
