package ireader.presentation.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import ireader.domain.services.tts_service.DesktopTTSService
import kotlinx.coroutines.delay

@Composable
fun TTSTextWithHighlighting(
    text: String,
    ttsService: DesktopTTSService,
    modifier: Modifier = Modifier
) {
    val ttsState = ttsService.state as ireader.domain.services.tts_service.DesktopTTSState
    
    // Poll for word boundary changes
    var currentBoundary by remember { mutableStateOf(ttsState.currentWordBoundary) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentBoundary = ttsState.currentWordBoundary
            delay(16) // ~60fps
        }
    }
    
    val annotatedText = if (currentBoundary != null && ttsState.isPlaying) {
        buildAnnotatedString {
            val boundary = currentBoundary!!
            val startOffset = boundary.startOffset
            val endOffset = boundary.endOffset
            
            if (startOffset >= 0 && endOffset < text.length && startOffset <= endOffset) {
                // Text before highlight
                if (startOffset > 0) {
                    append(text.substring(0, startOffset))
                }
                
                // Highlighted word
                withStyle(
                    style = SpanStyle(
                        background = Color(0xFFFFC107).copy(alpha = 0.5f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    append(text.substring(startOffset, endOffset + 1))
                }
                
                // Text after highlight
                if (endOffset < text.length - 1) {
                    append(text.substring(endOffset + 1))
                }
            } else {
                append(text)
            }
        }
    } else {
        buildAnnotatedString { append(text) }
    }
    
    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    )
}
