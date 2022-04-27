package org.ireader.presentation.feature_reader.presentation.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.ireader.core_ui.theme.readerScreenBackgroundColors
import org.ireader.presentation.feature_reader.presentation.reader.viewmodel.ReaderScreenPreferencesState
import org.ireader.core_ui.ui_components.reusable_composable.CaptionTextComposable

@Composable
fun ReaderBackgroundComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenPreferencesState,
    onBackgroundChange:(Int) -> Unit
) {

    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        CaptionTextComposable(modifier = Modifier.width(100.dp),
            text = "Background Color",
            style = MaterialTheme.typography.caption)
        LazyRow {
            items(readerScreenBackgroundColors.size) { index ->
                Spacer(modifier = modifier.width(10.dp))
                Box(modifier = modifier
                    .width(50.dp)
                    .height(30.dp)
                    .padding(horizontal = 0.dp)
                    .clip(CircleShape)
                    .background(color = readerScreenBackgroundColors[index].color)
                    .border(2.dp,
                        MaterialTheme.colors.primary,
                        CircleShape)
                    .clickable {
                        onBackgroundChange(index)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.backgroundColor == readerScreenBackgroundColors[index].color) {
                        Icon(imageVector = Icons.Default.Check,
                            contentDescription = "color selected",
                            tint = MaterialTheme.colors.primary)

                    }

                }
            }
        }

    }
}