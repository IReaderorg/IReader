package ireader.presentation.ui.home.history.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.cancel
import ireader.i18n.resources.confirm
import ireader.presentation.ui.component.reusable_composable.WarningAlertData

@Composable
fun WarningAlertDialog(data: WarningAlertData) {
    AlertDialog(
        onDismissRequest = { 
            data.onDismiss.value?.invoke() ?: run {
                data.enable = false
            }
        },
        title = { 
            Text(text = data.title.value?.toString() ?: "") 
        },
        text = { 
            Text(text = data.text.value?.toString() ?: "") 
        },
        confirmButton = {
            TextButton(
                onClick = {
                    data.onConfirm.value?.invoke() ?: run {
                        data.enable = false
                    }
                }
            ) {
                Text(
                    text = localize(Res.string.confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    data.onDismiss.value?.invoke() ?: run {
                        data.enable = false
                    }
                }
            ) {
                Text(text = localize(Res.string.cancel))
            }
        }
    )
}

@Composable
fun HighlightedText(
    text: String,
    searchQuery: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    color: Color = Color.Unspecified
) {
    if (searchQuery.isBlank()) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            maxLines = maxLines,
            overflow = overflow,
            color = color
        )
    } else {
        val annotatedString = buildAnnotatedString {
            var currentIndex = 0
            val lowerText = text.lowercase()
            val lowerQuery = searchQuery.lowercase()
            
            while (currentIndex < text.length) {
                val matchIndex = lowerText.indexOf(lowerQuery, currentIndex)
                if (matchIndex == -1) {
                    append(text.substring(currentIndex))
                    break
                }
                
                // Add text before match
                if (matchIndex > currentIndex) {
                    append(text.substring(currentIndex, matchIndex))
                }
                
                // Add highlighted match
                withStyle(
                    style = SpanStyle(
                        background = MaterialTheme.colorScheme.primaryContainer,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(text.substring(matchIndex, matchIndex + searchQuery.length))
                }
                
                currentIndex = matchIndex + searchQuery.length
            }
        }
        
        Text(
            text = annotatedString,
            modifier = modifier,
            style = style,
            maxLines = maxLines,
            overflow = overflow,
            color = if (color != Color.Unspecified) color else LocalContentColor.current
        )
    }
}
