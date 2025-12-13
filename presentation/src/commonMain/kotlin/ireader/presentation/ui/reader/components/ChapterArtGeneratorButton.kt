package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.data.characterart.PromptFocus

/**
 * Button to generate character art from current chapter text.
 * Shows in the reader bottom bar when enabled.
 */
@Composable
fun ChapterArtGeneratorButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Brush,
            contentDescription = "Generate Chapter Art",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Dialog for selecting prompt focus type before generating
 */
@Composable
fun ChapterArtFocusDialog(
    bookTitle: String,
    chapterTitle: String,
    onDismiss: () -> Unit,
    onGenerate: (PromptFocus) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Generate Chapter Art") 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Generate an AI image prompt from this chapter",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Book: $bookTitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Chapter: $chapterTitle",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "What should the AI focus on?",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { onGenerate(PromptFocus.AUTO) }) {
                    Text("Auto")
                }
                TextButton(onClick = { onGenerate(PromptFocus.CHARACTER) }) {
                    Text("Character")
                }
                TextButton(onClick = { onGenerate(PromptFocus.SCENE) }) {
                    Text("Scene")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Loading dialog while generating prompt
 */
@Composable
fun ChapterArtGeneratingDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Don't dismiss while generating */ },
        title = { Text("Generating Prompt...") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Analyzing chapter text with Gemini AI...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Result dialog showing generated prompt
 */
@Composable
fun ChapterArtPromptResultDialog(
    prompt: String,
    bookTitle: String,
    chapterTitle: String,
    onDismiss: () -> Unit,
    onProceedToUpload: () -> Unit,
    onCopyPrompt: () -> Unit
) {
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Generated Prompt")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                Text(
                    text = "You can use this prompt to generate character art",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onProceedToUpload) {
                Text("Create Art")
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(prompt))
                        copied = true
                        onCopyPrompt()
                    }
                ) {
                    Text(if (copied) "Copied!" else "Copy")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

/**
 * Error dialog for prompt generation failure
 */
@Composable
fun ChapterArtErrorDialog(
    error: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generation Failed") },
        text = {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
