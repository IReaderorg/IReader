package ireader.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.settings.components.GradioConfigEditDialog
import ireader.presentation.ui.settings.components.GradioTTSSection
import ireader.presentation.ui.settings.viewmodels.AITTSSettingsViewModel
import ireader.presentation.ui.settings.viewmodels.GradioTTSSettingsViewModel
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.tts_engine_manager)) },
                navigationIcon = {
                    AppIconButton(
                        imageVector = Icons.Default.ArrowBack,
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
                                text = "Recommended: Sherpa TTS App",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Text(
                            text = "For more powerful and natural-sounding voices on Android, install the Sherpa TTS app from the Play Store or F-Droid.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = "✓ High-quality neural voices\n✓ Works offline\n✓ Multiple languages\n✓ Integrates with Android TTS",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Once installed, go to Android Settings → Accessibility → Text-to-speech → Preferred engine and select Sherpa TTS.",
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
                        text = "Online TTS Engines",
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
                            text = "Native Android TTS",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "IReader uses your device's built-in Text-to-Speech engine. You can configure voices in Android Settings → Accessibility → Text-to-speech.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ System integration • ✓ Multiple engines supported • ✓ No downloads needed",
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



