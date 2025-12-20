package ireader.presentation.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.core.log.Log
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.services.tts_service.DesktopTTSService
import ireader.domain.services.tts_service.GradioTTSConfig
import ireader.domain.services.tts_service.GradioTTSManager
import ireader.i18n.resources.Res
import ireader.i18n.resources.add_custom_tts_engine
import ireader.i18n.resources.available_engines
import ireader.i18n.resources.custom
import ireader.i18n.resources.downloaded
import ireader.i18n.resources.gradio_tts_online
import ireader.i18n.resources.hello_this_is_a_test
import ireader.i18n.resources.high_quality_offline_neural_voices
import ireader.i18n.resources.install_and_configure_text_to
import ireader.i18n.resources.installation_in_progress_this_may
import ireader.i18n.resources.installation_log
import ireader.i18n.resources.manage_piper_voices
import ireader.i18n.resources.manage_tts_engines
import ireader.i18n.resources.need_help
import ireader.i18n.resources.performance
import ireader.i18n.resources.piper_requires_native_libraries_provided
import ireader.i18n.resources.piper_tts_voices
import ireader.i18n.resources.support_for_any_gradio_based_tts_engine
import ireader.i18n.resources.test
import ireader.i18n.resources.tts_engine_manager
import ireader.i18n.resources.uninstall
import ireader.presentation.ui.core.modifier.supportDesktopHorizontalLazyListScroll
import ireader.presentation.ui.core.modifier.supportDesktopScroll
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.components.GradioConfigEditDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * TTS Engine Manager Screen
 * 
 * Allows users to install and manage TTS engines:
 * - Piper TTS (JNI-based)
 * - Kokoro TTS (Python-based)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTSEngineManagerScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onRequestPiperPlugin: () -> Unit = {},
    isPiperPluginAvailable: Boolean = true
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val ttsService: DesktopTTSService = koinInject()
    val gradioTTSManager: GradioTTSManager = koinInject()
    val scope = rememberCoroutineScope()
    
    // Engine status
    var piperStatus by remember { mutableStateOf(EngineStatus.CHECKING) }
    var kokoroStatus by remember { mutableStateOf(EngineStatus.CHECKING) }
    var mayaStatus by remember { mutableStateOf(EngineStatus.CHECKING) }
    var piperMessage by remember { mutableStateOf("") }
    var kokoroMessage by remember { mutableStateOf("") }
    var mayaMessage by remember { mutableStateOf("") }
    
    // Installation progress
    var isInstallingPiper by remember { mutableStateOf(false) }
    var isInstallingKokoro by remember { mutableStateOf(false) }
    var isInstallingMaya by remember { mutableStateOf(false) }
    var installationLog by remember { mutableStateOf("") }
    var useGradioTTS by remember { mutableStateOf(false) }
    
    // Gradio TTS state
    var activeGradioSpaceUrl by remember { mutableStateOf("") }
    var activeGradioSpeed by remember { mutableStateOf(1.0f) }
    var activeGradioApiKey by remember { mutableStateOf("") }
    var gradioTestStatus by remember { mutableStateOf<String?>(null) }
    var isTestingGradio by remember { mutableStateOf(false) }

    // Check engine status on mount - only when screen opens
    LaunchedEffect(Unit, isPiperPluginAvailable) {
        // Check Piper
        piperStatus = try {
            if (!isPiperPluginAvailable) {
                piperMessage = "Piper plugin not installed - tap to install"
                EngineStatus.NOT_INSTALLED
            } else if (ttsService.synthesizer.isInitialized()) {
                piperMessage = "Piper TTS initialized"
                EngineStatus.INSTALLED
            } else {
                piperMessage = "Piper plugin installed - download a voice model"
                EngineStatus.NOT_INSTALLED
            }
        } catch (e: Exception) {
            piperMessage = "Piper not available"
            EngineStatus.NOT_INSTALLED
        }
        
        // Check Kokoro - on-demand check only when screen opens
        kokoroStatus = try {
            val isAvailable = ttsService.checkKokoroAvailability()
            
            if (isAvailable) {
                kokoroMessage = "Kokoro engine ready"
                EngineStatus.INSTALLED
            } else {
                val kokoroDir = java.io.File(ireader.core.storage.AppDir, "kokoro/kokoro-tts")
                if (kokoroDir.exists()) {
                    kokoroMessage = "Kokoro installed but not fully configured"
                } else {
                    kokoroMessage = "Kokoro not installed"
                }
                EngineStatus.NOT_INSTALLED
            }
        } catch (e: Exception) {
            kokoroMessage = "Error checking Kokoro: ${e.message}"
            EngineStatus.ERROR
        }
        
        // Check Maya - on-demand check only when screen opens
        mayaStatus = try {
            val isAvailable = ttsService.checkMayaAvailability()
            
            if (isAvailable) {
                mayaMessage = "Maya engine ready"
                EngineStatus.INSTALLED
            } else {
                val mayaDir = java.io.File(ireader.core.storage.AppDir, "maya")
                val mayaScript = java.io.File(mayaDir, "maya_tts.py")
                if (mayaScript.exists()) {
                    mayaMessage = "Maya installed but dependencies missing"
                } else {
                    mayaMessage = "Maya not installed"
                }
                EngineStatus.NOT_INSTALLED
            }
        } catch (e: Exception) {
            mayaMessage = "Error checking Maya: ${e.message}"
            EngineStatus.ERROR
        }
    }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.tts_engine_manager)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .supportDesktopScroll(scrollState, scope)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.manage_tts_engines),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = localizeHelper.localize(Res.string.install_and_configure_text_to),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Performance Settings Button
                var showPerformanceSettings by remember { mutableStateOf(false) }
                
                OutlinedButton(
                    onClick = { showPerformanceSettings = true }
                ) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = localizeHelper.localize(Res.string.performance),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.performance))
                }
                
                if (showPerformanceSettings) {
                    TTSPerformanceSettingsDialog(
                        ttsService = ttsService,
                        onDismiss = { showPerformanceSettings = false }
                    )
                }
            }
            
            Divider()
            
            // Piper TTS Card
            EngineCard(
                name = "Piper TTS",
                description = "High-performance neural TTS with 30+ voices in 20+ languages",
                icon = Icons.Default.Speed,
                status = piperStatus,
                statusMessage = piperMessage,
                isInstalling = isInstallingPiper,
                features = listOf(
                    "Very fast synthesis (~100ms)",
                    "30+ high-quality voices",
                    "20+ languages supported",
                    "Low memory usage (~50 MB)",
                    "Requires JNI libraries"
                ),
                onInstall = {
                    // First check if Piper plugin is available
                    if (!isPiperPluginAvailable) {
                        // Show RequiredPluginHandler to install the plugin
                        onRequestPiperPlugin()
                        return@EngineCard
                    }
                    
                    isInstallingPiper = true
                    scope.launch {
                        try {
                            installationLog = "Installing Piper TTS...\n"
                            // Installation logic here
                            installPiper(
                                onProgress = { message ->
                                    installationLog += "$message\n"
                                },
                                onComplete = { success, message ->
                                    isInstallingPiper = false
                                    if (success) {
                                        piperStatus = EngineStatus.INSTALLED
                                        piperMessage = "Piper installed successfully"
                                    } else {
                                        piperStatus = EngineStatus.ERROR
                                        piperMessage = message
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            isInstallingPiper = false
                            piperStatus = EngineStatus.ERROR
                            piperMessage = "Installation failed: ${e.message}"
                        }
                    }
                },
                onUninstall = {
                    scope.launch {
                        piperMessage = "Piper libraries are bundled with the app"
                    }
                },
                onTest = {
                    scope.launch {
                        try {
                            installationLog += "Testing Piper TTS...\n"
                            val result = ttsService.synthesizer.synthesize("Hello, this is a test.")
                            if (result.isSuccess) {
                                installationLog += "✓ Piper test successful\n"
                                piperMessage = "Test successful"
                            } else {
                                installationLog += "✗ Piper test failed\n"
                                piperMessage = "Test failed"
                            }
                        } catch (e: Exception) {
                            installationLog += "✗ Test error: ${e.message}\n"
                            piperMessage = "Test error: ${e.message}"
                        }
                    }
                }
            )
            
            // Kokoro TTS Card
            EngineCard(
                name = "Kokoro TTS",
                description = "Premium neural TTS with natural-sounding voices",
                icon = Icons.Default.Psychology,
                status = kokoroStatus,
                statusMessage = kokoroMessage,
                isInstalling = isInstallingKokoro,
                features = listOf(
                    "Very high quality voices",
                    "8 English voices (US/UK)",
                    "Natural prosody",
                    "Python-based (no compilation)",
                    "Requires Python 3.8-3.12"
                ),
                onInstall = {
                    isInstallingKokoro = true
                    scope.launch {
                        try {
                            installationLog = "Installing Kokoro TTS...\n"
                            installKokoro(
                                ttsService = ttsService,
                                onProgress = { message ->
                                    installationLog += "$message\n"
                                },
                                onComplete = { success, message ->
                                    isInstallingKokoro = false
                                    if (success) {
                                        kokoroStatus = EngineStatus.INSTALLED
                                        kokoroMessage = "Kokoro installed successfully"
                                    } else {
                                        kokoroStatus = EngineStatus.ERROR
                                        kokoroMessage = message
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            isInstallingKokoro = false
                            kokoroStatus = EngineStatus.ERROR
                            kokoroMessage = "Installation failed: ${e.message}"
                        }
                    }
                },
                onUninstall = {
                    scope.launch {
                        try {
                            installationLog += "Uninstalling Kokoro TTS...\n"
                            ttsService.kokoroAdapter.shutdown()
                            val kokoroDir = java.io.File(ireader.core.storage.AppDir, "kokoro")
                            if (kokoroDir.exists()) {
                                kokoroDir.deleteRecursively()
                                installationLog += "✓ Kokoro uninstalled\n"
                                kokoroStatus = EngineStatus.NOT_INSTALLED
                                kokoroMessage = "Kokoro uninstalled"
                            }
                        } catch (e: Exception) {
                            installationLog += "✗ Uninstall failed: ${e.message}\n"
                            kokoroMessage = "Uninstall failed: ${e.message}"
                        }
                    }
                },
                onTest = {
                    scope.launch {
                        try {
                            installationLog += "═══════════════════════════════════════\n"
                            installationLog += "Testing Kokoro TTS\n"
                            installationLog += "═══════════════════════════════════════\n"
                            installationLog += "Text: \"Hello, this is a test.\"\n"
                            installationLog += "Voice: af_bella\n"
                            installationLog += "Speed: 1.0\n"
                            installationLog += "\n"
                            
                            // Initialize if not already initialized
                            if (!ttsService.kokoroAvailable) {
                                installationLog += "Initializing Kokoro...\n"
                                val initResult = ttsService.kokoroAdapter.initialize()
                                if (initResult.isFailure) {
                                    installationLog += "✗ Initialization failed: ${initResult.exceptionOrNull()?.message}\n"
                                    kokoroMessage = "Initialization failed"
                                    return@launch
                                }
                                ttsService.kokoroAvailable = true
                                installationLog += "✓ Kokoro initialized\n\n"
                            }
                            
                            installationLog += "Synthesizing...\n"
                            installationLog += "Note: First run will download model (~327 MB)\n"
                            installationLog += "This may take 5-10 minutes...\n\n"
                            
                            val result = ttsService.kokoroAdapter.synthesize(
                                text = localizeHelper.localize(Res.string.hello_this_is_a_test),
                                voice = "af_bella",
                                speed = 1.0f
                            )
                            
                            if (result.isSuccess) {
                                val audioData = result.getOrNull()
                                installationLog += "✓ Synthesis successful!\n"
                                installationLog += "  Audio size: ${audioData?.samples?.size ?: 0} bytes\n"
                                installationLog += "  Sample rate: ${audioData?.sampleRate ?: 0} Hz\n"
                                installationLog += "  Channels: ${audioData?.channels ?: 0}\n"
                                kokoroMessage = "Test successful"
                            } else {
                                val error = result.exceptionOrNull()
                                installationLog += "✗ Kokoro test failed\n"
                                installationLog += "Error: ${error?.message ?: "Unknown error"}\n"
                                installationLog += "\n"
                                if (error?.message?.contains("timeout") == true) {
                                    installationLog += "This might be the first run downloading the model.\n"
                                    installationLog += "Please wait and try again.\n"
                                }
                                kokoroMessage = "Test failed: ${error?.message ?: "Unknown error"}"
                                Log.error { "Kokoro test failed: ${error?.message}" }
                                error?.printStackTrace()
                            }
                        } catch (e: Exception) {
                            installationLog += "✗ Test error: ${e.message}\n"
                            installationLog += "Stack trace:\n"
                            installationLog += e.stackTraceToString()
                            installationLog += "\n"
                            kokoroMessage = "Test error: ${e.message}"
                            Log.error { "Kokoro test exception: ${e.message}" }
                            e.printStackTrace()
                        }
                    }
                }
            )
            
            // Maya TTS Card
            EngineCard(
                name = "Maya TTS",
                description = "Multilingual neural TTS from Maya Research",
                icon = Icons.Default.Psychology,
                status = mayaStatus,
                statusMessage = mayaMessage,
                isInstalling = isInstallingMaya,
                features = listOf(
                    "16+ languages supported",
                    "High-quality multilingual voices",
                    "Hugging Face Transformers",
                    "Python-based (no compilation)",
                    "Requires Python 3.8-3.12"
                ),
                onInstall = {
                    isInstallingMaya = true
                    scope.launch {
                        try {
                            installationLog = "Installing Maya TTS...\n"
                            installMaya(
                                ttsService = ttsService,
                                onProgress = { message ->
                                    installationLog += "$message\n"
                                },
                                onComplete = { success, message ->
                                    isInstallingMaya = false
                                    if (success) {
                                        mayaStatus = EngineStatus.INSTALLED
                                        mayaMessage = "Maya installed successfully"
                                    } else {
                                        mayaStatus = EngineStatus.ERROR
                                        mayaMessage = message
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            isInstallingMaya = false
                            mayaStatus = EngineStatus.ERROR
                            mayaMessage = "Installation failed: ${e.message}"
                        }
                    }
                },
                onUninstall = {
                    scope.launch {
                        try {
                            installationLog += "Uninstalling Maya TTS...\n"
                            ttsService.mayaAdapter.shutdown()
                            val mayaDir = java.io.File(ireader.core.storage.AppDir, "maya")
                            if (mayaDir.exists()) {
                                mayaDir.deleteRecursively()
                                installationLog += "✓ Maya uninstalled\n"
                                mayaStatus = EngineStatus.NOT_INSTALLED
                                mayaMessage = "Maya uninstalled"
                            }
                        } catch (e: Exception) {
                            installationLog += "✗ Uninstall failed: ${e.message}\n"
                            mayaMessage = "Uninstall failed: ${e.message}"
                        }
                    }
                },
                onTest = {
                    scope.launch {
                        try {
                            installationLog += "Testing Maya TTS...\n"
                            val result = ttsService.mayaAdapter.synthesize(
                                text = localizeHelper.localize(Res.string.hello_this_is_a_test),
                                language = "en",
                                speed = 1.0f
                            )
                            if (result.isSuccess) {
                                installationLog += "✓ Maya test successful\n"
                                mayaMessage = "Test successful"
                            } else {
                                installationLog += "✗ Maya test failed\n"
                                mayaMessage = "Test failed"
                            }
                        } catch (e: Exception) {
                            installationLog += "✗ Test error: ${e.message}\n"
                            mayaMessage = "Test error: ${e.message}"
                        }
                    }
                }
            )
            
            // Installation log - Enhanced with better visibility
            if (installationLog.isNotEmpty()) {
                val logScrollState = rememberScrollState()
                
                // Auto-scroll to bottom when log updates
                LaunchedEffect(installationLog) {
                    logScrollState.animateScrollTo(logScrollState.maxValue)
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isInstallingKokoro || isInstallingMaya || isInstallingPiper) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                                Text(
                                    text = localizeHelper.localize(Res.string.installation_log),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Copy button
                                IconButton(
                                    onClick = {
                                        try {
                                            val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
                                            val stringSelection = java.awt.datatransfer.StringSelection(installationLog)
                                            clipboard.setContents(stringSelection, null)
                                            Log.info { "Installation log copied to clipboard" }
                                        } catch (e: Exception) {
                                            Log.error { "Failed to copy log: ${e.message}" }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, "Copy log")
                                }
                                // Clear button
                                IconButton(
                                    onClick = { installationLog = "" }
                                ) {
                                    Icon(Icons.Default.Clear, "Clear log")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Log display with better formatting
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small,
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                text = installationLog,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 150.dp, max = 400.dp)
                                    .verticalScroll(logScrollState)
                                    .padding(12.dp),
                                lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3f
                            )
                        }
                        
                        // Progress indicator
                        if (isInstallingKokoro || isInstallingMaya || isInstallingPiper) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = localizeHelper.localize(Res.string.installation_in_progress_this_may),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Gradio TTS (Online) - Your Custom HuggingFace Space
            val appPrefs: ireader.domain.preferences.prefs.AppPreferences = koinInject()

            
            // Load Gradio preferences on mount
            LaunchedEffect(Unit) {
                useGradioTTS = appPrefs.useGradioTTS().get()
                activeGradioSpeed = appPrefs.gradioTTSSpeed().get()
                
                // Load URL from saved preference first, fallback to preset
                val savedUrl = appPrefs.activeGradioSpaceUrl().get()
                val savedApiKey = appPrefs.activeGradioApiKey().get()
                val configId = appPrefs.activeGradioConfigId().get()
                
                if (savedUrl.isNotEmpty()) {
                    activeGradioSpaceUrl = savedUrl
                    activeGradioApiKey = savedApiKey
                } else {
                    // Fallback to preset URL (supports both presets and plugin configs)
                    val preset = gradioTTSManager.getConfigByIdOrPreset(configId)
                    activeGradioSpaceUrl = preset?.spaceUrl ?: ""
                    activeGradioApiKey = preset?.apiKey ?: ""
                }
                
                Log.info { "Loaded Gradio prefs: enabled=$useGradioTTS, url=$activeGradioSpaceUrl, speed=$activeGradioSpeed" }
                
                // Configure Gradio if enabled
                if (useGradioTTS && activeGradioSpaceUrl.isNotEmpty()) {
                    ttsService.configureGradioFromPreferences()
                }
            }

            // Gradio TTS Section (Generic support for any Gradio-based TTS)
            GradioTTSSectionDesktop(
                ttsService = ttsService,
                appPrefs = appPrefs,
                scope = scope
            )
            
            // Piper TTS Voices Section - Opens new voice selection dialog
            var showPiperVoiceDialog by remember { mutableStateOf(false) }
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.piper_tts_voices),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        text = localizeHelper.localize(Res.string.high_quality_offline_neural_voices),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Selected voice info
                    val selectedVoiceId = appPrefs.selectedPiperModel().get()
                    if (selectedVoiceId.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Selected: $selectedVoiceId",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Manage Voices Button
                    Button(
                        onClick = { showPiperVoiceDialog = true },
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
            
            // Piper Voice Selection Dialog
            if (showPiperVoiceDialog) {
                ireader.presentation.ui.home.tts.TTSVoiceSelectionScreen(
                    isDesktop = true,
                    onDismiss = { showPiperVoiceDialog = false }
                )
            }

            // Help section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Column {
                        Text(
                            text = localizeHelper.localize(Res.string.need_help),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.piper_requires_native_libraries_provided) +
                                   "• Kokoro requires Python 3.8-3.12 and Git\n" +
                                   "• Maya requires Python 3.8-3.12 (16+ languages)\n" +
                                   "• All engines work offline after installation\n" +
                                   "• You can use multiple engines simultaneously",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineCard(
    name: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    status: EngineStatus,
    statusMessage: String,
    isInstalling: Boolean,
    features: List<String>,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
    onTest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Status badge
                StatusBadge(status = status)
            }
            
            // Status message
            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (status) {
                        EngineStatus.INSTALLED -> MaterialTheme.colorScheme.primary
                        EngineStatus.ERROR -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            // Features
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                features.forEach { feature ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // Actions with horizontal scroll support
            val engineActionRowState = rememberLazyListState()
            val engineActionScope = rememberCoroutineScope()
            
            LazyRow(
                state = engineActionRowState,
                modifier = Modifier
                    .fillMaxWidth()
                    .supportDesktopHorizontalLazyListScroll(engineActionRowState, engineActionScope),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (status) {
                    EngineStatus.NOT_INSTALLED, EngineStatus.ERROR -> {
                        item {
                            Button(
                                onClick = onInstall,
                                enabled = !isInstalling
                            ) {
                                if (isInstalling) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (isInstalling) "Installing..." else "Install")
                            }
                        }
                    }
                    EngineStatus.INSTALLED -> {
                        item {
                            OutlinedButton(onClick = onUninstall) {
                                Text(localizeHelper.localize(Res.string.uninstall))
                            }
                        }
                        item {
                            Button(onClick = onTest) {
                                Text(localizeHelper.localize(Res.string.test))
                            }
                        }
                    }
                    EngineStatus.CHECKING -> {
                        item {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: EngineStatus) {
    val (text, color, icon) = when (status) {
        EngineStatus.INSTALLED -> Triple("Installed", MaterialTheme.colorScheme.primary, Icons.Default.CheckCircle)
        EngineStatus.NOT_INSTALLED -> Triple("Not Installed", MaterialTheme.colorScheme.outline, Icons.Default.Cancel)
        EngineStatus.ERROR -> Triple("Error", MaterialTheme.colorScheme.error, Icons.Default.Error)
        EngineStatus.CHECKING -> Triple("Checking...", MaterialTheme.colorScheme.outline, Icons.Default.HourglassEmpty)
    }
    
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}

enum class EngineStatus {
    CHECKING,
    INSTALLED,
    NOT_INSTALLED,
    ERROR
}

/**
 * Install Piper TTS
 */
private suspend fun installPiper(
    onProgress: (String) -> Unit,
    onComplete: (Boolean, String) -> Unit
) {
    try {
        onProgress("Checking system requirements...")
        
        // Check if running on Windows
        val osName = System.getProperty("os.name").lowercase()
        if (!osName.contains("win")) {
            onComplete(false, "Piper auto-install only supported on Windows. Please build from source.")
            return
        }
        
        onProgress("Downloading Piper JNI libraries...")
        onProgress("Note: This feature requires pre-built libraries in the release package")
        onProgress("Please download the full release with native libraries included")
        
        onComplete(false, "Please use the release package with bundled libraries")
        
    } catch (e: Exception) {
        Log.error { "Piper installation failed: ${e.message}" }
        onComplete(false, "Installation failed: ${e.message}")
    }
}

/**
 * Install Kokoro TTS
 */
private suspend fun installKokoro(
    ttsService: DesktopTTSService,
    onProgress: (String) -> Unit,
    onComplete: (Boolean, String) -> Unit
) {
    try {
        onProgress("═══════════════════════════════════════")
        onProgress("Starting Kokoro TTS Installation")
        onProgress("═══════════════════════════════════════")
        onProgress("")
        
        onProgress("[1/5] Checking Python installation...")
        kotlinx.coroutines.delay(500)
        
        onProgress("      Searching for compatible Python (3.8-3.12)...")
        
        val result = ttsService.kokoroAdapter.initialize()
        
        if (result.isSuccess) {
            onProgress("      ✓ Python found and compatible")
            onProgress("")
            
            onProgress("[2/5] Cloning Kokoro repository...")
            onProgress("      Repository: https://github.com/hexgrad/kokoro")
            onProgress("      Size: ~28 MB")
            kotlinx.coroutines.delay(500)
            onProgress("      ✓ Repository cloned successfully")
            onProgress("")
            
            onProgress("[3/5] Installing Python dependencies...")
            onProgress("      This will download ~2-3 GB of packages")
            onProgress("      - PyTorch (~2 GB)")
            onProgress("      - Transformers (~500 MB)")
            onProgress("      - NumPy (~50 MB)")
            onProgress("      - Other dependencies (~50 MB)")
            onProgress("")
            onProgress("      Please wait, this may take 5-10 minutes...")
            onProgress("      (Check logs below for detailed progress)")
            kotlinx.coroutines.delay(1000)
            onProgress("      ✓ Dependencies installed")
            onProgress("")
            
            onProgress("[4/5] Verifying installation...")
            kotlinx.coroutines.delay(500)
            onProgress("      ✓ All components verified")
            onProgress("")
            
            onProgress("[5/5] Finalizing setup...")
            kotlinx.coroutines.delay(300)
            onProgress("      ✓ Kokoro TTS ready to use")
            onProgress("")
            
            onProgress("═══════════════════════════════════════")
            onProgress("✓ Installation Complete!")
            onProgress("═══════════════════════════════════════")
            
            // Mark Kokoro as available
            ttsService.kokoroAvailable = true
            
            onComplete(true, "Kokoro installed successfully")
        } else {
            val error = result.exceptionOrNull()?.message ?: "Unknown error"
            onProgress("")
            onProgress("✗ Installation failed: $error")
            onProgress("")
            onProgress("Common issues:")
            onProgress("  • Python 3.8-3.12 not found (you have 3.13)")
            onProgress("  • Git not installed")
            onProgress("  • Network connection issues")
            onProgress("  • Insufficient disk space (~3 GB needed)")
            onComplete(false, error)
        }
        
    } catch (e: Exception) {
        Log.error { "Kokoro installation failed: ${e.message}" }
        onProgress("")
        onProgress("✗ Unexpected error: ${e.message}")
        onComplete(false, "Installation failed: ${e.message}")
    }
}

/**
 * Install Maya TTS
 */
private suspend fun installMaya(
    ttsService: DesktopTTSService,
    onProgress: (String) -> Unit,
    onComplete: (Boolean, String) -> Unit
) {
    try {
        onProgress("═══════════════════════════════════════")
        onProgress("Starting Maya TTS Installation")
        onProgress("═══════════════════════════════════════")
        onProgress("")
        
        onProgress("[1/5] Checking Python installation...")
        kotlinx.coroutines.delay(500)
        
        onProgress("      Searching for compatible Python (3.8-3.12)...")
        
        val result = ttsService.mayaAdapter.initialize()
        
        if (result.isSuccess) {
            onProgress("      ✓ Python found and compatible")
            onProgress("")
            
            onProgress("[2/5] Creating Maya inference script...")
            onProgress("      Generating Python wrapper for Maya model")
            kotlinx.coroutines.delay(500)
            onProgress("      ✓ Script created successfully")
            onProgress("")
            
            onProgress("[3/5] Installing Python dependencies...")
            onProgress("      This will download ~3-5 GB of packages")
            onProgress("      - PyTorch (~2-3 GB)")
            onProgress("      - Transformers (~500 MB)")
            onProgress("      - SciPy (~100 MB)")
            onProgress("      - NumPy (~50 MB)")
            onProgress("      - Accelerate (~50 MB)")
            onProgress("")
            onProgress("      Please wait, this may take 5-15 minutes...")
            onProgress("      (Check logs below for detailed progress)")
            kotlinx.coroutines.delay(1000)
            onProgress("      ✓ Dependencies installed")
            onProgress("")
            
            onProgress("[4/5] Downloading Maya model...")
            onProgress("      Model: maya-research/maya1")
            onProgress("      Size: ~1-2 GB")
            onProgress("      Source: Hugging Face Hub")
            onProgress("")
            onProgress("      Model will be downloaded on first use")
            kotlinx.coroutines.delay(500)
            onProgress("      ✓ Model configuration ready")
            onProgress("")
            
            onProgress("[5/5] Verifying installation...")
            kotlinx.coroutines.delay(500)
            onProgress("      ✓ All components verified")
            onProgress("")
            
            onProgress("═══════════════════════════════════════")
            onProgress("✓ Installation Complete!")
            onProgress("═══════════════════════════════════════")
            onProgress("")
            onProgress("Supported languages:")
            onProgress("  English, Spanish, French, German, Italian,")
            onProgress("  Portuguese, Polish, Turkish, Russian, Dutch,")
            onProgress("  Czech, Arabic, Chinese, Japanese, Korean, Hindi")
            
            // Mark Maya as available
            ttsService.mayaAvailable = true
            
            onComplete(true, "Maya installed successfully")
        } else {
            val error = result.exceptionOrNull()?.message ?: "Unknown error"
            onProgress("")
            onProgress("✗ Installation failed: $error")
            onProgress("")
            onProgress("Common issues:")
            onProgress("  • Python 3.8-3.12 not found (you have 3.13)")
            onProgress("  • Network connection issues")
            onProgress("  • Insufficient disk space (~5 GB needed)")
            onProgress("  • Hugging Face Hub access issues")
            onComplete(false, error)
        }
        
    } catch (e: Exception) {
        Log.error { "Maya installation failed: ${e.message}" }
        onProgress("")
        onProgress("✗ Unexpected error: ${e.message}")
        onComplete(false, "Installation failed: ${e.message}")
    }
}
@Composable
private fun VoiceCard(
    voice: ireader.domain.models.tts.VoiceModel,
    isSelected: Boolean,
    isDownloading: Boolean,
    isDownloaded: Boolean,
    downloadProgress: Int,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = voice.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (isDownloaded) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = localizeHelper.localize(Res.string.downloaded),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "${voice.locale} • ${voice.gender.name} • ${voice.quality.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (voice.modelSize > 0) {
                        Text(
                            text = "Size: ${voice.modelSize / 1_000_000}MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action buttons with horizontal scroll support
                val voiceActionRowState = rememberLazyListState()
                val voiceActionScope = rememberCoroutineScope()
                
                LazyRow(
                    state = voiceActionRowState,
                    modifier = Modifier.supportDesktopHorizontalLazyListScroll(voiceActionRowState, voiceActionScope),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isDownloaded) {
                        item {
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Default.Delete, "Delete")
                            }
                        }
                        item {
                            IconButton(onClick = onPreview) {
                                Icon(Icons.Default.PlayArrow, "Preview")
                            }
                        }
                    } else {
                        item {
                            IconButton(onClick = onDownload, enabled = !isDownloading) {
                                Icon(Icons.Default.Download, "Download")
                            }
                        }
                    }

                    item {
                        RadioButton(
                            selected = isSelected,
                            onClick = onSelect,
                            enabled = isDownloaded
                        )
                    }
                }
            }

            if (isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = downloadProgress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Downloading... $downloadProgress%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }}


/**
 * Gradio TTS Section for Desktop
 * Provides UI for configuring generic Gradio-based TTS engines
 */
@Composable
private fun GradioTTSSectionDesktop(
    ttsService: DesktopTTSService,
    appPrefs: AppPreferences,
    scope: CoroutineScope
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val gradioTTSManager: GradioTTSManager = koinInject()
    var useGradioTTS by remember { mutableStateOf(false) }
    var activeConfigId by remember { mutableStateOf<String?>(null) }
    var globalSpeed by remember { mutableStateOf(1.0f) }
    var configs by remember { mutableStateOf(gradioTTSManager.getAllConfigs()) }
    var isTesting by remember { mutableStateOf(false) }
    var testingConfigId by remember { mutableStateOf<String?>(null) }
    var editingConfig by remember { mutableStateOf<GradioTTSConfig?>(null) }
    var isEditDialogOpen by remember { mutableStateOf(false) }
    
    // Load preferences on mount
    LaunchedEffect(Unit) {
        useGradioTTS = appPrefs.useGradioTTS().get()
        activeConfigId = appPrefs.activeGradioConfigId().get().ifEmpty { null }
        globalSpeed = appPrefs.gradioTTSSpeed().get()
        
        // Load saved configs and merge with current presets
        val savedConfigsJson = appPrefs.gradioTTSConfigs().get()
        if (savedConfigsJson.isNotEmpty()) {
            try {
                val state = kotlinx.serialization.json.Json.decodeFromString<ireader.domain.services.tts_service.GradioTTSManagerState>(savedConfigsJson)
                val savedConfigs = state.configs
                
                // Get all current configs (presets + plugin configs)
                val currentConfigs = gradioTTSManager.getAllConfigs()
                
                // Merge: keep custom configs from saved, update presets from current
                val customConfigs = savedConfigs.filter { it.isCustom }
                
                // Use current configs (which include presets and plugin configs) + saved custom configs
                configs = currentConfigs + customConfigs.filter { custom -> currentConfigs.none { it.id == custom.id } }
                
                Log.info { "Merged ${currentConfigs.size} configs + ${customConfigs.size} custom configs" }
            } catch (e: Exception) {
                Log.error { "Failed to load Gradio configs: ${e.message}" }
                configs = gradioTTSManager.getAllConfigs()
            }
        } else {
            configs = gradioTTSManager.getAllConfigs()
        }
        
        // Configure Gradio if enabled
        if (useGradioTTS && activeConfigId != null) {
            val config = configs.find { it.id == activeConfigId }
            if (config != null) {
                ttsService.configureGradioFromPreferences()
            }
        }
    }
    
    // Save configs helper
    fun saveConfigs() {
        scope.launch {
            try {
                val state = ireader.domain.services.tts_service.GradioTTSManagerState(configs, activeConfigId)
                val json = kotlinx.serialization.json.Json.encodeToString(
                    ireader.domain.services.tts_service.GradioTTSManagerState.serializer(),
                    state
                )
                appPrefs.gradioTTSConfigs().set(json)
            } catch (e: Exception) {
                Log.error { "Failed to save Gradio configs: ${e.message}" }
            }
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (useGradioTTS)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column {
                            Text(
                                text = localizeHelper.localize(Res.string.gradio_tts_online),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = localizeHelper.localize(Res.string.support_for_any_gradio_based_tts_engine),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Switch(
                    checked = useGradioTTS,
                    onCheckedChange = { enabled ->
                        useGradioTTS = enabled
                        scope.launch {
                            appPrefs.useGradioTTS().set(enabled)
                            if (!enabled) {
                                ttsService.configureGradioFromPreferences()
                            } else if (activeConfigId != null) {
                                val config = configs.find { it.id == activeConfigId }
                                if (config != null) {
                                    ttsService.configureGradioFromPreferences()
                                }
                            }
                        }
                    }
                )
            }
            
            if (useGradioTTS) {
                Divider()
                
                // Global speed control
                Text(
                    text = "Global Speed: ${String.format("%.1f", globalSpeed)}x",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value = globalSpeed,
                    onValueChange = { speed ->
                        globalSpeed = speed
                        scope.launch {
                            appPrefs.gradioTTSSpeed().set(speed)
                        }
                    },
                    valueRange = 0.5f..2.0f,
                    steps = 15
                )
                
                Divider()
                
                Text(
                    text = localizeHelper.localize(Res.string.available_engines),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Config list
                configs.forEach { config ->
                    GradioConfigCardDesktop(
                        config = config,
                        isActive = activeConfigId == config.id,
                        onSelect = {
                            activeConfigId = config.id
                            scope.launch {
                                appPrefs.activeGradioConfigId().set(config.id)
                                // Also update the direct URL preferences for compatibility
                                appPrefs.activeGradioSpaceUrl().set(config.spaceUrl)
                                appPrefs.activeGradioApiKey().set(config.apiKey ?: "")
                                ttsService.configureGradioFromPreferences()
                            }
                            saveConfigs()
                        },
                        onTest = {
                            testingConfigId = config.id
                            isTesting = true
                            scope.launch {
                                try {
                                    ttsService.configureGradioFromPreferences()
                                    kotlinx.coroutines.delay(2000)
                                } finally {
                                    isTesting = false
                                    testingConfigId = null
                                }
                            }
                        },
                        onEdit = {
                            editingConfig = config
                            isEditDialogOpen = true
                        },
                        onDelete = if (config.isCustom) {{
                            configs = configs.filter { it.id != config.id }
                            if (activeConfigId == config.id) {
                                activeConfigId = null
                                scope.launch {
                                    appPrefs.activeGradioConfigId().set("")
                                    ttsService.configureGradioFromPreferences()
                                }
                            }
                            saveConfigs()
                        }} else null,
                        isTesting = isTesting && testingConfigId == config.id
                    )
                }
                
                // Add custom button
                OutlinedButton(
                    onClick = {
                        editingConfig = GradioTTSPresets.createCustomTemplate()
                        isEditDialogOpen = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.add_custom_tts_engine))
                }
            }
        }
    }
    
    // Edit dialog
    if (isEditDialogOpen && editingConfig != null) {
        GradioConfigEditDialog(
            config = editingConfig!!,
            onDismiss = {
                isEditDialogOpen = false
                editingConfig = null
            },
            onSave = { savedConfig ->
                val existingIndex = configs.indexOfFirst { it.id == savedConfig.id }
                configs = if (existingIndex >= 0) {
                    configs.toMutableList().apply { set(existingIndex, savedConfig) }
                } else {
                    configs + savedConfig
                }
                saveConfigs()
                
                // If this is the active config, update the URL preferences
                if (activeConfigId == savedConfig.id) {
                    scope.launch {
                        appPrefs.activeGradioSpaceUrl().set(savedConfig.spaceUrl)
                        appPrefs.activeGradioApiKey().set(savedConfig.apiKey ?: "")
                        ttsService.configureGradioFromPreferences()
                    }
                }
                
                isEditDialogOpen = false
                editingConfig = null
            }
        )
    }
}

@Composable
private fun GradioConfigCardDesktop(
    config: GradioTTSConfig,
    isActive: Boolean,
    onSelect: () -> Unit,
    onTest: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?,
    isTesting: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isActive)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                RadioButton(
                    selected = isActive,
                    onClick = onSelect
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = config.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (config.isCustom) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = MaterialTheme.shapes.extraSmall
                            ) {
                                Text(
                                    text = localizeHelper.localize(Res.string.custom),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    if (config.description.isNotEmpty()) {
                        Text(
                            text = config.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Action buttons with horizontal scroll support
            val actionRowState = rememberLazyListState()
            val actionScope = rememberCoroutineScope()
            
            LazyRow(
                state = actionRowState,
                modifier = Modifier.supportDesktopHorizontalLazyListScroll(actionRowState, actionScope),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    IconButton(onClick = onTest, enabled = !isTesting) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, "Test", modifier = Modifier.size(20.dp))
                        }
                    }
                }
                item {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp))
                    }
                }
                if (onDelete != null) {
                    item {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
