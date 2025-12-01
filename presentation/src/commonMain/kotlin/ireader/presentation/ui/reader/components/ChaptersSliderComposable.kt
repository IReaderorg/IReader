package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import ireader.domain.models.entities.Chapter
import ireader.i18n.resources.Res
import ireader.i18n.resources.next_chapter_1
import ireader.i18n.resources.previous_chapter_1
import ireader.presentation.ui.component.reusable_composable.AppIcon
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (chapters.isNotEmpty() && currentChapterIndex != -1) chapters[currentChapterIndex].name else currentChapter?.name ?: "",
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
                AppIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.previous_chapter_1))
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
                AppIcon(imageVector = Icons.Default.ArrowForward, contentDescription = localizeHelper.localize(Res.string.next_chapter_1))
            }
        }
    }
}
