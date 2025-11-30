package ireader.presentation.ui.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

enum class BilingualMode {
    SIDE_BY_SIDE,
    PARAGRAPH_BY_PARAGRAPH
}

@Composable
fun BilingualText(
    originalText: String,
    translatedText: String,
    mode: BilingualMode,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    originalColor: Color = MaterialTheme.colorScheme.onSurface,
    translatedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    lineHeight: TextUnit = TextUnit.Unspecified,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null
) {
    when (mode) {
        BilingualMode.SIDE_BY_SIDE -> {
            SideBySideText(
                originalText = originalText,
                translatedText = translatedText,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = fontFamily,
                textAlign = textAlign,
                originalColor = originalColor,
                translatedColor = translatedColor,
                lineHeight = lineHeight,
                letterSpacing = letterSpacing,
                fontWeight = fontWeight
            )
        }
        BilingualMode.PARAGRAPH_BY_PARAGRAPH -> {
            ParagraphByParagraphText(
                originalText = originalText,
                translatedText = translatedText,
                modifier = modifier,
                fontSize = fontSize,
                fontFamily = fontFamily,
                textAlign = textAlign,
                originalColor = originalColor,
                translatedColor = translatedColor,
                lineHeight = lineHeight,
                letterSpacing = letterSpacing,
                fontWeight = fontWeight
            )
        }
    }
}

@Composable
private fun SideBySideText(
    originalText: String,
    translatedText: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    textAlign: TextAlign?,
    originalColor: Color,
    translatedColor: Color,
    lineHeight: TextUnit,
    letterSpacing: TextUnit,
    fontWeight: FontWeight?
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Original text column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = localizeHelper.localize(Res.string.original),
                        style = MaterialTheme.typography.labelSmall,
                        color = originalColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = originalText,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        textAlign = textAlign,
                        color = originalColor,
                        lineHeight = lineHeight,
                        letterSpacing = letterSpacing,
                        fontWeight = fontWeight
                    )
                }
            }
        }
        
        // Divider
        Divider(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
        
        // Translated text column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = localizeHelper.localize(Res.string.translation),
                        style = MaterialTheme.typography.labelSmall,
                        color = translatedColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = translatedText,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        textAlign = textAlign,
                        color = translatedColor,
                        lineHeight = lineHeight,
                        letterSpacing = letterSpacing,
                        fontWeight = fontWeight
                    )
                }
            }
        }
    }
}

@Composable
private fun ParagraphByParagraphText(
    originalText: String,
    translatedText: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit,
    fontFamily: FontFamily?,
    textAlign: TextAlign?,
    originalColor: Color,
    translatedColor: Color,
    lineHeight: TextUnit,
    letterSpacing: TextUnit,
    fontWeight: FontWeight?
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Original text
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = localizeHelper.localize(Res.string.original),
                    style = MaterialTheme.typography.labelSmall,
                    color = originalColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = originalText,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    textAlign = textAlign,
                    color = originalColor,
                    lineHeight = lineHeight,
                    letterSpacing = letterSpacing,
                    fontWeight = fontWeight
                )
            }
        }
        
        // Translated text
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = localizeHelper.localize(Res.string.translation),
                    style = MaterialTheme.typography.labelSmall,
                    color = translatedColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = translatedText,
                    fontSize = fontSize,
                    fontFamily = fontFamily,
                    textAlign = textAlign,
                    color = translatedColor,
                    lineHeight = lineHeight,
                    letterSpacing = letterSpacing,
                    fontWeight = fontWeight
                )
            }
        }
    }
}
