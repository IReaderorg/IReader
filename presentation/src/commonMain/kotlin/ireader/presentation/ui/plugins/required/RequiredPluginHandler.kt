package ireader.presentation.ui.plugins.required

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ireader.domain.plugins.RequiredPluginChecker
import org.koin.compose.koinInject

/**
 * Composable that handles showing the required plugin screen when needed.
 * Place this at the root of your app to automatically show the full screen when
 * a required plugin is requested.
 */
/**
 * Get the appropriate JS engine plugin type for the current platform.
 * Android uses J2V8, Desktop uses GraalVM.
 */
expect fun getPlatformJSEngineType(): RequiredPluginType

@Composable
fun RequiredPluginHandler(
    requiredPluginChecker: RequiredPluginChecker = koinInject(),
    viewModel: RequiredPluginViewModel = koinInject()
) {
    val jsEngineRequired by requiredPluginChecker.jsEngineRequired.collectAsState()
    val piperTTSRequired by requiredPluginChecker.piperTTSRequired.collectAsState()
    
    // Get platform-specific JS engine type
    val jsEngineType = getPlatformJSEngineType()
    
    // Show JS Engine full screen
    AnimatedVisibility(
        visible = jsEngineRequired,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        RequiredPluginFullScreen(
            pluginType = jsEngineType,
            featureName = "JavaScript Sources",
            onDismiss = { requiredPluginChecker.clearJSEngineRequest() },
            onPluginReady = { requiredPluginChecker.clearJSEngineRequest() },
            viewModel = viewModel
        )
    }
    
    // Show Piper TTS full screen
    AnimatedVisibility(
        visible = piperTTSRequired,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        RequiredPluginFullScreen(
            pluginType = RequiredPluginType.PIPER_TTS,
            featureName = "Piper Text-to-Speech",
            onDismiss = { requiredPluginChecker.clearPiperTTSRequest() },
            onPluginReady = { requiredPluginChecker.clearPiperTTSRequest() },
            viewModel = viewModel
        )
    }
}

/**
 * Full screen wrapper for RequiredPluginScreen
 * Shows a complete screen overlay when a required plugin is needed
 */
@Composable
fun RequiredPluginFullScreen(
    pluginType: RequiredPluginType,
    featureName: String,
    onDismiss: () -> Unit,
    onPluginReady: () -> Unit,
    viewModel: RequiredPluginViewModel
) {
    LaunchedEffect(pluginType) {
        viewModel.initialize(pluginType)
    }
    
    val state by viewModel.state
    
    // Auto-dismiss when plugin is ready
    LaunchedEffect(state.isInstalled, state.isEnabled) {
        if (state.isInstalled && state.isEnabled) {
            onPluginReady()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        RequiredPluginScreen(
            state = state,
            featureName = featureName,
            onInstall = { viewModel.installPlugin() },
            onEnable = { viewModel.enablePlugin() },
            onDismiss = onDismiss,
            onRetry = { viewModel.retry() }
        )
    }
}

/**
 * State holder for required plugin UI
 */
class RequiredPluginUiState {
    var showJSEngineDialog by mutableStateOf(false)
    var showPiperTTSDialog by mutableStateOf(false)
    var showGraalVMDialog by mutableStateOf(false)
    
    fun showJSEngine() {
        showJSEngineDialog = true
    }
    
    fun showPiperTTS() {
        showPiperTTSDialog = true
    }
    
    fun showGraalVM() {
        showGraalVMDialog = true
    }
    
    fun dismissAll() {
        showJSEngineDialog = false
        showPiperTTSDialog = false
        showGraalVMDialog = false
    }
}

/**
 * Remember a RequiredPluginUiState
 */
@Composable
fun rememberRequiredPluginUiState(): RequiredPluginUiState {
    return remember { RequiredPluginUiState() }
}

/**
 * Composable that shows required plugin dialogs based on the UI state.
 * Use this when you want manual control over when to show the dialogs.
 */
@Composable
fun RequiredPluginDialogs(
    uiState: RequiredPluginUiState,
    viewModel: RequiredPluginViewModel = koinInject()
) {
    if (uiState.showJSEngineDialog) {
        RequiredPluginDialog(
            pluginType = RequiredPluginType.JS_ENGINE,
            featureName = "JavaScript Sources",
            onDismiss = { uiState.showJSEngineDialog = false },
            onPluginReady = { uiState.showJSEngineDialog = false },
            viewModel = viewModel
        )
    }
    
    if (uiState.showGraalVMDialog) {
        RequiredPluginDialog(
            pluginType = RequiredPluginType.GRAALVM_ENGINE,
            featureName = "JavaScript Sources",
            onDismiss = { uiState.showGraalVMDialog = false },
            onPluginReady = { uiState.showGraalVMDialog = false },
            viewModel = viewModel
        )
    }
    
    if (uiState.showPiperTTSDialog) {
        RequiredPluginDialog(
            pluginType = RequiredPluginType.PIPER_TTS,
            featureName = "Piper Text-to-Speech",
            onDismiss = { uiState.showPiperTTSDialog = false },
            onPluginReady = { uiState.showPiperTTSDialog = false },
            viewModel = viewModel
        )
    }
}

/**
 * Check if JS engine is available and show dialog if not.
 * Returns true if the engine is available, false if dialog was shown.
 */
@Composable
fun checkJSEngineOrShowDialog(
    requiredPluginChecker: RequiredPluginChecker = koinInject()
): () -> Boolean {
    return {
        if (requiredPluginChecker.isJSEngineAvailable()) {
            true
        } else {
            requiredPluginChecker.requestJSEngine()
            false
        }
    }
}

/**
 * Check if Piper TTS is available and show dialog if not.
 * Returns true if available, false if dialog was shown.
 */
@Composable
fun checkPiperTTSOrShowDialog(
    requiredPluginChecker: RequiredPluginChecker = koinInject()
): () -> Boolean {
    return {
        if (requiredPluginChecker.isPiperTTSAvailable()) {
            true
        } else {
            requiredPluginChecker.requestPiperTTS()
            false
        }
    }
}
