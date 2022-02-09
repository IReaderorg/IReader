package org.ireader.presentation.feature_reader.presentation.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.ireader.core_ui.theme.readerScreenBackgroundColors
import org.ireader.domain.view_models.reader.ReaderScreenViewModel

@Composable
fun ReaderBackgroundComposable(modifier: Modifier = Modifier, viewModel: ReaderScreenViewModel) {

    Column {
        Text(text = "Background Color", style = MaterialTheme.typography.subtitle2)
        Spacer(modifier = modifier.height(10.dp))
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
                    .clickable { viewModel.changeBackgroundColor(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.prefState.backgroundColor == readerScreenBackgroundColors[index].color) {
                        Icon(imageVector = Icons.Default.Check,
                            contentDescription = "color selected",
                            tint = MaterialTheme.colors.primary)

                    }

                }
            }
        }

    }
}