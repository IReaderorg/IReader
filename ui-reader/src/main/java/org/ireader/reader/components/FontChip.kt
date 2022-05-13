package org.ireader.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ireader.common_resources.UiText
import org.ireader.components.reusable_composable.CaptionTextComposable
import org.ireader.core_ui.theme.fonts
import org.ireader.reader.viewmodel.ReaderScreenPreferencesState

@Composable
fun FontChip(
    modifier: Modifier = Modifier,
    state: ReaderScreenPreferencesState,
    onFontSelected: (Int) -> Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.width(100.dp),
            text = "Font",
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400),
            color = MaterialTheme.colorScheme.onSurface
        )
        LazyRow {
            items(count = fonts.size) { index ->
                Spacer(modifier = modifier.width(10.dp))
                Box(
                    modifier = modifier
                        .height(30.dp)
                        .clip(RectangleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .border(
                            2.dp,
                            if (fonts[index] == state.font) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                                .4f
                            ),
                            CircleShape
                        )
                        .clickable {
                            onFontSelected(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CaptionTextComposable(
                        text = UiText.DynamicString(fonts[index].fontName),
                        maxLine = 1,
                        align = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
