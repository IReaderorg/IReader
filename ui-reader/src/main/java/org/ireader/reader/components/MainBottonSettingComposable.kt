package org.ireader.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Chapter
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.ui_reader.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomSettingComposable(
    modifier: Modifier = Modifier,
    scope: CoroutineScope,
    drawerState: DrawerState,
    chapters: List<Chapter>,
    chapter: Chapter?,
    currentChapterIndex: Int,
    onSetting: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onPlay: () -> Unit,
    onSliderFinished: () -> Unit,
    onSliderChange: (index: Float) -> Unit,
) {
    ChaptersSliderComposable(
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
           contentDescription = stringResource(R.string.drawer),
            onClick = { scope.launch { drawerState.open() } }
        )
        AppIconButton(
            imageVector = Icons.Default.Headphones,
           contentDescription = stringResource(R.string.play),
            onClick = { onPlay() }
        )
        AppIconButton(
            imageVector = Icons.Default.Settings,
           contentDescription = stringResource(R.string.settings),
            onClick = { onSetting() }
        )
    }
}
