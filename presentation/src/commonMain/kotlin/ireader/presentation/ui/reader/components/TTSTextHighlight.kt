package ireader.presentation.ui.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Component for highlighting currently playing text in TTS
 * Requirements: 10.3, 10.4
 */
@Composable
fun TTSHighlightedText(
    text: String,
    highlightedRange: IntRange?,
    modifier: Modifier = Modifier,
    highlightColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val annotatedText = if (highlightedRange != null && highlightedRange.first >= 0 && highlightedRange.last < text.length) {
        buildAnnotatedString {
            // Text before highlight
            if (highlightedRange.first > 0) {
                append(text.substring(0, highlightedRange.first))
            }
            
            // Highlighted text
            withStyle(
                style = SpanStyle(
                    background = highlightColor,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                append(text.substring(highlightedRange.first, highlightedRange.last + 1))
            }
            
            // Text after highlight
            if (highlightedRange.last < text.length - 1) {
                append(text.substring(highlightedRange.last + 1))
            }
        }
    } else {
        AnnotatedString(text)
    }
    
    Text(
        text = annotatedText,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge
    )
}

/**
 * Calculate text range for current word being spoken
 */
fun calculateCurrentWordRange(
    text: String,
    currentPosition: Int
): IntRange? {
    if (currentPosition < 0 || currentPosition >= text.length) {
        return null
    }
    
    // Find word boundaries
    var start = currentPosition
    var end = currentPosition
    
    // Find start of word
    while (start > 0 && !text[start - 1].isWhitespace()) {
        start--
    }
    
    // Find end of word
    while (end < text.length - 1 && !text[end + 1].isWhitespace()) {
        end++
    }
    
    return start..end
}

/**
 * Calculate text range for current sentence being spoken
 */
fun calculateCurrentSentenceRange(
    text: String,
    currentPosition: Int
): IntRange? {
    if (currentPosition < 0 || currentPosition >= text.length) {
        return null
    }
    
    // Find sentence boundaries
    var start = currentPosition
    var end = currentPosition
    
    // Find start of sentence
    while (start > 0) {
        val char = text[start - 1]
        if (char == '.' || char == '!' || char == '?' || char == '\n') {
            break
        }
        start--
    }
    
    // Skip leading whitespace
    while (start < text.length && text[start].isWhitespace()) {
        start++
    }
    
    // Find end of sentence
    while (end < text.length - 1) {
        val char = text[end]
        if (char == '.' || char == '!' || char == '?') {
            break
        }
        end++
    }
    
    return start..end
}
