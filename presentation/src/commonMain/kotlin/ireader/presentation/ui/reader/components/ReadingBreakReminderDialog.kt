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
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                    text = localizeHelper.localize(Res.string.snooze_reminder),
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
                        text = localizeHelper.localize(Res.string.remind_me_again_in),
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
                            Text(localizeHelper.localize(Res.string.five_min))
                        }
                        OutlinedButton(
                            onClick = {
                                onSnooze(10)
                                showSnoozeOptions = false
                            }
                        ) {
                            Text(localizeHelper.localize(Res.string.ten_min))
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
                            Text(localizeHelper.localize(Res.string.fifteen_min))
                        }
                        OutlinedButton(
                            onClick = {
                                onSnooze(30)
                                showSnoozeOptions = false
                            }
                        ) {
                            Text(localizeHelper.localize(Res.string.thirty_min))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSnoozeOptions = false }) {
                    Text(localizeHelper.localize(Res.string.cancel))
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
                    contentDescription = localizeHelper.localize(Res.string.reading_break),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = localizeHelper.localize(Res.string.time_for_a_break),
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
                        text = localizeHelper.localize(Res.string.time_to_stretch_your_eyes_and_rest_a_bit),
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
                    Text(localizeHelper.localize(Res.string.continue_reading))
                }
            },
            dismissButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSnoozeOptions = true }
                    ) {
                        Text(localizeHelper.localize(Res.string.snooze))
                    }
                    OutlinedButton(
                        onClick = onTakeBreak
                    ) {
                        Text(localizeHelper.localize(Res.string.take_a_break))
                    }
                }
            }
        )
    }
}
