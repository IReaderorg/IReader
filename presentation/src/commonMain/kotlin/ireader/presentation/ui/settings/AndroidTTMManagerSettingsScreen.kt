package ireader.presentation.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.resources.*
import ireader.i18n.resources.back
import ireader.i18n.resources.for_more_powerful_and_natural
import ireader.i18n.resources.high_quality_neural_voicesn_works
import ireader.i18n.resources.ireader_uses_your_devices_built
import ireader.i18n.resources.native_android_tts
import ireader.i18n.resources.once_installed_go_to_android
import ireader.i18n.resources.online_tts_engines
import ireader.i18n.resources.recommended_sherpa_tts_app
import ireader.i18n.resources.system_integration_multiple_engines_supported
import ireader.i18n.resources.tts_engine_manager
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.components.GradioConfigEditDialog
import ireader.presentation.ui.settings.components.GradioTTSSection
import ireader.presentation.ui.settings.components.TTSMergeAndCacheSection
import ireader.presentation.ui.settings.viewmodels.AITTSSettingsViewModel
import ireader.presentation.ui.settings.viewmodels.GradioTTSSettingsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidTTSMManagerSettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: AITTSSettingsViewModel,
    gradioViewModel: GradioTTSSettingsViewModel? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val state by viewModel.state.collectAsState()
    val gradioState = gradioViewModel?.state?.collectAsState()?.value
    val readerPreferences: ReaderPreferences = koinInject()
    val chapterCache: ireader.domain.services.tts_service.TTSChapterCache = koinInject()
    val ttsController: ireader.domain.services.tts_service.v2.TTSController = koinInject()
    val scope = rememberCoroutineScope()
    
    // TTS Merge settings state
    var mergeWordsRemote by remember { mutableStateOf(readerPreferences.ttsMergeWordsRemote().get()) }
    var mergeWordsNative by remember { mutableStateOf(readerPreferences.ttsMergeWordsNative().get()) }
    
    // Chapter cache settings state
    var chapterCacheEnabled by remember { mutableStateOf(readerPreferences.ttsChapterCacheEnabled().get()) }
    var chapterCacheDays by remember { mutableStateOf(readerPreferences.ttsChapterCacheDays().get()) }
    
    // Cache stats from TTSChapterCache
    var cacheStats by remember { mutableStateOf(chapterCache.getCacheStats()) }
    val cacheEntryCount = cacheStats.entryCount
    val cacheSizeMB = cacheStats.totalSizeMB
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.tts_engine_manager)) },
                navigationIcon = {
                    AppIconButton(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = localizeHelper.localize(Res.string.back),
                        onClick = onBackPressed
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sherpa TTS App Recommendation (Android)
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
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
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
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
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = localizeHelper.localize(Res.string.once_installed_go_to_android),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            

            // Gradio TTS Section (Online TTS engines including Gradio)
            if (gradioViewModel != null && gradioState != null) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.online_tts_engines),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                item {
                    GradioTTSSection(
                        useGradioTTS = gradioState.useGradioTTS,
                        onUseGradioTTSChange = { gradioViewModel.setUseGradioTTS(it) },
                        configs = gradioState.configs,
                        activeConfigId = gradioState.activeConfigId,
                        onSelectConfig = { gradioViewModel.setActiveConfig(it) },
                        onTestConfig = { gradioViewModel.testConfig(it) },
                        onEditConfig = { gradioViewModel.openEditDialog(it) },
                        onDeleteConfig = { gradioViewModel.deleteConfig(it) },
                        onAddCustomConfig = { gradioViewModel.createNewCustomConfig() },
                        globalSpeed = gradioState.globalSpeed,
                        onGlobalSpeedChange = { gradioViewModel.setGlobalSpeed(it) },
                        isTesting = gradioState.isTesting,
                        testingConfigId = gradioState.activeConfigId
                    )
                }
            }
            
            // TTS Text Merging and Chapter Caching Section
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                TTSMergeAndCacheSection(
                    mergeWordsRemote = mergeWordsRemote,
                    onMergeWordsRemoteChange = { value ->
                        mergeWordsRemote = value
                        scope.launch { 
                            readerPreferences.ttsMergeWordsRemote().set(value)
                            // Re-enable chunk mode with new word count to re-merge paragraphs
                            if (value > 0) {
                                ttsController.dispatch(ireader.domain.services.tts_service.v2.TTSCommand.EnableChunkMode(value))
                            } else {
                                ttsController.dispatch(ireader.domain.services.tts_service.v2.TTSCommand.DisableChunkMode)
                            }
                        }
                    },
                    mergeWordsNative = mergeWordsNative,
                    onMergeWordsNativeChange = { value ->
                        mergeWordsNative = value
                        scope.launch { readerPreferences.ttsMergeWordsNative().set(value) }
                    },
                    chapterCacheEnabled = chapterCacheEnabled,
                    onChapterCacheEnabledChange = { enabled ->
                        chapterCacheEnabled = enabled
                        scope.launch { readerPreferences.ttsChapterCacheEnabled().set(enabled) }
                    },
                    chapterCacheDays = chapterCacheDays,
                    onChapterCacheDaysChange = { days ->
                        chapterCacheDays = days
                        scope.launch { readerPreferences.ttsChapterCacheDays().set(days) }
                    },
                    cacheEntryCount = cacheEntryCount,
                    cacheSizeMB = cacheSizeMB,
                    onClearCache = {
                        // Clear all cached chapter audio
                        chapterCache.clearAll()
                        // Refresh cache stats
                        cacheStats = chapterCache.getCacheStats()
                    }
                )
            }
            
            // Native TTS Info
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.native_android_tts),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.ireader_uses_your_devices_built),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.system_integration_multiple_engines_supported),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Error message
            state.error?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Edit dialog for Gradio config
    if (gradioViewModel != null && gradioState != null && gradioState.isEditDialogOpen && gradioState.editingConfig != null) {
        GradioConfigEditDialog(
            config = gradioState.editingConfig,
            onDismiss = { gradioViewModel.closeEditDialog() },
            onSave = { gradioViewModel.saveEditingConfig(it) }
        )
    }
}



