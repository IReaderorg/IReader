package ireader.presentation.ui.settings

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
import ireader.i18n.resources.*
import ireader.presentation.ui.home.tts.NativeTTSVoiceSelectionScreen

/**
 * Android implementation of Native TTS Section
 * Shows native TTS info and voice selection button
 */
@Composable
actual fun NativeAndroidTTSSection(
    localizeHelper: ireader.i18n.LocalizeHelper,
    modifier: Modifier
) {
    val context = LocalContext.current
    var showVoiceSelection by remember { mutableStateOf(false) }
    
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = localizeHelper.localize(Res.string.native_android_tts),
                style = MaterialTheme.typography.titleMedium
            )
            
            // Description
            Text(
                text = localizeHelper.localize(Res.string.ireader_uses_your_devices_built),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // System integration info
            Text(
                text = localizeHelper.localize(Res.string.system_integration_multiple_engines_supported),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voice Selection Button
                OutlinedButton(
                    onClick = { showVoiceSelection = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.voice_selection))
                }
                
                // System TTS Settings Button
                OutlinedButton(
                    onClick = { openAndroidSystemTTSSettings(context) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.system_settings))
                }
            }
        }
    }
    
    // Voice Selection Dialog
    if (showVoiceSelection) {
        NativeTTSVoiceSelectionScreen(
            onDismiss = { showVoiceSelection = false }
        )
    }
}

/**
 * Open Android system TTS settings
 */
private fun openAndroidSystemTTSSettings(context: Context) {
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
