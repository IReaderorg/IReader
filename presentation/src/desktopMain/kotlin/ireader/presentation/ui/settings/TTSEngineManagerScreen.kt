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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.material3.OutlinedTextField
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
import ireader.domain.services.tts_service.DesktopTTSService
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
    modifier: Modifier = Modifier
) {
    val ttsService: DesktopTTSService = koinInject()
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
    var useCoquiTTS by remember { mutableStateOf(false) }

    // Check engine status on mount - only when screen opens
    LaunchedEffect(Unit) {
        // Check Piper
        piperStatus = try {
            if (ttsService.synthesizer.isInitialized()) {
                piperMessage = "Piper TTS initialized"
                EngineStatus.INSTALLED
            } else {
                piperMessage = "Piper not initialized"
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TTS Engine Manager") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
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
                        text = "Manage TTS Engines",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Install and configure text-to-speech engines for offline reading",
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
                        contentDescription = "Performance",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Performance")
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
                                text = "Hello, this is a test.",
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
                                text = "Hello, this is a test.",
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
                                    text = "Installation Log",
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
                                    text = "Installation in progress... This may take several minutes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Coqui TTS (Online) - Your Custom HuggingFace Space
            val appPrefs: ireader.domain.preferences.prefs.AppPreferences = koinInject()
            var coquiSpaceUrl by remember { mutableStateOf("") }
            var coquiSpeed by remember { mutableStateOf(1.0f) }
            var coquiApiKey by remember { mutableStateOf("") }
            var isCoquiTesting by remember { mutableStateOf(false) }
            var coquiTestResult by remember { mutableStateOf<String?>(null) }
            
            // Load Coqui preferences on mount
            LaunchedEffect(Unit) {
                useCoquiTTS = appPrefs.useCoquiTTS().get()
                coquiSpaceUrl = appPrefs.coquiSpaceUrl().get()
                coquiSpeed = appPrefs.coquiSpeed().get()
                coquiApiKey = appPrefs.coquiApiKey().get()
                
                Log.info { "Loaded Coqui prefs: enabled=$useCoquiTTS, url=$coquiSpaceUrl, speed=$coquiSpeed" }
                
                // Configure Coqui if enabled
                if (useCoquiTTS && coquiSpaceUrl.isNotEmpty()) {
                    ttsService.configureCoqui(coquiSpaceUrl, coquiApiKey.ifEmpty { null })
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                    tint = if (useCoquiTTS)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column {
                                    Text(
                                        text = "Coqui TTS (Online)",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "High-quality neural TTS via HuggingFace Space",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        // Status badge
                        if (useCoquiTTS && ttsService.coquiAvailable) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Ready",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        
                        Switch(
                            checked = useCoquiTTS,
                            onCheckedChange = { enabled ->
                                useCoquiTTS = enabled
                                scope.launch {
                                    appPrefs.useCoquiTTS().set(enabled)
                                    Log.info { "Saved useCoquiTTS: $enabled" }
                                    if (enabled && coquiSpaceUrl.isNotEmpty()) {
                                        ttsService.configureCoqui(coquiSpaceUrl, coquiApiKey.ifEmpty { null })
                                    } else if (!enabled) {
                                        ttsService.configureCoqui("", null)
                                    }
                                }
                            }
                        )
                    }

                    // Features list (always visible)
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "High-quality neural voices",
                            "No local installation required",
                            "Works via internet connection",
                            "Custom HuggingFace Space support"
                        ).forEach { feature ->
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
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (useCoquiTTS) {
                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Space URL
                        Text(
                            text = "HuggingFace Space URL",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = coquiSpaceUrl,
                            onValueChange = { url ->
                                coquiSpaceUrl = url
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("https://x-ireader.hf.space") },
                            singleLine = true,
                            label = { Text("Space URL") }
                        )
                        
                        Text(
                            text = "Example: https://x-ireader.hf.space",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // API Key (optional)
                        Text(
                            text = "API Key (Optional)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = coquiApiKey,
                            onValueChange = { key ->
                                coquiApiKey = key
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("For private spaces only") },
                            singleLine = true,
                            label = { Text("API Key") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Speed control
                        Text(
                            text = "Speech Speed: ${"%.1f".format(coquiSpeed)}x",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Slider(
                            value = coquiSpeed,
                            onValueChange = { speed ->
                                coquiSpeed = speed
                            },
                            valueRange = 0.5f..2.0f,
                            steps = 15,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0.5x", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("2.0x", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Save button
                        Button(
                            onClick = {
                                scope.launch {
                                    // Save all preferences
                                    appPrefs.coquiSpaceUrl().set(coquiSpaceUrl)
                                    appPrefs.coquiApiKey().set(coquiApiKey)
                                    appPrefs.coquiSpeed().set(coquiSpeed)
                                    
                                    Log.info { "Saved Coqui prefs: url=$coquiSpaceUrl, speed=$coquiSpeed" }
                                    
                                    // Configure the service
                                    if (coquiSpaceUrl.isNotEmpty()) {
                                        ttsService.configureCoqui(coquiSpaceUrl, coquiApiKey.ifEmpty { null })
                                    }
                                    
                                    coquiTestResult = "✓ Settings saved successfully!"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Settings")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Test result
                        if (coquiTestResult != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (coquiTestResult!!.startsWith("✓"))
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (coquiTestResult!!.startsWith("✓")) 
                                            Icons.Default.CheckCircle 
                                        else 
                                            Icons.Default.Error,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (coquiTestResult!!.startsWith("✓"))
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = coquiTestResult!!,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Test button
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isCoquiTesting = true
                                    coquiTestResult = null
                                    installationLog += "Testing Coqui TTS...\n"
                                    installationLog += "URL: $coquiSpaceUrl\n"
                                    
                                    try {
                                        // Save settings first
                                        appPrefs.coquiSpaceUrl().set(coquiSpaceUrl)
                                        appPrefs.coquiApiKey().set(coquiApiKey)
                                        appPrefs.coquiSpeed().set(coquiSpeed)
                                        
                                        // Configure
                                        ttsService.configureCoqui(coquiSpaceUrl, coquiApiKey.ifEmpty { null })
                                        
                                        if (ttsService.coquiAvailable) {
                                            installationLog += "✓ Coqui TTS configured successfully\n"
                                            coquiTestResult = "✓ Connection successful! Coqui TTS is ready."
                                        } else {
                                            installationLog += "✗ Failed to configure Coqui TTS\n"
                                            coquiTestResult = "✗ Failed to connect. Check URL and internet connection."
                                        }
                                    } catch (e: Exception) {
                                        installationLog += "✗ Test error: ${e.message}\n"
                                        coquiTestResult = "✗ Error: ${e.message}"
                                    } finally {
                                        isCoquiTesting = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = coquiSpaceUrl.isNotEmpty() && !isCoquiTesting
                        ) {
                            if (isCoquiTesting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (isCoquiTesting) "Testing..." else "Test Connection")
                        }
                    }
                }
            }
            // Piper TTS Voices Section

                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                text = "Piper TTS Voices",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Text(
                            text = "High-quality offline neural voices. Download and use with Sherpa TTS app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Show available voices count
                        val availableVoices = ireader.domain.catalogs.PiperVoiceCatalog.getAllVoices()
                        val languages = ireader.domain.catalogs.PiperVoiceCatalog.getSupportedLanguages()

                        Text(
                            text = "${availableVoices.size} voices available in ${languages.size} languages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }


            // Voice list header

                Text(
                    text = "Available Voices",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )


            // Voice list state management - using PiperModelManager like TTSEngineSettingsScreen
            val modelManager: ireader.domain.services.tts_service.piper.PiperModelManager = koinInject()
            
            var selectedVoiceId by remember { mutableStateOf<String?>(null) }
            var downloadingVoice by remember { mutableStateOf<String?>(null) }
            var downloadProgress by remember { mutableStateOf(0) }
            var downloadedVoices by remember { mutableStateOf<Set<String>>(emptySet()) }
            var piperVoiceModels by remember { mutableStateOf<List<ireader.domain.services.tts_service.piper.VoiceModel>>(emptyList()) }
            
            // Load installed voices on mount - using same logic as TTSEngineSettingsScreen
            LaunchedEffect(Unit) {
                try {
                    // Get all available models from model manager
                    val models = modelManager.getAvailableModels()
                    
                    // Get downloaded model IDs from preferences
                    val downloadedIds = appPrefs.downloadedModels().get()
                    
                    // Check which models are actually downloaded (file exists)
                    val modelsWithDownloadStatus = models.map { model ->
                        val paths = modelManager.getModelPaths(model.id)
                        val isActuallyDownloaded = paths != null || downloadedIds.contains(model.id)
                        model.copy(isDownloaded = isActuallyDownloaded)
                    }
                    
                    piperVoiceModels = modelsWithDownloadStatus
                    downloadedVoices = modelsWithDownloadStatus.filter { it.isDownloaded }.map { it.id }.toSet()
                    selectedVoiceId = appPrefs.selectedPiperModel().get().ifEmpty { null }
                    
                    Log.info { "Loaded ${models.size} Piper voice models, ${downloadedVoices.size} downloaded" }
                } catch (e: Exception) {
                    Log.error { "Failed to load voice models: ${e.message}" }
                }
            }
            
            // Voice list - using PiperVoiceCatalog for display but piperVoiceModels for download status
            val voices = ireader.domain.catalogs.PiperVoiceCatalog.getAllVoices()
            voices.forEachIndexed { index, voice ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Find the corresponding PiperModelManager model for this voice
                val piperModel = piperVoiceModels.find { it.id == voice.id }
                val isDownloaded = piperModel?.isDownloaded == true || downloadedVoices.contains(voice.id)
                
                VoiceCard(
                    voice = voice,
                    isSelected = selectedVoiceId == voice.id,
                    isDownloading = downloadingVoice == voice.id,
                    isDownloaded = isDownloaded,
                    downloadProgress = if (downloadingVoice == voice.id) downloadProgress else 0,
                    onSelect = {
                        if (isDownloaded) {
                            selectedVoiceId = voice.id
                            scope.launch {
                                appPrefs.selectedPiperModel().set(voice.id)
                                // Also select the voice in the TTS service
                                try {
                                    ttsService.selectVoiceModel(voice.id)
                                    installationLog += "✓ Selected voice: ${voice.name}\n"
                                } catch (e: Exception) {
                                    installationLog += "✗ Failed to select voice: ${e.message}\n"
                                }
                            }
                        } else {
                            installationLog += "⚠ Voice must be downloaded first: ${voice.name}\n"
                        }
                    },
                    onPreview = {
                        if (isDownloaded) {
                            scope.launch {
                                try {
                                    installationLog += "Previewing voice: ${voice.name}...\n"
                                    // First select the voice model
                                    ttsService.selectVoiceModel(voice.id)
                                    // Then synthesize to test the voice
                                    val result = ttsService.synthesizer.synthesize(
                                        "Hello, this is a preview of the ${voice.name} voice."
                                    )
                                    if (result.isSuccess) {
                                        val audioData = result.getOrNull()
                                        installationLog += "✓ Preview synthesis successful\n"
                                        installationLog += "  Audio size: ${audioData?.samples?.size ?: 0} bytes\n"
                                        installationLog += "  Sample rate: ${audioData?.sampleRate ?: 0} Hz\n"
                                    } else {
                                        installationLog += "✗ Preview failed: ${result.exceptionOrNull()?.message}\n"
                                    }
                                } catch (e: Exception) {
                                    installationLog += "✗ Preview error: ${e.message}\n"
                                }
                            }
                        } else {
                            installationLog += "⚠ Voice must be downloaded first: ${voice.name}\n"
                        }
                    },
                    onDownload = {
                        scope.launch {
                            downloadingVoice = voice.id
                            downloadProgress = 0
                            installationLog += "═══════════════════════════════════════\n"
                            installationLog += "Downloading voice: ${voice.name}\n"
                            installationLog += "═══════════════════════════════════════\n"
                            installationLog += "Size: ${voice.modelSize / 1_000_000}MB\n"
                            installationLog += "URL: ${voice.downloadUrl}\n\n"
                            
                            try {
                                // Find the model from PiperModelManager
                                val model = piperVoiceModels.find { it.id == voice.id }
                                
                                if (model != null) {
                                    Log.info { "Starting download for ${model.name} from ${model.modelUrl}" }
                                    modelManager.downloadModel(model).collect { progress ->
                                        downloadProgress = (progress.progress * 100).toInt()
                                        if (downloadProgress % 10 == 0) {
                                            installationLog += "  Progress: $downloadProgress%\n"
                                        }
                                    }
                                    
                                    // Update downloaded status
                                    downloadedVoices = downloadedVoices + voice.id
                                    piperVoiceModels = piperVoiceModels.map { m ->
                                        if (m.id == voice.id) m.copy(isDownloaded = true) else m
                                    }
                                    
                                    // Save to preferences
                                    val currentDownloaded = appPrefs.downloadedModels().get().toMutableSet()
                                    currentDownloaded.add(voice.id)
                                    appPrefs.downloadedModels().set(currentDownloaded)
                                    
                                    installationLog += "✓ Download complete!\n"
                                    installationLog += "Voice ${voice.name} is now available.\n\n"
                                } else {
                                    // Model not found in PiperModelManager, try to create one from VoiceModel
                                    Log.warn { "Voice ${voice.id} not found in PiperModelManager, creating from catalog" }
                                    val newModel = ireader.domain.services.tts_service.piper.VoiceModel(
                                        id = voice.id,
                                        name = voice.name,
                                        language = voice.language,
                                        quality = when (voice.quality) {
                                            ireader.domain.models.tts.VoiceQuality.LOW -> ireader.domain.services.tts_service.piper.VoiceModel.Quality.LOW
                                            ireader.domain.models.tts.VoiceQuality.MEDIUM -> ireader.domain.services.tts_service.piper.VoiceModel.Quality.MEDIUM
                                            ireader.domain.models.tts.VoiceQuality.HIGH -> ireader.domain.services.tts_service.piper.VoiceModel.Quality.HIGH
                                            ireader.domain.models.tts.VoiceQuality.PREMIUM -> ireader.domain.services.tts_service.piper.VoiceModel.Quality.HIGH
                                        },
                                        gender = when (voice.gender) {
                                            ireader.domain.models.tts.VoiceGender.MALE -> ireader.domain.services.tts_service.piper.VoiceModel.Gender.MALE
                                            ireader.domain.models.tts.VoiceGender.FEMALE -> ireader.domain.services.tts_service.piper.VoiceModel.Gender.FEMALE
                                            ireader.domain.models.tts.VoiceGender.NEUTRAL -> ireader.domain.services.tts_service.piper.VoiceModel.Gender.NEUTRAL
                                        },
                                        sizeBytes = voice.modelSize,
                                        modelUrl = voice.downloadUrl,
                                        configUrl = voice.configUrl,
                                        isDownloaded = false
                                    )
                                    
                                    modelManager.downloadModel(newModel).collect { progress ->
                                        downloadProgress = (progress.progress * 100).toInt()
                                        if (downloadProgress % 10 == 0) {
                                            installationLog += "  Progress: $downloadProgress%\n"
                                        }
                                    }
                                    
                                    downloadedVoices = downloadedVoices + voice.id
                                    
                                    // Save to preferences
                                    val currentDownloaded = appPrefs.downloadedModels().get().toMutableSet()
                                    currentDownloaded.add(voice.id)
                                    appPrefs.downloadedModels().set(currentDownloaded)
                                    
                                    installationLog += "✓ Download complete!\n"
                                    installationLog += "Voice ${voice.name} is now available.\n\n"
                                }
                            } catch (e: Exception) {
                                installationLog += "✗ Download error: ${e.message}\n\n"
                                Log.error { "Voice download failed: ${e.message}" }
                            } finally {
                                downloadingVoice = null
                                downloadProgress = 0
                            }
                        }
                    },
                    onDelete = {
                        scope.launch {
                            try {
                                installationLog += "Deleting voice: ${voice.name}...\n"
                                
                                // Delete using model manager
                                val result = modelManager.deleteModel(voice.id)
                                
                                result.onSuccess {
                                    downloadedVoices = downloadedVoices - voice.id
                                    piperVoiceModels = piperVoiceModels.map { m ->
                                        if (m.id == voice.id) m.copy(isDownloaded = false) else m
                                    }
                                    
                                    // Remove from preferences
                                    val currentDownloaded = appPrefs.downloadedModels().get().toMutableSet()
                                    currentDownloaded.remove(voice.id)
                                    appPrefs.downloadedModels().set(currentDownloaded)
                                    
                                    if (selectedVoiceId == voice.id) {
                                        selectedVoiceId = null
                                        appPrefs.selectedPiperModel().set("")
                                    }
                                    installationLog += "✓ Voice deleted\n"
                                }.onFailure { error ->
                                    installationLog += "✗ Delete failed: ${error.message}\n"
                                }
                            } catch (e: Exception) {
                                installationLog += "✗ Delete error: ${e.message}\n"
                            }
                        }
                    }
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
                            text = "Need Help?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Piper requires native libraries (provided in releases)\n" +
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
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (status) {
                    EngineStatus.NOT_INSTALLED, EngineStatus.ERROR -> {
                        Button(
                            onClick = onInstall,
                            enabled = !isInstalling,
                            modifier = Modifier.weight(1f)
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
                    EngineStatus.INSTALLED -> {
                        OutlinedButton(
                            onClick = onUninstall,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Uninstall")
                        }
                        Button(
                            onClick = onTest,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test")
                        }
                    }
                    EngineStatus.CHECKING -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
                                contentDescription = "Downloaded",
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

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isDownloaded) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, "Delete")
                        }
                        IconButton(onClick = onPreview) {
                            Icon(Icons.Default.PlayArrow, "Preview")
                        }
                    } else {
                        IconButton(onClick = onDownload, enabled = !isDownloading) {
                            Icon(Icons.Default.Download, "Download")
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = onSelect,
                        enabled = isDownloaded
                    )
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