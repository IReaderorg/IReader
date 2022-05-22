package org.ireader.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.ireader.common_models.entities.Chapter
import org.ireader.components.reusable_composable.AppIcon

@Composable
fun ChaptersSliderComposable(
    modifier: Modifier = Modifier,
    currentChapter: Chapter?,
    currentChapterIndex: Int,
    chapters: List<Chapter>,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSliderChange: (index: Float) -> Unit,
    onSliderDragFinished: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (chapters.isNotEmpty() && currentChapterIndex != -1) chapters[currentChapterIndex].name else currentChapter?.name?:"",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            
            IconButton(
                modifier = modifier.weight(1f),
                onClick = {
                    onPrev()
                },
            ) {
                AppIcon(imageVector = Icons.Default.ArrowBack, contentDescription = "Previous Chapter")
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
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = .6f),
                    inactiveTickColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f),
                    inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .6f),
                    activeTickColor = MaterialTheme.colorScheme.primary.copy(alpha = .6f)
                ),
            )
            IconButton(modifier = modifier.weight(1f), onClick = {
                onNext()
            }) {
                AppIcon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next Chapter")
            }
        }
    }
}
