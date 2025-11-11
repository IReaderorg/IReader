package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.core.log.Log
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.services.tts_service.piper.DownloadProgress
import ireader.domain.services.tts_service.piper.PiperModelManager
import ireader.domain.services.tts_service.piper.PiperSpeechSynthesizer
import ireader.domain.services.tts_service.piper.VoiceModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Voice model management panel for selecting and managing Piper TTS voice models
 * 
 * Features:
 * - Display available and downloaded voice models
 * - Allow user to select active voice model
 * - Persist selection to preferences
 * - Reload synthesizer when model changes
 * 
 * Requirements: 2.4, 8.1
 */
@Composable
fun VoiceModelManagementPanel(
    modifier: Modifier = Modifier,
    modelManager: PiperModelManager = koinInject(),
    synthesizer: PiperSpeechSynthesizer = koinInject(),
    appPrefs: AppPreferences = koinInject()
) {
    var availableModels by remember { mutableStateOf<List<VoiceModel>>(emptyList()) }
    var filteredModels by remember { mutableStateOf<List<VoiceModel>>(emptyList()) }
    var selectedModelId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var modelToDownload by remember { mutableStateOf<VoiceModel?>(null) }
    var downloadFlow by remember { mutableStateOf<Flow<DownloadProgress>?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var modelToDelete by remember { mutableStateOf<VoiceModel?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLanguageFilter by remember { mutableStateOf<String?>(null) }
    var selectedQualityFilter by remember { mutableStateOf<VoiceModel.Quality?>(null) }
    var selectedGenderFilter by remember { mutableStateOf<VoiceModel.Gender?>(null) }
    var showDownloadedOnly by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Apply filters
    LaunchedEffect(availableModels, searchQuery, selectedLanguageFilter, selectedQualityFilter, selectedGenderFilter, showDownloadedOnly) {
        filteredModels = availableModels.filter { model ->
            val matchesSearch = searchQuery.isBlank() || 
                model.name.contains(searchQuery, ignoreCase = true) ||
                model.language.contains(searchQuery, ignoreCase = true)
            
            val matchesLanguage = selectedLanguageFilter == null || 
                model.language.startsWith(selectedLanguageFilter!!)
            
            val matchesQuality = selectedQualityFilter == null || 
                model.quality == selectedQualityFilter
            
            val matchesGender = selectedGenderFilter == null || 
                model.gender == selectedGenderFilter
            
            val matchesDownloaded = !showDownloadedOnly || model.isDownloaded
            
            matchesSearch && matchesLanguage && matchesQuality && matchesGender && matchesDownloaded
        }
    }
    
    // Load available models and selected model on mount
    LaunchedEffect(Unit) {
        try {
            // Load selected model from preferences
            selectedModelId = appPrefs.selectedPiperModel().get()
            
            // Load available models
            val models = modelManager.getAvailableModels()
            val downloadedModels = modelManager.getDownloadedModels()
            
            // Mark downloaded models
            availableModels = models.map { model ->
                model.copy(isDownloaded = downloadedModels.any { it.id == model.id })
            }
            
            isLoading = false
        } catch (e: Exception) {
            Log.error { "Failed to load voice models: ${e.message}" }
            isLoading = false
        }
    }
    
    // Handle model selection
    val onModelSelected: (VoiceModel) -> Unit = { model ->
        scope.launch {
            try {
                // Save selection to preferences
                appPrefs.selectedPiperModel().set(model.id)
                selectedModelId = model.id
                
                // Reload synthesizer with new model
                val paths = modelManager.getModelPaths(model.id)
                if (paths != null) {
                    Log.info { "Loading voice model: ${model.name}" }
                    synthesizer.initialize(paths.modelPath, paths.configPath)
                        .onSuccess {
                            Log.info { "Voice model loaded successfully" }
                        }
                        .onFailure { error ->
                            Log.error { "Failed to load voice model: ${error.message}" }
                        }
                }
            } catch (e: Exception) {
                Log.error { "Error selecting voice model: ${e.message}" }
            }
        }
    }
    
    // Handle model download
    val onDownloadModel: (VoiceModel) -> Unit = { model ->
        scope.launch {
            try {
                modelToDownload = model
                downloadFlow = modelManager.downloadModel(model)
                showDownloadDialog = true
            } catch (e: Exception) {
                Log.error { "Failed to start download: ${e.message}" }
            }
        }
    }
    
    // Handle download complete
    val onDownloadComplete: () -> Unit = {
        scope.launch {
            // Refresh model list to show newly downloaded model
            val models = modelManager.getAvailableModels()
            val downloadedModels = modelManager.getDownloadedModels()
            
            availableModels = models.map { model ->
                model.copy(isDownloaded = downloadedModels.any { it.id == model.id })
            }
            
            // Auto-select if no model is currently selected
            if (selectedModelId.isEmpty() && modelToDownload != null) {
                onModelSelected(modelToDownload!!)
            }
        }
    }
    
    // Handle model deletion
    val onDeleteModel: (VoiceModel) -> Unit = { model ->
        modelToDelete = model
        showDeleteConfirmation = true
    }
    
    // Confirm deletion
    val confirmDelete: () -> Unit = {
        scope.launch {
            modelToDelete?.let { model ->
                try {
                    modelManager.deleteModel(model.id)
                        .onSuccess {
                            Log.info { "Model deleted: ${model.name}" }
                            
                            // Refresh model list
                            val models = modelManager.getAvailableModels()
                            val downloadedModels = modelManager.getDownloadedModels()
                            
                            availableModels = models.map { m ->
                                m.copy(isDownloaded = downloadedModels.any { it.id == m.id })
                            }
                            
                            // Clear selection if deleted model was selected
                            if (selectedModelId == model.id) {
                                appPrefs.selectedPiperModel().set("")
                                selectedModelId = ""
                            }
                        }
                        .onFailure { error ->
                            Log.error { "Failed to delete model: ${error.message}" }
                        }
                } catch (e: Exception) {
                    Log.error { "Error deleting model: ${e.message}" }
                }
            }
            showDeleteConfirmation = false
            modelToDelete = null
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Voice Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "Voice Model Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Divider()
            
            // Loading state
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Storage usage info
                StorageUsageInfo(
                    models = availableModels
                )
                
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search voices...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
                
                // Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Language filter
                    val languages = availableModels.map { it.language.substringBefore("-") }.distinct().sorted()
                    FilterChipDropdown(
                        label = "Language",
                        options = languages,
                        selectedOption = selectedLanguageFilter,
                        onOptionSelected = { selectedLanguageFilter = it }
                    )
                    
                    // Quality filter
                    FilterChipDropdown(
                        label = "Quality",
                        options = VoiceModel.Quality.values().map { it.name },
                        selectedOption = selectedQualityFilter?.name,
                        onOptionSelected = { 
                            selectedQualityFilter = it?.let { VoiceModel.Quality.valueOf(it) }
                        }
                    )
                    
                    // Gender filter
                    FilterChipDropdown(
                        label = "Gender",
                        options = VoiceModel.Gender.values().map { it.name },
                        selectedOption = selectedGenderFilter?.name,
                        onOptionSelected = { 
                            selectedGenderFilter = it?.let { VoiceModel.Gender.valueOf(it) }
                        }
                    )
                    
                    // Downloaded only - Icon only to save space
                    FilterChip(
                        selected = showDownloadedOnly,
                        onClick = { showDownloadedOnly = !showDownloadedOnly },
                        label = { 
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Show Downloaded Only",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        leadingIcon = if (showDownloadedOnly) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
                
                // Results count
                Text(
                    text = "${filteredModels.size} voice${if (filteredModels.size != 1) "s" else ""} found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Voice model selector
                VoiceModelSelector(
                    models = filteredModels,
                    selectedModelId = selectedModelId,
                    onModelSelected = onModelSelected,
                    onDownloadModel = onDownloadModel,
                    onDeleteModel = onDeleteModel
                )
                
                // Info text
                if (selectedModelId.isEmpty()) {
                    Text(
                        text = "Please download and select a voice model to enable TTS",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val selectedModel = availableModels.find { it.id == selectedModelId }
                    if (selectedModel != null) {
                        Text(
                            text = "Active voice: ${selectedModel.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
    
    // Download dialog
    if (showDownloadDialog && modelToDownload != null && downloadFlow != null) {
        ModelDownloadDialog(
            model = modelToDownload!!,
            downloadFlow = downloadFlow!!,
            onDismiss = {
                showDownloadDialog = false
                modelToDownload = null
                downloadFlow = null
            },
            onDownloadComplete = {
                onDownloadComplete()
                showDownloadDialog = false
                modelToDownload = null
                downloadFlow = null
            }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation && modelToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                modelToDelete = null
            },
            title = {
                Text("Delete Voice Model")
            },
            text = {
                Text("Are you sure you want to delete ${modelToDelete!!.name}? This will free up ${formatFileSize(modelToDelete!!.sizeBytes)} of storage.")
            },
            confirmButton = {
                Button(
                    onClick = confirmDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        modelToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Format file size in bytes to human-readable format
 */
private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.2f MB", mb)
        kb >= 1.0 -> String.format("%.2f KB", kb)
        else -> "$bytes B"
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipDropdown(
    label: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        FilterChip(
            selected = selectedOption != null,
            onClick = { expanded = true },
            label = { 
                Text(
                    text = selectedOption ?: label,
                    maxLines = 1
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Clear option
            if (selectedOption != null) {
                DropdownMenuItem(
                    text = { Text("All") },
                    onClick = {
                        onOptionSelected(null)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                )
                Divider()
            }
            
            // Options
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    leadingIcon = if (option == selectedOption) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}
