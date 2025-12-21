package ireader.presentation.ui.home.tts

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.currentOrThrow
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Android implementation of TTS Engine Settings Screen
 * 
 * Shows options to:
 * - Open Android system TTS settings (for native TTS)
 * - Open IReader TTS settings (for Coqui TTS configuration)
 */
@Composable
actual fun TTSEngineSettingsScreen(
    isDesktop: Boolean,
    onDismiss: () -> Unit,
    onNavigateToTTSManager: () -> Unit
) {
    val context = LocalContext.current
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(localizeHelper.localize(Res.string.tts_engine_settings))
        },
        text = {
            TTSEngineSettingsContent(
                context = context,
                localizeHelper = localizeHelper,
                onDismiss = onDismiss,
                onNavigateToTTSManager = onNavigateToTTSManager
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.close))
            }
        }
    )
}

@Composable
private fun TTSEngineSettingsContent(
    context: Context,
    localizeHelper: ireader.i18n.LocalizeHelper,
    onDismiss: () -> Unit,
    onNavigateToTTSManager: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = localizeHelper.localize(Res.string.choose_which_tts_settings_to_open),
            style = MaterialTheme.typography.bodyMedium
        )
        
        SystemTTSSettingsCard(
            context = context,
            localizeHelper = localizeHelper,
            onDismiss = onDismiss
        )
        
        OnlineTTSEnginesCard(
            localizeHelper = localizeHelper,
            onNavigateToTTSManager = onNavigateToTTSManager,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun SystemTTSSettingsCard(
    context: Context,
    localizeHelper: ireader.i18n.LocalizeHelper,
    onDismiss: () -> Unit
) {
    OutlinedCard(
        onClick = {
            openAndroidSystemTTSSettings(context)
            onDismiss()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.SettingsVoice,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localizeHelper.localize(Res.string.system_tts_settings),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = localizeHelper.localize(Res.string.configure_androids_built_in_text_to_speech_engine),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun OnlineTTSEnginesCard(
    localizeHelper: ireader.i18n.LocalizeHelper,
    onNavigateToTTSManager: () -> Unit,
    onDismiss: () -> Unit
) {
    OutlinedCard(
        onClick = {
            onNavigateToTTSManager()
            onDismiss()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Cloud,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = localizeHelper.localize(Res.string.online_tts_engines),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = localizeHelper.localize(Res.string.configure_gradio_based_tts_coqui),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

/**
 * Open Android system TTS settings
 */
private fun openAndroidSystemTTSSettings(context: android.content.Context) {
    val intent = Intent().apply {
        action = "com.android.settings.TTS_SETTINGS"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to accessibility settings which contains TTS
        try {
            val fallbackIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallbackIntent)
        } catch (e2: Exception) {
            // Last resort: open general settings
            val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(settingsIntent)
        }
    }
}


/**
 * Android implementation of TTS Voice Selection Screen
 * 
 * Shows available system TTS voices
 */
@Composable
actual fun TTSVoiceSelectionScreen(
    isDesktop: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(localizeHelper.localize(Res.string.voice_selection))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.voice_selection_is_managed_through),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedCard(
                    onClick = {
                        openAndroidSystemTTSSettings(context)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
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
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = localizeHelper.localize(Res.string.open_tts_settings),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = localizeHelper.localize(Res.string.select_voice_language_and_speech_rate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.close))
            }
        }
    )
}
