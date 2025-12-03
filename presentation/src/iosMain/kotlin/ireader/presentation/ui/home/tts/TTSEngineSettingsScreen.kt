package ireader.presentation.ui.home.tts

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ireader.i18n.resources.Res
import ireader.i18n.resources.tts_engine_manager
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.theme.currentOrThrow
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TTSEngineSettingsScreen(
    isDesktop: Boolean,
    onDismiss: () -> Unit,
    onNavigateToTTSManager: () -> Unit
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
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
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "iOS Text-to-Speech",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "iOS uses the built-in AVSpeechSynthesizer for text-to-speech. " +
                                   "Voice settings can be configured in iOS Settings > Accessibility > Spoken Content.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        NSURL.URLWithString("App-Prefs:root=ACCESSIBILITY")?.let { url ->
                            UIApplication.sharedApplication.openURL(url)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open iOS Settings")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TTSVoiceSelectionScreen(
    isDesktop: Boolean,
    onDismiss: () -> Unit
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voice Selection",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                Text(
                    text = "iOS voices are managed through the system. " +
                           "You can download additional voices in iOS Settings > Accessibility > Spoken Content > Voices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = {
                        NSURL.URLWithString("App-Prefs:root=ACCESSIBILITY")?.let { url ->
                            UIApplication.sharedApplication.openURL(url)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open iOS Settings")
                }
            }
        }
    }
}
