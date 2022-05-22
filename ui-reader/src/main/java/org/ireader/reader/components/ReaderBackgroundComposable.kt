package org.ireader.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ireader.components.components.component.PreferenceRow
import org.ireader.core_ui.theme.readerScreenBackgroundColors
import org.ireader.reader.viewmodel.ReaderScreenViewModel
import org.ireader.ui_reader.R

@Composable
fun ReaderBackgroundComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
    onBackgroundChange: (Int) -> Unit
) {

    PreferenceRow(title = stringResource(R.string.background_color), action = {
        LazyRow {
            items(readerScreenBackgroundColors.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color = readerScreenBackgroundColors[index].color)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                        .clickable {
                            onBackgroundChange(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.backgroundColor.value == readerScreenBackgroundColors[index].color) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "color selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    })

//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        CaptionTextComposable(
//            modifier = Modifier.width(100.dp),
//            text = stringResource(R.string.background_color),
//            style = MaterialTheme.typography.labelSmall
//        )
//        LazyRow {
//            items(readerScreenBackgroundColors.size) { index ->
//                Spacer(modifier = modifier.width(10.dp))
//                Box(
//                    modifier = modifier
//                        .width(50.dp)
//                        .height(30.dp)
//                        .padding(horizontal = 0.dp)
//                        .clip(CircleShape)
//                        .background(color = readerScreenBackgroundColors[index].color)
//                        .border(
//                            2.dp,
//                            MaterialTheme.colorScheme.primary,
//                            CircleShape
//                        )
//                        .clickable {
//                            onBackgroundChange(index)
//                        },
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (viewModel.backgroundColor.value == readerScreenBackgroundColors[index].color) {
//                        Icon(
//                            imageVector = Icons.Default.Check,
//                            contentDescription = "color selected",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
//            }
//        }
//    }
}
