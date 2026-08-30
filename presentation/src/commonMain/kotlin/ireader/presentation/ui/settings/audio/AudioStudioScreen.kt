package ireader.presentation.ui.settings.audio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioStudioScreen(
    state: AudioStudioState,
    onNavigateUp: () -> Unit,
    onSelectEngine: (AudioEngineType) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onSpeechPitchChange: (Float) -> Unit,
    onVoiceChange: (String) -> Unit,
    onToggleAutoNext: (Boolean) -> Unit,
    onToggleAutoScroll: (Boolean) -> Unit,
    onToggleSkipBlankLines: (Boolean) -> Unit,
    onSleepTimerChange: (Int) -> Unit,
    onTogglePlaySample: () -> Unit,
    onResetRateAndPitch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Voices & Engines", "Playback", "Highlighting")

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = "Audio & Voice Studio",
                popBackStack = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Interactive Voice Test Bench Card
            item {
                VoiceTestBenchCard(
                    state = state,
                    onTogglePlaySample = onTogglePlaySample
                )
            }

            // 2. Tab Navigation
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Tab 0: Engines & Voices
                    item {
                        EngineSelectionSection(
                            selectedEngine = state.selectedEngine,
                            onSelectEngine = onSelectEngine
                        )
                    }

                    item {
                        VoiceAndSpeedSection(
                            state = state,
                            onSpeechRateChange = onSpeechRateChange,
                            onSpeechPitchChange = onSpeechPitchChange,
                            onVoiceChange = onVoiceChange,
                            onResetRateAndPitch = onResetRateAndPitch
                        )
                    }
                }
                1 -> {
                    // Tab 1: Playback Controls
                    item {
                        PlaybackControlsSection(
                            state = state,
                            onToggleAutoNext = onToggleAutoNext,
                            onToggleAutoScroll = onToggleAutoScroll,
                            onToggleSkipBlankLines = onToggleSkipBlankLines,
                            onSleepTimerChange = onSleepTimerChange
                        )
                    }
                }
                2 -> {
                    // Tab 2: Highlighting & Visuals
                    item {
                        HighlightingPreviewSection(state = state)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun VoiceTestBenchCard(
    state: AudioStudioState,
    onTogglePlaySample: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (state.isPlayingSample) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeMute,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice Test Bench",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (state.selectedEngine) {
                            AudioEngineType.DEVICE_TTS -> "Device TTS"
                            AudioEngineType.PIPER_NEURAL -> "Neural (Piper)"
                            AudioEngineType.GRADIO_AI -> "Cloud AI"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${state.sampleText}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Speed: ${((state.speechRate * 10).toInt() / 10f)}x | Pitch: ${((state.speechPitch * 10).toInt() / 10f)}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )

                Button(
                    onClick = onTogglePlaySample,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isPlayingSample) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (state.isPlayingSample) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (state.isPlayingSample) "Stop" else "Test Voice")
                }
            }
        }
    }
}

@Composable
private fun EngineSelectionSection(
    selectedEngine: AudioEngineType,
    onSelectEngine: (AudioEngineType) -> Unit
) {
    Text(
        text = "Synthesis Engine",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedEngine == AudioEngineType.DEVICE_TTS,
            onClick = { onSelectEngine(AudioEngineType.DEVICE_TTS) },
            label = { Text("Device TTS") },
            leadingIcon = { Icon(Icons.Outlined.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = selectedEngine == AudioEngineType.PIPER_NEURAL,
            onClick = { onSelectEngine(AudioEngineType.PIPER_NEURAL) },
            label = { Text("Piper Neural") },
            leadingIcon = { Icon(Icons.Outlined.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.weight(1f)
        )

        FilterChip(
            selected = selectedEngine == AudioEngineType.GRADIO_AI,
            onClick = { onSelectEngine(AudioEngineType.GRADIO_AI) },
            label = { Text("Cloud AI") },
            leadingIcon = { Icon(Icons.Outlined.Cloud, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun VoiceAndSpeedSection(
    state: AudioStudioState,
    onSpeechRateChange: (Float) -> Unit,
    onSpeechPitchChange: (Float) -> Unit,
    onVoiceChange: (String) -> Unit,
    onResetRateAndPitch: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Voice Selector
            Text("Voice Model", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(state.selectedVoiceName, style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Speech Rate Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Speech Speed", style = MaterialTheme.typography.titleSmall)
                Text("${((state.speechRate * 10).toInt() / 10f)}x", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = state.speechRate,
                onValueChange = onSpeechRateChange,
                valueRange = 0.5f..3.0f,
                steps = 24
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Speech Pitch Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pitch", style = MaterialTheme.typography.titleSmall)
                Text("${((state.speechPitch * 10).toInt() / 10f)}x", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = state.speechPitch,
                onValueChange = onSpeechPitchChange,
                valueRange = 0.5f..2.0f,
                steps = 14
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onResetRateAndPitch,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Speed & Pitch (1.0x)")
            }
        }
    }
}

@Composable
private fun PlaybackControlsSection(
    state: AudioStudioState,
    onToggleAutoNext: (Boolean) -> Unit,
    onToggleAutoScroll: (Boolean) -> Unit,
    onToggleSkipBlankLines: (Boolean) -> Unit,
    onSleepTimerChange: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Auto Next
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleAutoNext(!state.autoNextChapter) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Next Chapter", style = MaterialTheme.typography.bodyLarge)
                    Text("Seamlessly continue playback into next chapter", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.autoNextChapter, onCheckedChange = onToggleAutoNext)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Auto Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleAutoScroll(!state.autoScrollWithSpeech) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Scroll with Speech", style = MaterialTheme.typography.bodyLarge)
                    Text("Keep currently spoken paragraph centered in reader", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.autoScrollWithSpeech, onCheckedChange = onToggleAutoScroll)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Skip Blank Lines
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleSkipBlankLines(!state.skipBlankLines) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Skip Blank Paragraphs", style = MaterialTheme.typography.bodyLarge)
                    Text("Instantly jump over empty lines without pauses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.skipBlankLines, onCheckedChange = onToggleSkipBlankLines)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Sleep Timer
            Text("Sleep Timer", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0 to "Off", 15 to "15m", 30 to "30m", 45 to "45m", 60 to "60m").forEach { (mins, label) ->
                    FilterChip(
                        selected = state.sleepTimerMinutes == mins,
                        onClick = { onSleepTimerChange(mins) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HighlightingPreviewSection(state: AudioStudioState) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Paragraph Highlighting Preview", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "When TTS reads chapters, the active paragraph is highlighted with a smooth background glow.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Previous non-active paragraph in the novel...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "▶ \"${state.sampleText}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Next non-active paragraph waiting to be read...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
