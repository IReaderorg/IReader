package org.ireader.presentation.feature_reader.presentation.reader.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import org.ireader.core_ui.theme.fonts
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenPreferencesState
import org.ireader.core_ui.ui_components.reusable_composable.CaptionTextComposable


@Composable
fun FontChip(
    modifier: Modifier = Modifier,
    state: ReaderScreenPreferencesState,
    onFontSelected: (Int) -> Unit,
) {

    Row(modifier = Modifier
        .fillMaxWidth()
        .height(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.width(100.dp),
            text = "Font",
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400)
        )
        LazyRow {
            items(count = fonts.size) { index ->
                Spacer(modifier = modifier.width(10.dp))
                Box(modifier = modifier
                    .height(30.dp)
                    .clip(RectangleShape)
                    .background(MaterialTheme.colors.background)
                    .border(2.dp,
                        if (fonts[index] == state.font) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground.copy(
                            .4f),
                        CircleShape)
                    .clickable {
                        onFontSelected(index)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    CaptionTextComposable(text = fonts[index].fontName,
                        maxLine = 1,
                        align = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                }
            }
        }

    }


}


