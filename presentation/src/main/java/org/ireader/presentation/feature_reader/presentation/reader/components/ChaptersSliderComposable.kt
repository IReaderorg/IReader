package org.ireader.presentation.feature_reader.presentation.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.ireader.common_models.entities.Chapter


@Composable
fun ChaptersSliderComposable(
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    currentChapter: Chapter,
    currentChapterIndex: Int,
    chapters: List<Chapter>,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSliderChange: (index: Float) -> Unit,
    onSliderDragFinished: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = if (chapters.isNotEmpty() && currentChapterIndex != -1) chapters[currentChapterIndex].title else currentChapter.title,
            color = MaterialTheme.colors.onBackground,
            style = MaterialTheme.typography.subtitle2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(modifier = modifier.weight(1f),
                onClick = {
                    onPrev()
                }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Previous Chapter")
            }
            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(6f),
                value = if (chapters.isEmpty()) 0F else currentChapterIndex.toFloat(),
                onValueChange = {
                    onSliderChange(it)
                },
                onValueChangeFinished = {
                    onSliderDragFinished()
                },
                valueRange = 0f..(if (chapters.isNotEmpty()) chapters.size - 1 else 0).toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colors.primary,
                    activeTrackColor = MaterialTheme.colors.primary.copy(alpha = .6f),
                    inactiveTickColor = MaterialTheme.colors.onBackground.copy(alpha = .6f),
                    inactiveTrackColor = MaterialTheme.colors.onBackground.copy(alpha = .6f),
                    activeTickColor = MaterialTheme.colors.primary.copy(alpha = .6f)
                ),
            )
            IconButton(modifier = modifier.weight(1f), onClick = {
                onNext()
            }) {
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next Chapter")
            }
        }
    }
}