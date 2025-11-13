package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dialog that reminds users to take a reading break with snooze functionality
 * Requirements: 17.1, 17.2, 17.3, 17.4, 17.5
 * Auto-dismisses after 15 seconds if no action is taken
 */
@Composable
fun ReadingBreakReminderDialog(
    intervalMinutes: Int,
    onTakeBreak: () -> Unit,
    onContinueReading: () -> Unit,
    onSnooze: (minutes: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var countdown by remember { mutableStateOf(15) }
    var showSnoozeOptions by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Auto-dismiss countdown
    LaunchedEffect(Unit) {
        scope.launch {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            // Auto-dismiss and continue reading
            onContinueReading()
        }
    }
    
    if (showSnoozeOptions) {
        // Snooze options dialog
        AlertDialog(
            onDismissRequest = { showSnoozeOptions = false },
            title = {
                Text(
                    text = "Snooze Reminder",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Remind me again in:",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Snooze buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSnooze(5)
                                showSnoozeOptions = false
                            }
                        ) {
                            Text("5 min")
                        }
                        OutlinedButton(
                            onClick = {
                                onSnooze(10)
                                showSnoozeOptions = false
                            }
                        ) {
                            Text("10 min")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSnooze(15)
                                showSnoozeOptions = false
                            }
                        ) {
                            Text("15 min")
                        }
                        OutlinedButton(
                            onClick = {
                                onSnooze(30)
                                showSnoozeOptions = false
                            }
                        ) {
                            Text("30 min")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSnoozeOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    } else {
        // Main reminder dialog
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "Reading Break",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = "Time for a Break!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "You've been reading for $intervalMinutes minutes.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Time to stretch your eyes and rest a bit! 👀",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Auto-continuing in $countdown seconds...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onContinueReading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Continue Reading")
                }
            },
            dismissButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSnoozeOptions = true }
                    ) {
                        Text("Snooze")
                    }
                    OutlinedButton(
                        onClick = onTakeBreak
                    ) {
                        Text("Take a Break")
                    }
                }
            }
        )
    }
}
