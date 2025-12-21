package ireader.presentation.ui.settings.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.resources.Res
import ireader.i18n.resources.all
import ireader.i18n.resources.back
import ireader.i18n.resources.dismiss
import ireader.i18n.resources.search_voices
import ireader.i18n.resources.select_voice
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.components.VoiceCard
import ireader.presentation.ui.settings.viewmodels.VoiceSelectionViewModel

/**
 * Voice selection screen for choosing TTS voices
 * Requirements: 4.1, 4.2, 4.3, 10.1
 */
@Composable
fun VoiceSelectionScreen(
    viewModel: VoiceSelectionViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val installedVoices by viewModel.installedVoices.collectAsState()
    
    VoiceSelectionContent(
        state = state,
        downloadProgress = downloadProgress,
        installedVoices = installedVoices.toSet(),
        onLanguageSelected = { viewModel.filterByLanguage(it) },
        onSearchQueryChanged = { viewModel.searchVoices(it) },
        onVoiceSelected = { viewModel.selectVoice(it) },
        onDownloadVoice = { viewModel.downloadVoice(it) },
        onPreviewVoice = { viewModel.previewVoice(it) },
        onDeleteVoice = { viewModel.deleteVoice(it) },
        onNavigateBack = onNavigateBack,
        onErrorDismissed = { viewModel.clearError() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSelectionContent(
    state: ireader.presentation.ui.settings.viewmodels.VoiceSelectionState,
    downloadProgress: ireader.presentation.ui.settings.viewmodels.DownloadProgress?,
    installedVoices: Set<String>,
    onLanguageSelected: (String?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onVoiceSelected: (ireader.domain.models.tts.VoiceModel) -> Unit,
    onDownloadVoice: (ireader.domain.models.tts.VoiceModel) -> Unit,
    onPreviewVoice: (ireader.domain.models.tts.VoiceModel) -> Unit,
    onDeleteVoice: (ireader.domain.models.tts.VoiceModel) -> Unit,
    onNavigateBack: () -> Unit,
    onErrorDismissed: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.select_voice)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(localizeHelper.localize(Res.string.search_voices)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            
            // Language filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                item {
                    FilterChip(
                        selected = state.selectedLanguage == null,
                        onClick = { onLanguageSelected(null) },
                        label = { Text(localizeHelper.localize(Res.string.all)) }
                    )
                }
                
                // Language chips
                items(state.supportedLanguages, key = { it }) { language ->
                    FilterChip(
                        selected = state.selectedLanguage == language,
                        onClick = { onLanguageSelected(language) },
                        label = { Text(getLanguageName(language)) }
                    )
                }
            }
            
            Divider()
            
            // Voice list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.filteredVoices,
                    key = { it.id }
                ) { voice ->
                    VoiceCard(
                        voice = voice,
                        isSelected = voice.id == state.selectedVoice?.id,
                        isDownloaded = installedVoices.contains(voice.id),
                        downloadProgress = if (downloadProgress?.voiceId == voice.id) 
                            downloadProgress.progress else null,
                        onSelect = { onVoiceSelected(voice) },
                        onDownload = { onDownloadVoice(voice) },
                        onPreview = { onPreviewVoice(voice) },
                        onDelete = { onDeleteVoice(voice) }
                    )
                }
            }
        }
        
        // Error snackbar
        state.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = onErrorDismissed) {
                        Text(localizeHelper.localize(Res.string.dismiss))
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

/**
 * Get display name for language code
 */
private fun getLanguageName(code: String): String {
    return when (code) {
        "en" -> "English"
        "es" -> "Spanish"
        "fr" -> "French"
        "de" -> "German"
        "zh" -> "Chinese"
        "ja" -> "Japanese"
        "ko" -> "Korean"
        "pt" -> "Portuguese"
        "it" -> "Italian"
        "ru" -> "Russian"
        "nl" -> "Dutch"
        "pl" -> "Polish"
        "tr" -> "Turkish"
        "ar" -> "Arabic"
        "hi" -> "Hindi"
        "sv" -> "Swedish"
        "no" -> "Norwegian"
        "da" -> "Danish"
        "fi" -> "Finnish"
        "el" -> "Greek"
        "cs" -> "Czech"
        "uk" -> "Ukrainian"
        "vi" -> "Vietnamese"
        else -> code.uppercase()
    }
}
