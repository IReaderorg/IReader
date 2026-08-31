package ireader.presentation.ui.settings.audio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeMute
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.tts.PiperVoice
import ireader.domain.services.platform.PlatformType
import ireader.domain.services.tts_service.GradioTTSConfig
import ireader.i18n.resources.*
import ireader.i18n.resources.Res
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.tts.PiperVoiceSelectionContent
import ireader.presentation.ui.settings.components.GradioConfigEditDialog
import ireader.presentation.ui.settings.components.GradioTTSSection
import ireader.presentation.ui.settings.components.TTSMergeAndCacheSection
import ireader.presentation.ui.settings.viewmodels.TestResult

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
    // Cloud Callbacks
    onSelectCloudConfig: (String) -> Unit = {},
    onTestCloudConfig: (String) -> Unit = {},
    onOpenEditCloudDialog: (GradioTTSConfig?) -> Unit = {},
    onDismissEditCloudDialog: () -> Unit = {},
    onSaveCloudConfig: (GradioTTSConfig) -> Unit = {},
    onDeleteCloudConfig: (String) -> Unit = {},
    onClearCloudTestResult: () -> Unit = {},
    // Piper Callbacks (Desktop)
    onFilterPiperLanguage: (String?) -> Unit = {},
    onSelectPiperVoice: (PiperVoice) -> Unit = {},
    onDownloadPiperVoice: (PiperVoice) -> Unit = {},
    onDeletePiperVoice: (PiperVoice) -> Unit = {},
    onRefreshPiperVoices: () -> Unit = {},
    // Caching Callbacks
    onMergeWordsRemoteChange: (Int) -> Unit = {},
    onMergeWordsNativeChange: (Int) -> Unit = {},
    onChapterCacheEnabledChange: (Boolean) -> Unit = {},
    onChapterCacheDaysChange: (Int) -> Unit = {},
    onClearChapterCache: () -> Unit = {},
    onNavigateToFeatureStore: () -> Unit = {},
    onAutoDetectCloudSpace: ((String, String?, (Result<GradioTTSConfig>) -> Unit) -> Unit)? = null,
    onTestCustomCloudConfig: ((GradioTTSConfig, (Result<ByteArray>) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Voices & Engines", "Cloud Models", "Playback & Cache", "Highlighting")
    val snackbarHostState = remember { SnackbarHostState() }
    var showPiperCatalogDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.cloudTestResult) {
        when (val result = state.cloudTestResult) {
            is TestResult.Success -> {
                snackbarHostState.showSnackbar("Cloud TTS connection successful!")
                onClearCloudTestResult()
            }
            is TestResult.Error -> {
                snackbarHostState.showSnackbar("Cloud TTS Error: ${result.message}")
                onClearCloudTestResult()
            }
            null -> {}
        }
    }

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = "Audio & Voice Studio",
                popBackStack = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                            state = state,
                            onSelectEngine = onSelectEngine
                        )
                    }

                    // Engine-specific options
                    when (state.selectedEngine) {
                        AudioEngineType.GRADIO_AI -> {
                            item {
                                CloudVoiceSelectionCard(
                                    state = state,
                                    onSelectCloudConfig = onSelectCloudConfig,
                                    onOpenCloudManager = { selectedTab = 1 }
                                )
                            }
                        }
                        AudioEngineType.PIPER_NEURAL -> {
                            if (state.platformType == PlatformType.DESKTOP) {
                                item {
                                    DesktopPiperSelectionCard(
                                        state = state,
                                        onOpenCatalog = { showPiperCatalogDialog = true },
                                        onSelectPiperVoice = onSelectPiperVoice
                                    )
                                }
                            }
                        }
                        AudioEngineType.DEVICE_TTS -> {
                            if (state.platformType == PlatformType.ANDROID) {
                                item {
                                    AndroidSherpaRecommendationCard()
                                }
                            }
                        }
                        AudioEngineType.KOKORO_NEURAL -> {
                            item {
                                KokoroNeuralCard()
                            }
                        }
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
                    // Tab 1: Cloud Models (Gradio Manager & Feature Store Plugins)
                    item {
                        FeatureStoreTTSPluginsCard(
                            plugins = state.installedTTSPlugins,
                            onOpenFeatureStore = onNavigateToFeatureStore
                        )
                    }
                    item {
                        GradioTTSSection(
                            useGradioTTS = state.selectedEngine == AudioEngineType.GRADIO_AI,
                            onUseGradioTTSChange = { enabled ->
                                onSelectEngine(if (enabled) AudioEngineType.GRADIO_AI else {
                                    if (state.platformType == PlatformType.DESKTOP) AudioEngineType.PIPER_NEURAL else AudioEngineType.DEVICE_TTS
                                })
                            },
                            configs = state.cloudConfigs,
                            activeConfigId = state.activeCloudConfigId,
                            onSelectConfig = onSelectCloudConfig,
                            onTestConfig = onTestCloudConfig,
                            onEditConfig = onOpenEditCloudDialog,
                            onDeleteConfig = onDeleteCloudConfig,
                            onAddCustomConfig = { onOpenEditCloudDialog(null) },
                            globalSpeed = state.speechRate,
                            onGlobalSpeedChange = onSpeechRateChange,
                            isTesting = state.isTestingCloudConfig,
                            testingConfigId = state.activeCloudConfigId
                        )
                    }
                }
                2 -> {
                    // Tab 2: Playback & Cache
                    item {
                        PlaybackControlsSection(
                            state = state,
                            onToggleAutoNext = onToggleAutoNext,
                            onToggleAutoScroll = onToggleAutoScroll,
                            onToggleSkipBlankLines = onToggleSkipBlankLines,
                            onSleepTimerChange = onSleepTimerChange
                        )
                    }

                    item {
                        TTSMergeAndCacheSection(
                            mergeWordsRemote = state.mergeWordsRemote,
                            onMergeWordsRemoteChange = onMergeWordsRemoteChange,
                            mergeWordsNative = state.mergeWordsNative,
                            onMergeWordsNativeChange = onMergeWordsNativeChange,
                            chapterCacheEnabled = state.chapterCacheEnabled,
                            onChapterCacheEnabledChange = onChapterCacheEnabledChange,
                            chapterCacheDays = state.chapterCacheDays,
                            onChapterCacheDaysChange = onChapterCacheDaysChange,
                            cacheEntryCount = state.cacheEntryCount,
                            cacheSizeMB = state.cacheSizeMB,
                            onClearCache = onClearChapterCache
                        )
                    }
                }
                3 -> {
                    // Tab 3: Highlighting & Visuals
                    item {
                        HighlightingPreviewSection(state = state)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Custom Cloud TTS Engine Studio Screen / Overlay
    AnimatedVisibility(
        visible = state.isEditCloudDialogOpen && state.editingCloudConfig != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        state.editingCloudConfig?.let { config ->
            CustomTTSEngineScreen(
                config = config,
                onDismiss = onDismissEditCloudDialog,
                onSave = onSaveCloudConfig,
                onAutoDetect = onAutoDetectCloudSpace,
                onTestCustomConfig = onTestCustomCloudConfig
            )
        }
    }

    // Piper Catalog Dialog (Desktop)
    if (showPiperCatalogDialog) {
        AlertDialog(
            onDismissRequest = { showPiperCatalogDialog = false },
            title = { Text("Piper Neural Voice Catalog") },
            text = {
                Box(modifier = Modifier.fillMaxWidth().height(450.dp)) {
                    PiperVoiceSelectionContent(
                        voices = state.piperVoices,
                        selectedVoiceId = state.piperVoices.find { it.name == state.selectedVoiceName }?.id,
                        isLoading = false,
                        isRefreshing = state.isRefreshingPiperVoices,
                        refreshError = state.piperErrorMessage,
                        downloadingVoiceId = state.downloadingPiperVoiceId,
                        downloadProgress = state.piperDownloadProgress,
                        filterLanguage = state.selectedPiperLanguage,
                        availableLanguages = state.availablePiperLanguages,
                        onVoiceSelect = { voice ->
                            onSelectPiperVoice(voice)
                            showPiperCatalogDialog = false
                        },
                        onVoiceDownload = onDownloadPiperVoice,
                        onRefresh = onRefreshPiperVoices,
                        onFilterLanguageChange = onFilterPiperLanguage,
                        onDismissError = {},
                        onVoiceDelete = onDeletePiperVoice
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPiperCatalogDialog = false }) {
                    Text("Done")
                }
            }
        )
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
                        imageVector = if (state.isPlayingSample) Icons.AutoMirrored.Outlined.VolumeUp else Icons.AutoMirrored.Outlined.VolumeMute,
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
                            AudioEngineType.KOKORO_NEURAL -> "Neural (Kokoro)"
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
    state: AudioStudioState,
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
        state.availableEngines.forEach { engine ->
            val isSelected = state.selectedEngine == engine
            FilterChip(
                selected = isSelected,
                onClick = { onSelectEngine(engine) },
                label = {
                    Text(
                        when (engine) {
                            AudioEngineType.DEVICE_TTS -> "Device TTS"
                            AudioEngineType.PIPER_NEURAL -> "Piper Neural"
                            AudioEngineType.KOKORO_NEURAL -> "Kokoro Neural"
                            AudioEngineType.GRADIO_AI -> "Cloud AI"
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        when (engine) {
                            AudioEngineType.DEVICE_TTS -> Icons.Outlined.PhoneAndroid
                            AudioEngineType.PIPER_NEURAL -> Icons.Outlined.Psychology
                            AudioEngineType.KOKORO_NEURAL -> Icons.AutoMirrored.Outlined.VolumeUp
                            AudioEngineType.GRADIO_AI -> Icons.Outlined.Cloud

                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CloudVoiceSelectionCard(
    state: AudioStudioState,
    onSelectCloudConfig: (String) -> Unit,
    onOpenCloudManager: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Active Cloud Model", style = MaterialTheme.typography.titleSmall)
                }
                TextButton(onClick = onOpenCloudManager) {
                    Text("Manage Spaces")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(state.cloudConfigs) { config ->
                    val isSelected = config.id == state.activeCloudConfigId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCloudConfig(config.id) },
                        label = { Text(config.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopPiperSelectionCard(
    state: AudioStudioState,
    onOpenCatalog: () -> Unit,
    onSelectPiperVoice: (PiperVoice) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Piper Neural Voices (Desktop)", style = MaterialTheme.typography.titleSmall)
                }
                Button(onClick = onOpenCatalog) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Browse Catalog")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            val downloadedVoices = remember(state.piperVoices) { state.piperVoices.filter { it.isDownloaded } }

            if (downloadedVoices.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(downloadedVoices) { voice ->
                        val isSelected = voice.name == state.selectedVoiceName
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectPiperVoice(voice) },
                            label = { Text("${voice.name} (${voice.language})") }
                        )
                    }
                }
            } else {
                Text(
                    "No Piper models downloaded yet. Click Browse Catalog to download high-quality offline voices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AndroidSherpaRecommendationCard() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = localizeHelper.localize(Res.string.recommended_sherpa_tts_app),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = localizeHelper.localize(Res.string.for_more_powerful_and_natural),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = localizeHelper.localize(Res.string.high_quality_neural_voicesn_works),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun KokoroNeuralCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Kokoro ONNX Neural Engine",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                text = "Next-generation 82M open weights neural text-to-speech engine running locally on desktop ONNX runtime.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
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
            Text("Selected Voice / Model", style = MaterialTheme.typography.titleSmall)
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

@Composable
private fun FeatureStoreTTSPluginsCard(
    plugins: List<ireader.domain.plugins.PluginInfo>,
    onOpenFeatureStore: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically, 
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Voice Plugins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                FilledTonalIconButton(
                    onClick = onOpenFeatureStore
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Browse Feature Store")
                }
            }

            if (plugins.isEmpty()) {
                Text(
                    text = "Install cloud TTS voice models and neural engines directly from the Feature Store to expand your studio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = "${plugins.size} voice plugin(s) installed and active in cloud synthesis:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                plugins.forEach { plugin ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(plugin.manifest.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("v${plugin.manifest.version} • ${plugin.manifest.description}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


