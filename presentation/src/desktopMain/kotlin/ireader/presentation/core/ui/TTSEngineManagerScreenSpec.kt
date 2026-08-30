package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.settings.audio.AudioStudioScreen
import ireader.presentation.ui.settings.audio.AudioStudioViewModel

/**
 * Desktop implementation of Audio & Voice Studio Screen
 */
actual class TTSEngineManagerScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    actual fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: AudioStudioViewModel = getIViewModel()
        val state by viewModel.state.collectAsState()

        AudioStudioScreen(
            state = state,
            onNavigateUp = { navController.safePopBackStack() },
            onSelectEngine = { viewModel.setEngine(it) },
            onSpeechRateChange = { viewModel.setSpeechRate(it) },
            onSpeechPitchChange = { viewModel.setSpeechPitch(it) },
            onVoiceChange = { viewModel.setVoice(it) },
            onToggleAutoNext = { viewModel.toggleAutoNext(it) },
            onToggleAutoScroll = { viewModel.toggleAutoScroll(it) },
            onToggleSkipBlankLines = { viewModel.toggleSkipBlankLines(it) },
            onSleepTimerChange = { viewModel.setSleepTimer(it) },
            onTogglePlaySample = { viewModel.togglePlaySample() },
            onResetRateAndPitch = { viewModel.resetRateAndPitch() }
        )
    }
}
