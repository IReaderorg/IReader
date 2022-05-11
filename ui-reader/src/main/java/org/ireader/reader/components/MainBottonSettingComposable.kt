package org.ireader.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Chapter
import org.ireader.common_resources.UiText
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.ui_reader.R

@Composable
fun MainBottomSettingComposable(
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    scrollState: LazyListState,
    chapters: List<Chapter>,
    chapter: Chapter,
    currentChapterIndex: Int,
    onSetting: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onPlay: () -> Unit,
    onSliderFinished: () -> Unit,
    onSliderChange: (index: Float) -> Unit,
) {
    ChaptersSliderComposable(
        scrollState = scrollState,
        onNext = {
            onNext()
        },
        onPrev = {
            onPrev()
        },
        onSliderDragFinished = {
            onSliderFinished()
        },
        onSliderChange = {
            onSliderChange(it)
        },
        chapters = chapters,
        currentChapter = chapter,
        currentChapterIndex = currentChapterIndex
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconButton(
            imageVector = Icons.Default.Menu,
            text = UiText.StringResource(R.string.drawer),
            onClick = { scope.launch { scaffoldState.drawerState.open() } }
        )
        AppIconButton(
            imageVector = Icons.Default.Headphones,
            text = UiText.StringResource(R.string.play),
            onClick = { onPlay() }
        )
        AppIconButton(
            imageVector = Icons.Default.Settings,
            text = UiText.StringResource(R.string.settings),
            onClick = { onSetting() }
        )
    }
}
