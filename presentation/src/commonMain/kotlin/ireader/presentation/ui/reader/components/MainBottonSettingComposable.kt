package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.Chapter
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


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
        onAutoScrollToggle: (() -> Unit)? = null,
        onReviews: (() -> Unit)? = null,
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
            contentDescription = localize(MR.strings.drawer),
            onClick = { scope.launch { drawerState.open() } }
        )

        // TODO: implement  TTS functionality in desktop

        AppIconButton(
            imageVector = Icons.Default.Headphones,
            contentDescription = localize(MR.strings.play),
            onClick = { onPlay() }
        )
        
        // Chapter Reviews button
        if (onReviews != null) {
            AppIconButton(
                imageVector = androidx.compose.material.icons.Icons.Default.RateReview,
                contentDescription = "Chapter Reviews",
                onClick = { onReviews() }
            )
        }
        
        // Auto-scroll toggle button
        if (onAutoScrollToggle != null) {
            AppIconButton(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Auto Scroll",
                onClick = { onAutoScrollToggle() }
            )
        }

        AppIconButton(
            imageVector = Icons.Default.Settings,
            contentDescription = localize(MR.strings.settings),
            onClick = { onSetting() }
        )
    }
}
