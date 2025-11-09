package ireader.presentation.ui.reader.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SelectableTranslatableText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    color: Color = Color.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    selectable: Boolean = true,
    onTextSelected: (String) -> Unit = {},
    onTranslateRequest: (String) -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    var showContextMenu by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }
    var menuPosition by remember { mutableStateOf(Offset.Zero) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    
    Box(modifier = modifier) {
        if (selectable) {
            SelectionContainer {
                Text(
                    text = text,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    textAlign = textAlign,
                    color = color,
                    lineHeight = lineHeight,
                    letterSpacing = letterSpacing,
                    fontWeight = fontWeight,
                    onTextLayout = { textLayoutResult = it },
                    modifier = Modifier.pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val longPressTimeout = withTimeoutOrNull(500L) {
                                waitForUpOrCancellation()
                            }
                            
                            if (longPressTimeout == null) {
                                // Long press detected - consume the event
                                down.consume()
                                val offset = down.position
                                textLayoutResult?.let { layout ->
                                    val position = layout.getOffsetForPosition(offset)
                                    val paragraphText = extractParagraphAtPosition(text, position)
                                    if (paragraphText.isNotBlank()) {
                                        selectedText = paragraphText
                                        menuPosition = offset
                                        showContextMenu = true
                                    }
                                }
                            }
                            // If it's a tap (released before timeout), do nothing - let it pass through
                        }
                    }
                )
            }
        } else {
            Text(
                text = text,
                fontSize = fontSize,
                fontFamily = fontFamily,
                textAlign = textAlign,
                color = color,
                lineHeight = lineHeight,
                letterSpacing = letterSpacing,
                fontWeight = fontWeight,
                onTextLayout = { textLayoutResult = it },
                modifier = Modifier.pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val longPressTimeout = withTimeoutOrNull(500L) {
                            waitForUpOrCancellation()
                        }
                        
                        if (longPressTimeout == null) {
                            // Long press detected - consume the event
                            down.consume()
                            val offset = down.position
                            textLayoutResult?.let { layout ->
                                val position = layout.getOffsetForPosition(offset)
                                val paragraphText = extractParagraphAtPosition(text, position)
                                if (paragraphText.isNotBlank()) {
                                    selectedText = paragraphText
                                    menuPosition = offset
                                    showContextMenu = true
                                }
                            }
                        }
                        // If it's a tap (released before timeout), do nothing - let it pass through
                    }
                }
            )
        }
        
        if (showContextMenu && selectedText.isNotBlank()) {
            ParagraphTranslationMenu(
                selectedText = selectedText,
                onTranslate = {
                    onTranslateRequest(selectedText)
                },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(selectedText))
                },
                onDismiss = {
                    showContextMenu = false
                    selectedText = ""
                }
            )
        }
    }
}

/**
 * Extract the paragraph containing the given position
 * A paragraph is defined as text between newlines or the entire text if no newlines
 */
private fun extractParagraphAtPosition(text: String, position: Int): String {
    if (position < 0 || position >= text.length) return ""
    
    // Find the start of the paragraph (previous newline or start of text)
    var start = position
    while (start > 0 && text[start - 1] != '\n') {
        start--
    }
    
    // Find the end of the paragraph (next newline or end of text)
    var end = position
    while (end < text.length && text[end] != '\n') {
        end++
    }
    
    return text.substring(start, end).trim()
}
