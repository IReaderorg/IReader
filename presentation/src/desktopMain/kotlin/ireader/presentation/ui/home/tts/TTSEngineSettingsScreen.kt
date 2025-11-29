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
import ireader.domain.services.tts_service.DesktopTTSService
import ireader.domain.services.tts_service.piper.PiperModelManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

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
    val scope = rememberCoroutineScope()
    
    // Engine status
    var piperAvailable by remember { mutableStateOf(false) }
    var kokoroAvailable by remember { mutableStateOf(false) }
    var mayaAvailable by remember { mutableStateOf(false) }
    var currentEngine by remember { mutableStateOf("") }
    
    // Check engine status
    LaunchedEffect(Unit) {
        piperAvailable = ttsService.synthesizer.isInitialized()
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
                        text = "TTS Engine Manager",
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
                                text = "Current Engine",
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
                
                // Engine list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Piper TTS
                    EngineCard(
                        name = "Piper TTS",
                        description = "High-performance neural TTS with 30+ voices",
                        isAvailable = piperAvailable,
                        isCurrentEngine = currentEngine == "PIPER",
                        onSelect = {
                            scope.launch {
                                ttsService.setEngine(ireader.domain.services.tts_service.DesktopTTSService.TTSEngine.PIPER)
                                currentEngine = "PIPER"
                            }
                        }
                    )
                    
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
                    text = "Note: Some engines require installation. Use the full TTS Manager in Settings for installation options.",
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
                        text = "Not installed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (isCurrentEngine) {
                AssistChip(
                    onClick = {},
                    label = { Text("Active") },
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
 * Desktop implementation of TTS Voice Selection Screen
 * 
 * Shows available Piper/Kokoro/Maya voices based on current engine
 * For Piper: Shows ALL available voices with download option
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TTSVoiceSelectionScreen(
    isDesktop: Boolean,
    onDismiss: () -> Unit
) {
    val ttsService: DesktopTTSService = koinInject()
    val modelManager: ireader.domain.services.tts_service.piper.PiperModelManager = koinInject()
    val appPrefs: ireader.domain.preferences.prefs.AppPreferences = koinInject()
    val scope = rememberCoroutineScope()
    
    // Voice state
    var allVoices by remember { mutableStateOf<List<PiperVoiceInfo>>(emptyList()) }
    // Store full VoiceModel objects for download
    var fullVoiceModels by remember { mutableStateOf<List<ireader.domain.services.tts_service.piper.VoiceModel>>(emptyList()) }
    var selectedVoice by remember { mutableStateOf<String?>(null) }
    var currentEngine by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var downloadingVoiceId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var downloadError by remember { mutableStateOf<String?>(null) }
    var filterLanguage by remember { mutableStateOf<String?>(null) }
    
    // Load voices based on current engine
    LaunchedEffect(Unit) {
        currentEngine = ttsService.getCurrentEngine().name
        selectedVoice = ttsService.state.selectedVoiceModel?.id
        
        ireader.core.log.Log.info { "TTSVoiceSelectionScreen: Loading voices for engine $currentEngine" }
        
        // Get available voices based on engine
        when (currentEngine) {
            "PIPER" -> {
                // Load ALL available Piper voices from model manager
                ireader.core.log.Log.info { "Loading Piper voices from modelManager..." }
                val models = modelManager.getAvailableModels()
                ireader.core.log.Log.info { "ModelManager returned ${models.size} models" }
                
                // Get downloaded model IDs from preferences
                val downloadedIds = appPrefs.downloadedModels().get()
                ireader.core.log.Log.info { "Downloaded model IDs: $downloadedIds" }
                
                // Check which models are actually downloaded (file exists)
                val modelsWithDownloadStatus = models.map { model ->
                    val paths = modelManager.getModelPaths(model.id)
                    val isActuallyDownloaded = paths != null || downloadedIds.contains(model.id)
                    model.copy(isDownloaded = isActuallyDownloaded)
                }
                
                // Store full models for download
                fullVoiceModels = modelsWithDownloadStatus
                
                // Convert to UI model
                allVoices = modelsWithDownloadStatus.map { model ->
                    PiperVoiceInfo(
                        id = model.id,
                        name = model.name,
                        language = model.language,
                        isDownloaded = model.isDownloaded,
                        sizeBytes = model.sizeBytes,
                        quality = model.quality.name
                    )
                }
                ireader.core.log.Log.info { "Loaded ${allVoices.size} Piper voices" }
            }
            "KOKORO" -> {
                // Kokoro has predefined voices
                allVoices = listOf(
                    PiperVoiceInfo("af_bella", "Bella (Female)", "en-US", true, 0, "HIGH"),
                    PiperVoiceInfo("af_nicole", "Nicole (Female)", "en-US", true, 0, "HIGH"),
                    PiperVoiceInfo("af_sarah", "Sarah (Female)", "en-US", true, 0, "HIGH"),
                    PiperVoiceInfo("af_sky", "Sky (Female)", "en-US", true, 0, "HIGH"),
                    PiperVoiceInfo("am_adam", "Adam (Male)", "en-US", true, 0, "HIGH"),
                    PiperVoiceInfo("am_michael", "Michael (Male)", "en-US", true, 0, "HIGH"),
                    PiperVoiceInfo("bf_emma", "Emma (Female)", "en-GB", true, 0, "HIGH"),
                    PiperVoiceInfo("bm_george", "George (Male)", "en-GB", true, 0, "HIGH")
                )
            }
            "MAYA" -> {
                // Maya supports multiple languages
                allVoices = listOf(
                    PiperVoiceInfo("en", "English", "en", true, 0, "HIGH"),
                    PiperVoiceInfo("es", "Spanish", "es", true, 0, "HIGH"),
                    PiperVoiceInfo("fr", "French", "fr", true, 0, "HIGH"),
                    PiperVoiceInfo("de", "German", "de", true, 0, "HIGH"),
                    PiperVoiceInfo("it", "Italian", "it", true, 0, "HIGH"),
                    PiperVoiceInfo("pt", "Portuguese", "pt", true, 0, "HIGH"),
                    PiperVoiceInfo("ru", "Russian", "ru", true, 0, "HIGH"),
                    PiperVoiceInfo("zh", "Chinese", "zh", true, 0, "HIGH"),
                    PiperVoiceInfo("ja", "Japanese", "ja", true, 0, "HIGH"),
                    PiperVoiceInfo("ko", "Korean", "ko", true, 0, "HIGH")
                )
            }
        }
        isLoading = false
    }
    
    // Get unique languages for filter
    val availableLanguages = remember(allVoices) {
        allVoices.map { it.language }.distinct().sorted()
    }
    
    // Filter voices by language
    val filteredVoices = remember(allVoices, filterLanguage) {
        if (filterLanguage == null) allVoices
        else allVoices.filter { it.language == filterLanguage }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.9f),
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
                    Column {
                        Text(
                            text = "Voice Selection",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Engine: $currentEngine • ${allVoices.size} voices available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                // Language filter (for Piper)
                if (currentEngine == "PIPER" && availableLanguages.size > 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter:", style = MaterialTheme.typography.bodyMedium)
                        FilterChip(
                            selected = filterLanguage == null,
                            onClick = { filterLanguage = null },
                            label = { Text("All") }
                        )
                        availableLanguages.take(6).forEach { lang ->
                            FilterChip(
                                selected = filterLanguage == lang,
                                onClick = { filterLanguage = if (filterLanguage == lang) null else lang },
                                label = { Text(lang) }
                            )
                        }
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Error message
                if (downloadError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                            Text(
                                text = downloadError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { downloadError = null }) {
                                Icon(Icons.Default.Close, "Dismiss")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading voices...")
                        }
                    }
                } else if (filteredVoices.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No voices available",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (currentEngine == "PIPER") 
                                    "Voice catalog could not be loaded. Check logs for details."
                                else 
                                    "Please install the TTS engine first from Settings > TTS Engine Manager",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Voice list
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Show downloaded voices first
                        val downloadedVoices = filteredVoices.filter { it.isDownloaded }
                        val notDownloadedVoices = filteredVoices.filter { !it.isDownloaded }
                        
                        if (downloadedVoices.isNotEmpty()) {
                            Text(
                                text = "Downloaded (${downloadedVoices.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            downloadedVoices.forEach { voice ->
                                PiperVoiceCard(
                                    voice = voice,
                                    isSelected = voice.id == selectedVoice,
                                    isDownloading = downloadingVoiceId == voice.id,
                                    downloadProgress = if (downloadingVoiceId == voice.id) downloadProgress else 0f,
                                    onSelect = {
                                        scope.launch {
                                            when (currentEngine) {
                                                "PIPER" -> ttsService.selectVoiceModel(voice.id)
                                            }
                                            selectedVoice = voice.id
                                        }
                                    },
                                    onDownload = {}
                                )
                            }
                        }
                        
                        if (notDownloadedVoices.isNotEmpty() && currentEngine == "PIPER") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Available for Download (${notDownloadedVoices.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            notDownloadedVoices.forEach { voice ->
                                PiperVoiceCard(
                                    voice = voice,
                                    isSelected = false,
                                    isDownloading = downloadingVoiceId == voice.id,
                                    downloadProgress = if (downloadingVoiceId == voice.id) downloadProgress else 0f,
                                    onSelect = {},
                                    onDownload = {
                                        scope.launch {
                                            downloadingVoiceId = voice.id
                                            downloadProgress = 0f
                                            downloadError = null
                                            
                                            try {
                                                // Find the full model from our stored list
                                                val model = fullVoiceModels.find { it.id == voice.id }
                                                ireader.core.log.Log.info { "Downloading voice: ${voice.id}, model found: ${model != null}" }
                                                
                                                if (model != null) {
                                                    ireader.core.log.Log.info { "Starting download for ${model.name} from ${model.modelUrl}" }
                                                    modelManager.downloadModel(model).collect { progress ->
                                                        downloadProgress = progress.progress
                                                        ireader.core.log.Log.info { "Download progress: ${(progress.progress * 100).toInt()}% - ${progress.status}" }
                                                    }
                                                    
                                                    // Update the voice as downloaded in UI
                                                    allVoices = allVoices.map { v ->
                                                        if (v.id == voice.id) v.copy(isDownloaded = true) else v
                                                    }
                                                    
                                                    // Update fullVoiceModels
                                                    fullVoiceModels = fullVoiceModels.map { m ->
                                                        if (m.id == voice.id) m.copy(isDownloaded = true) else m
                                                    }
                                                    
                                                    // Save to preferences
                                                    val currentDownloaded = appPrefs.downloadedModels().get().toMutableSet()
                                                    currentDownloaded.add(voice.id)
                                                    appPrefs.downloadedModels().set(currentDownloaded)
                                                    
                                                    ireader.core.log.Log.info { "Voice ${voice.id} downloaded successfully" }
                                                } else {
                                                    downloadError = "Voice model not found in catalog"
                                                    ireader.core.log.Log.error { "Voice model ${voice.id} not found in fullVoiceModels (size: ${fullVoiceModels.size})" }
                                                }
                                            } catch (e: Exception) {
                                                downloadError = "Download failed: ${e.message}"
                                                ireader.core.log.Log.error { "Voice download failed: ${e.message}" }
                                                e.printStackTrace()
                                            } finally {
                                                downloadingVoiceId = null
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PiperVoiceInfo(
    val id: String,
    val name: String,
    val language: String,
    val isDownloaded: Boolean,
    val sizeBytes: Long,
    val quality: String
)

@Composable
private fun PiperVoiceCard(
    voice: PiperVoiceInfo,
    isSelected: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onSelect: () -> Unit,
    onDownload: () -> Unit
) {
    OutlinedCard(
        onClick = { if (voice.isDownloaded) onSelect() },
        modifier = Modifier.fillMaxWidth(),
        enabled = voice.isDownloaded && !isDownloading,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        isSelected -> Icons.Default.CheckCircle
                        voice.isDownloaded -> Icons.Default.RadioButtonUnchecked
                        else -> Icons.Default.Download
                    },
                    contentDescription = null,
                    tint = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        voice.isDownloaded -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = voice.name,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = voice.language,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (voice.sizeBytes > 0) {
                            Text(
                                text = "• ${voice.sizeBytes / (1024 * 1024)} MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "• ${voice.quality}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                when {
                    isSelected -> {
                        AssistChip(
                            onClick = {},
                            label = { Text("Selected") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    isDownloading -> {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    !voice.isDownloaded -> {
                        Button(
                            onClick = onDownload,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download")
                        }
                    }
                }
            }
            
            // Download progress bar
            if (isDownloading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Downloading... ${(downloadProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = "Configure Gradio TTS",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Text(
                    text = "Gradio TTS uses a HuggingFace Space to synthesize speech. You can use the default space or deploy your own.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                HorizontalDivider()
                
                // Space URL
                OutlinedTextField(
                    value = spaceUrl,
                    onValueChange = { spaceUrl = it },
                    label = { Text("HuggingFace Space URL") },
                    placeholder = { Text("https://x-ireader.hf.space") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // API Key (optional)
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (optional)") },
                    placeholder = { Text("For private spaces") },
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
                        Text("Cancel")
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
                            Text("Test")
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
                        Text("Save")
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
    val presets = remember { ireader.domain.services.tts_service.GradioTTSPresets.getAllPresets() }
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
                    text = "Configure Gradio TTS",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Text(
                    text = "Select a preset TTS engine or configure a custom Gradio space.",
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
                        label = { Text("Presets") }
                    )
                    FilterChip(
                        selected = useCustom,
                        onClick = { useCustom = true },
                        label = { Text("Custom") }
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
                            label = { Text("Space URL") },
                            placeholder = { Text("https://username-space.hf.space") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = customApiName,
                            onValueChange = { customApiName = it },
                            label = { Text("API Name") },
                            placeholder = { Text("/predict or /synthesize_speech") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Text(
                            text = "Tip: Check the API tab on the Hugging Face Space page to find the correct API name.",
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
                        Text("Cancel")
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
                                        ireader.domain.services.tts_service.GradioTTSPresets.getPresetById(selectedPresetId)
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
                            Text("Test")
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
                                    ireader.domain.services.tts_service.GradioTTSPresets.getPresetById(selectedPresetId)
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
                        Text("Save")
                    }
                }
            }
        }
    }
}
