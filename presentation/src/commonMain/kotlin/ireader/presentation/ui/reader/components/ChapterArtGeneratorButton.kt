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
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Button to generate character art from current chapter text.
 * Shows in the reader bottom bar when enabled.
 */
@Composable
fun ChapterArtGeneratorButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Brush,
            contentDescription = localizeHelper.localize(Res.string.generate_chapter_art),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(localizeHelper.localize(Res.string.generate_chapter_art)) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.generate_an_ai_image_prompt_from_this_chapter),
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
                    text = localizeHelper.localize(Res.string.what_should_the_ai_focus_on),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { onGenerate(PromptFocus.AUTO) }) {
                    Text(localizeHelper.localize(Res.string.auto))
                }
                TextButton(onClick = { onGenerate(PromptFocus.CHARACTER) }) {
                    Text(localizeHelper.localize(Res.string.character))
                }
                TextButton(onClick = { onGenerate(PromptFocus.SCENE) }) {
                    Text(localizeHelper.localize(Res.string.scene))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = { /* Don't dismiss while generating */ },
        title = { Text(localizeHelper.localize(Res.string.generating_prompt)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = localizeHelper.localize(Res.string.analyzing_chapter_text_with_gemini_ai),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                Text(localizeHelper.localize(Res.string.generated_prompt))
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
                    text = localizeHelper.localize(Res.string.you_can_use_this_prompt_to_generate_character_art),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onProceedToUpload) {
                Text(localizeHelper.localize(Res.string.create_art))
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
                    Text(localizeHelper.localize(Res.string.close))
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.generation_failed)) },
        text = {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onRetry) {
                Text(localizeHelper.localize(Res.string.notification_retry))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.close))
            }
        }
    )
}
