package org.ireader.presentation.feature_reader.presentation.reader.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderEvent
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenViewModel
import org.ireader.presentation.presentation.reusable_composable.CaptionTextComposable


@Composable
fun FontChip(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
) {

    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.width(100.dp),
            text = "Font",
            fontSize = 12.sp,
            style = TextStyle(fontWeight = FontWeight.W400)
        )
        LazyRow {
            items(fonts) { font ->
                Spacer(modifier = modifier.width(10.dp))
                Box(modifier = modifier
                    .height(30.dp)
                    .clip(RectangleShape)
                    .background(MaterialTheme.colors.background)
                    .border(2.dp,
                        if (font == viewModel.font) MaterialTheme.colors.primary else MaterialTheme.colors.onBackground.copy(
                            .4f),
                        CircleShape)
                    .clickable { viewModel.onEvent(ReaderEvent.ChangeFont(font)) },
                    contentAlignment = Alignment.Center
                ) {
                    CaptionTextComposable(text = font.fontName,
                        maxLine = 1,
                        align = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
                }
            }
        }

    }


}


