package ireader.presentation.ui.home.tts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ireader.domain.plugins.RequiredPluginChecker
import ireader.domain.services.tts_service.DesktopTTSService
import ireader.domain.services.tts_service.PiperVoiceService
import ireader.domain.services.tts_service.PiperVoiceDownloader
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res
import ireader.presentation.ui.core.theme.currentOrThrow

/**
 * Desktop implementation of TTS Engine Settings Screen
 * 
 * Shows the TTS Engine Manager with options to:
 * - View installed engines (Piper, Kokoro, Maya)
 * - Install/uninstall engines
 * - Test engines
 * - Configure Gradio TTS server
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TTSEngineSettingsScreen(
    isDesktop: Boolean,
    onDismiss: () -> Unit,
    onNavigateToTTSManager: () -> Unit
) {
    val ttsService: DesktopTTSService = koinInject()
    val requiredPluginChecker: RequiredPluginChecker = koinInject()
    val scope = rememberCoroutineScope()
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    
    // Observe Piper plugin availability as a Flow (reacts to plugin loading)
    val isPiperPluginAvailable by requiredPluginChecker.observePiperTTSAvailability()
        .collectAsState(initial = requiredPluginChecker.isPiperTTSAvailable())
    
    // Engine status - Piper requires plugin on desktop
    var piperAvailable by remember { mutableStateOf(false) }
    var piperNativeLibraryFailed by remember { mutableStateOf(false) }
    var kokoroAvailable by remember { mutableStateOf(false) }
    var mayaAvailable by remember { mutableStateOf(false) }
    var currentEngine by remember { mutableStateOf("") }
    
    // Check engine status when plugin availability changes
    LaunchedEffect(isPiperPluginAvailable) {
        // Piper requires the plugin to be installed AND native library to load
        val synthInitialized = ttsService.synthesizer.isInitialized()
        piperAvailable = isPiperPluginAvailable && synthInitialized
        // Check if plugin is installed but native library failed
        piperNativeLibraryFailed = isPiperPluginAvailable && !ireader.domain.services.tts_service.piper.PiperJNISynthesizer.isPiperReady()
        kokoroAvailable = ttsService.kokoroAvailable
        mayaAvailable = ttsService.mayaAvailable
        currentEngine = ttsService.getCurrentEngine().name
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.85f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.tts_engine_manager),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Current engine info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = localizeHelper.localize(Res.string.current_engine),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = currentEngine,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // State for voice selection dialog
                var showVoiceSelection by remember { mutableStateOf(false) }
                
                // Engine list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Piper TTS with voice management
                    PiperEngineCard(
                        isAvailable = piperAvailable,
                        isPiperPluginInstalled = isPiperPluginAvailable,
                        isNativeLibraryFailed = piperNativeLibraryFailed,
                        isCurrentEngine = currentEngine == "PIPER",
                        onSelect = {
                            // Check if plugin is installed first
                            if (!isPiperPluginAvailable) {
                                // Request plugin installation and dismiss dialog so RequiredPluginHandler shows
                                requiredPluginChecker.requestPiperTTS()
                                onDismiss()
                                return@PiperEngineCard
                            }
                            // Don't allow selection if native library failed
                            if (piperNativeLibraryFailed) {
                                return@PiperEngineCard
                            }
                            scope.launch {
                                ttsService.setEngine(ireader.domain.services.tts_service.DesktopTTSService.TTSEngine.PIPER)
                                currentEngine = "PIPER"
                            }
                        },
                        onManageVoices = {
                            // Check if plugin is installed first
                            if (!isPiperPluginAvailable) {
                                // Request plugin installation and dismiss dialog so RequiredPluginHandler shows
                                requiredPluginChecker.requestPiperTTS()
                                onDismiss()
                                return@PiperEngineCard
                            }
                            // Don't allow voice management if native library failed
                            if (piperNativeLibraryFailed) {
                                return@PiperEngineCard
                            }
                            showVoiceSelection = true
                        },
                        onInstallPlugin = {
                            // Dismiss dialog so RequiredPluginHandler shows
                            requiredPluginChecker.requestPiperTTS()
                            onDismiss()
                        }
                    )
                    
                    // Voice Selection Dialog
                    if (showVoiceSelection) {
                        TTSVoiceSelectionScreen(
                            isDesktop = true,
                            onDismiss = { showVoiceSelection = false }
                        )
                    }
                    
                    // Kokoro TTS
                    EngineCard(
                        name = "Kokoro TTS",
                        description = "Premium neural TTS with natural-sounding voices",
                        isAvailable = kokoroAvailable,
                        isCurrentEngine = currentEngine == "KOKORO",
                        onSelect = {
                            scope.launch {
                                ttsService.setEngine(ireader.domain.services.tts_service.DesktopTTSService.TTSEngine.KOKORO)
                                currentEngine = "KOKORO"
                            }
                        }
                    )
                    
                    // Maya TTS
                    EngineCard(
                        name = "Maya TTS",
                        description = "Multilingual neural TTS from Maya Research",
                        isAvailable = mayaAvailable,
                        isCurrentEngine = currentEngine == "MAYA",
                        onSelect = {
                            scope.launch {
                                ttsService.setEngine(ireader.domain.services.tts_service.DesktopTTSService.TTSEngine.MAYA)
                                currentEngine = "MAYA"
                            }
                        }
                    )
                    
                    // Gradio TTS (Online - Generic)
                    var gradioAvailable by remember { mutableStateOf(ttsService.gradioAvailable) }
                    var showGradioConfig by remember { mutableStateOf(false) }
                    var activeGradioConfigName by remember { mutableStateOf(ttsService.activeGradioConfig?.name) }
                    
                    EngineCard(
                        name = "Gradio TTS (Online)",
                        description = if (activeGradioConfigName != null) 
                            "Using: $activeGradioConfigName" 
                        else 
                            "Support for any Gradio-based TTS (Persian, Edge, XTTS, etc.)",
                        isAvailable = gradioAvailable,
                        isCurrentEngine = currentEngine == "GRADIO",
                        onSelect = {
                            if (gradioAvailable) {
                                scope.launch {
                                    ttsService.setEngine(ireader.domain.services.tts_service.DesktopTTSService.TTSEngine.GRADIO)
                                    currentEngine = "GRADIO"
                                }
                            } else {
                                showGradioConfig = true
                            }
                        }
                    )
                    
                    // Gradio Configuration Dialog
                    if (showGradioConfig) {
                        GradioConfigDialog(
                            ttsService = ttsService,
                            appPrefs = koinInject(),
                            onDismiss = { showGradioConfig = false },
                            onConfigured = {
                                // Update availability state
                                gradioAvailable = ttsService.gradioAvailable
                                activeGradioConfigName = ttsService.activeGradioConfig?.name
                                showGradioConfig = false

                                // Automatically select the engine after configuration
                                if (ttsService.gradioAvailable) {
                                    scope.launch {
                                        ttsService.setEngine(ireader.domain.services.tts_service.DesktopTTSService.TTSEngine.GRADIO)
                                        currentEngine = "GRADIO"
                                    }
                                }
                            }
                        )
                    }
                }
                
                // Help text
                Text(
                    text = localizeHelper.localize(Res.string.note_some_engines_require_installation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun EngineCard(
    name: String,
    description: String,
    isAvailable: Boolean,
    isCurrentEngine: Boolean,
    onSelect: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    OutlinedCard(
        onClick = { if (isAvailable) onSelect() },
        modifier = Modifier.fillMaxWidth(),
        enabled = isAvailable,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isCurrentEngine) 
                MaterialTheme.colorScheme.secondaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Icon(
                imageVector = when {
                    isCurrentEngine -> Icons.Default.CheckCircle
                    isAvailable -> Icons.Default.RadioButtonUnchecked
                    else -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = when {
                    isCurrentEngine -> MaterialTheme.colorScheme.primary
                    isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.error
                }
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isAvailable) {
                    Text(
                        text = localizeHelper.localize(Res.string.not_installed),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (isCurrentEngine) {
                AssistChip(
                    onClick = {},
                    label = { Text(localizeHelper.localize(Res.string.active_downloads)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        labelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

/**
 * Special Engine Card for Piper TTS with voice management button
 * Piper requires the plugin to be installed on desktop
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PiperEngineCard(
    isAvailable: Boolean,
    isPiperPluginInstalled: Boolean,
    isNativeLibraryFailed: Boolean,
    isCurrentEngine: Boolean,
    onSelect: () -> Unit,
    onManageVoices: () -> Unit,
    onInstallPlugin: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    OutlinedCard(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isNativeLibraryFailed, // Disable if native library failed
        colors = CardDefaults.outlinedCardColors(
            containerColor = when {
                isCurrentEngine -> MaterialTheme.colorScheme.secondaryContainer
                isNativeLibraryFailed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status icon
                Icon(
                    imageVector = when {
                        isNativeLibraryFailed -> Icons.Default.Error
                        isCurrentEngine -> Icons.Default.CheckCircle
                        isPiperPluginInstalled -> Icons.Default.RadioButtonUnchecked
                        else -> Icons.Default.Download
                    },
                    contentDescription = null,
                    tint = when {
                        isNativeLibraryFailed -> MaterialTheme.colorScheme.error
                        isCurrentEngine -> MaterialTheme.colorScheme.primary
                        isPiperPluginInstalled -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.tertiary
                    }
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.piper_tts),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.high_performance_neural_tts_with),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Show status based on plugin installation and native library
                    Text(
                        text = when {
                            isNativeLibraryFailed -> "Native library failed - missing VC++ Runtime"
                            isPiperPluginInstalled && isAvailable -> localizeHelper.localize(Res.string.pre_installed)
                            isPiperPluginInstalled -> "Plugin installed - download a voice"
                            else -> "Plugin required - tap to install"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isNativeLibraryFailed -> MaterialTheme.colorScheme.error
                            isPiperPluginInstalled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                }
                
                if (isCurrentEngine) {
                    AssistChip(
                        onClick = {},
                        label = { Text(localizeHelper.localize(Res.string.active_downloads)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            
            // Show appropriate button based on state
            when {
                isNativeLibraryFailed -> {
                    // Show error message and help link
                    Text(
                        text = "Piper requires Visual C++ Redistributable. Please install it from Microsoft and restart the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                !isPiperPluginInstalled -> {
                    Button(
                        onClick = onInstallPlugin,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Install Piper Plugin")
                    }
                }
                else -> {
                    OutlinedButton(
                        onClick = onManageVoices,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizeHelper.localize(Res.string.manage_piper_voices))
                    }
                }
            }
        }
    }
}


/**
 * Desktop implementation of TTS Voice Selection Screen
 * 
 * Uses the unified PiperVoiceService for voice catalog management.
 * Voices are fetched from https://rhasspy.github.io/piper-samples/voices.json
 * and stored in the local database for offline access.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TTSVoiceSelectionScreen(
    isDesktop: Boolean,
    onDismiss: () -> Unit
) {
    val ttsService: DesktopTTSService = koinInject()
    val voiceService: ireader.domain.services.tts_service.PiperVoiceService = koinInject()
    val voiceDownloader: PiperVoiceDownloader = koinInject()
    val appPrefs: ireader.domain.preferences.prefs.AppPreferences = koinInject()
    val scope = rememberCoroutineScope()
    
    // Collect state from unified service
    val voices by voiceService.subscribeAll().collectAsState(initial = emptyList())
    val isRefreshing by voiceService.isRefreshing.collectAsState()
    val refreshError by voiceService.refreshError.collectAsState()
    
    // Local state - load from preferences directly for persistence
    var selectedVoice by remember { mutableStateOf<String?>(appPrefs.selectedPiperModel().get().takeIf { it.isNotEmpty() }) }
    var currentEngine by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var downloadingVoiceId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    var filterLanguage by remember { mutableStateOf<String?>(null) }
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    
    // Initialize voice service and load current engine
    LaunchedEffect(Unit) {
        currentEngine = ttsService.getCurrentEngine().name
        // Load selected voice from preferences (more reliable than state)
        val savedVoiceId = appPrefs.selectedPiperModel().get()
        if (savedVoiceId.isNotEmpty()) {
            selectedVoice = savedVoiceId
            ireader.core.log.Log.info { "TTSVoiceSelectionScreen: Loaded saved voice from prefs: $savedVoiceId" }
        } else {
            // Fallback to state if prefs empty
            selectedVoice = ttsService.state.selectedVoiceModel?.id
        }
        
        ireader.core.log.Log.info { "TTSVoiceSelectionScreen: Loading voices for engine $currentEngine, selected: $selectedVoice" }
        
        // Initialize voice service (fetches from remote if needed)
        voiceService.initialize()
        isLoading = false
    }
    
    // Get unique languages for filter
    val availableLanguages = remember(voices) {
        voices.map { it.language }.distinct().sorted()
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight(0.95f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Compact header with close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.piper_voice_selection),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                // Use the enhanced unified voice selection UI
                PiperVoiceSelectionContent(
                    voices = voices,
                    selectedVoiceId = selectedVoice,
                    isLoading = isLoading,
                    isRefreshing = isRefreshing,
                    refreshError = refreshError ?: downloadError,
                    downloadingVoiceId = downloadingVoiceId,
                    downloadProgress = downloadProgress,
                    filterLanguage = filterLanguage,
                    availableLanguages = availableLanguages,
                    onVoiceSelect = { voice ->
                        scope.launch {
                            ttsService.selectVoiceModel(voice.id)
                            selectedVoice = voice.id
                        }
                    },
                    onVoiceDownload = { voice ->
                        scope.launch {
                            downloadingVoiceId = voice.id
                            downloadProgress = 0f
                            downloadError = null
                            
                            try {
                                voiceDownloader.downloadVoice(voice) { progress ->
                                    downloadProgress = progress
                                }
                                voiceService.markAsDownloaded(voice.id)
                                ireader.core.log.Log.info { "Voice ${voice.id} downloaded successfully" }
                            } catch (e: Exception) {
                                downloadError = "Download failed: ${e.message}"
                                ireader.core.log.Log.error { "Voice download failed: ${e.message}" }
                            } finally {
                                downloadingVoiceId = null
                            }
                        }
                    },
                    onRefresh = { scope.launch { voiceService.refresh() } },
                    onFilterLanguageChange = { filterLanguage = it },
                    onDismissError = { downloadError = null },
                    onVoiceDelete = { voice ->
                        scope.launch {
                            try {
                                voiceDownloader.deleteVoice(voice.id)
                                voiceService.markAsNotDownloaded(voice.id)
                            } catch (e: Exception) {
                                downloadError = "Delete failed: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Dialog for configuring Gradio TTS
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoquiConfigDialog(
    ttsService: DesktopTTSService,
    appPrefs: ireader.domain.preferences.prefs.AppPreferences,
    onDismiss: () -> Unit,
    onConfigured: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var spaceUrl by remember { mutableStateOf(appPrefs.activeGradioConfigId().get()) }
    var apiKey by remember { mutableStateOf(appPrefs.gradioTTSConfigs().get()) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.7f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = localizeHelper.localize(Res.string.configure_gradio_tts),
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Text(
                    text = localizeHelper.localize(Res.string.gradio_tts_uses_a_huggingface),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                HorizontalDivider()
                
                // Space URL
                OutlinedTextField(
                    value = spaceUrl,
                    onValueChange = { spaceUrl = it },
                    label = { Text(localizeHelper.localize(Res.string.huggingface_space_url)) },
                    placeholder = { Text("https://x-ireader.hf.space") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // API Key (optional)
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(localizeHelper.localize(Res.string.api_key_optional)) },
                    placeholder = { Text(localizeHelper.localize(Res.string.for_private_spaces)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Test result
                if (testResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (testResult!!.startsWith("✓"))
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = testResult!!,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(localizeHelper.localize(Res.string.cancel))
                    }
                    
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isTesting = true
                                testResult = null
                                try {
                                    // Configure and test
                                    ttsService.configureGradioFromPreferences()
                                    if (ttsService.gradioAvailable) {
                                        testResult = "✓ Connection successful!"
                                    } else {
                                        testResult = "✗ Failed to connect. Check URL."
                                    }
                                } catch (e: Exception) {
                                    testResult = "✗ Error: ${e.message}"
                                } finally {
                                    isTesting = false
                                }
                            }
                        },
                        enabled = spaceUrl.isNotEmpty() && !isTesting,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(localizeHelper.localize(Res.string.test))
                        }
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                // Save settings
                                appPrefs.activeGradioConfigId().set(spaceUrl)
                                appPrefs.gradioTTSConfigs().set(apiKey)
                                
                                // Configure engine
                                ttsService.configureGradioFromPreferences()
                                
                                onConfigured()
                            }
                        },
                        enabled = spaceUrl.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(localizeHelper.localize(Res.string.save))
                    }
                }
            }
        }
    }
}


/**
 * Dialog for configuring Gradio TTS with preset selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GradioConfigDialog(
    ttsService: DesktopTTSService,
    appPrefs: ireader.domain.preferences.prefs.AppPreferences,
    onDismiss: () -> Unit,
    onConfigured: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val gradioTTSManager: ireader.domain.services.tts_service.GradioTTSManager = koinInject()
    // Get all configs including plugin configs
    val presets = remember { gradioTTSManager.getAllConfigs() }
    var selectedPresetId by remember { mutableStateOf(appPrefs.activeGradioConfigId().get().ifEmpty { presets.firstOrNull()?.id ?: "" }) }
    var customSpaceUrl by remember { mutableStateOf("") }
    var customApiName by remember { mutableStateOf("/predict") }
    var useCustom by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = localizeHelper.localize(Res.string.configure_gradio_tts),
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Text(
                    text = localizeHelper.localize(Res.string.select_a_preset_tts_engine),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                HorizontalDivider()
                
                // Preset/Custom toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !useCustom,
                        onClick = { useCustom = false },
                        label = { Text(localizeHelper.localize(Res.string.presets)) }
                    )
                    FilterChip(
                        selected = useCustom,
                        onClick = { useCustom = true },
                        label = { Text(localizeHelper.localize(Res.string.custom)) }
                    )
                }
                
                if (!useCustom) {
                    // Preset selection
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            OutlinedCard(
                                onClick = { selectedPresetId = preset.id },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = if (selectedPresetId == preset.id)
                                        MaterialTheme.colorScheme.secondaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedPresetId == preset.id,
                                        onClick = { selectedPresetId = preset.id }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.name,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = preset.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = preset.spaceUrl,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Custom configuration
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = customSpaceUrl,
                            onValueChange = { customSpaceUrl = it },
                            label = { Text(localizeHelper.localize(Res.string.space_url)) },
                            placeholder = { Text("https://username-space.hf.space") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = customApiName,
                            onValueChange = { customApiName = it },
                            label = { Text(localizeHelper.localize(Res.string.api_name)) },
                            placeholder = { Text(localizeHelper.localize(Res.string.predict_or_synthesize_speech)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Text(
                            text = localizeHelper.localize(Res.string.tip_check_the_api_tab),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Test result
                if (testResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (testResult!!.startsWith("✓"))
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = testResult!!,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(localizeHelper.localize(Res.string.cancel))
                    }
                    
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isTesting = true
                                testResult = null
                                try {
                                    val config = if (useCustom) {
                                        ireader.domain.services.tts_service.GradioTTSConfig(
                                            id = "custom_${System.currentTimeMillis()}",
                                            name = "Custom TTS",
                                            spaceUrl = customSpaceUrl,
                                            apiName = customApiName,
                                            isCustom = true
                                        )
                                    } else {
                                        gradioTTSManager.getConfigByIdOrPreset(selectedPresetId)
                                    }
                                    
                                    if (config != null) {
                                        ttsService.configureGradio(config)
                                        if (ttsService.gradioAvailable) {
                                            testResult = "✓ Configuration successful!"
                                        } else {
                                            testResult = "✗ Failed to configure. Check settings."
                                        }
                                    } else {
                                        testResult = "✗ Invalid configuration"
                                    }
                                } catch (e: Exception) {
                                    testResult = "✗ Error: ${e.message}"
                                } finally {
                                    isTesting = false
                                }
                            }
                        },
                        enabled = (useCustom && customSpaceUrl.isNotEmpty()) || (!useCustom && selectedPresetId.isNotEmpty()),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(localizeHelper.localize(Res.string.test))
                        }
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                val config = if (useCustom) {
                                    ireader.domain.services.tts_service.GradioTTSConfig(
                                        id = "custom_${System.currentTimeMillis()}",
                                        name = "Custom TTS",
                                        spaceUrl = customSpaceUrl,
                                        apiName = customApiName,
                                        isCustom = true
                                    )
                                } else {
                                    gradioTTSManager.getConfigByIdOrPreset(selectedPresetId)
                                }
                                
                                if (config != null) {
                                    // Save settings
                                    appPrefs.useGradioTTS().set(true)
                                    appPrefs.activeGradioConfigId().set(config.id)
                                    
                                    // Configure engine
                                    ttsService.configureGradio(config)
                                    
                                    onConfigured()
                                }
                            }
                        },
                        enabled = (useCustom && customSpaceUrl.isNotEmpty()) || (!useCustom && selectedPresetId.isNotEmpty()),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(localizeHelper.localize(Res.string.save))
                    }
                }
            }
        }
    }
}
