package org.ireader.presentation.feature_reader.presentation.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.source.Source
import org.ireader.domain.view_models.reader.ReaderScreenViewModel
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import org.ireader.presentation.utils.scroll.CarouselScrollState

@Composable
fun MainBottomSettingComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    scrollState: CarouselScrollState,
    chapter: Chapter,
    source: Source,
) {
    val coroutineScope = rememberCoroutineScope()
    val currentIndex = viewModel.state.currentChapterIndex
    //val currentChapter = viewModel.getCurrentChapterByIndex()
    val chapters = viewModel.state.chapters
    ChaptersSliderComposable(
        scrollState = scrollState,
        onNext = {
            if (currentIndex < chapters.lastIndex) {
                viewModel.updateChapterSliderIndex(currentIndex + 1)
                viewModel.getChapter(viewModel.getCurrentChapterByIndex().id, source = source)
                coroutineScope.launch {
                    scrollState.scrollTo(0)
                }
            } else {
                coroutineScope.launch {
                    viewModel.showSnackBar(UiText.StringResource(R.string.this_is_last_chapter))

                }
            }
        },
        onPrev = {
            if (currentIndex > 0) {
                viewModel.updateChapterSliderIndex(currentIndex - 1)
                viewModel.getChapter(viewModel.getCurrentChapterByIndex().id, source = source)
                coroutineScope.launch {
                    scrollState.scrollTo(0)
                }
            } else {
                coroutineScope.launch {
                    viewModel.showSnackBar(UiText.StringResource(org.ireader.core.R.string.this_is_first_chapter))
                }
            }
        },
        onSliderDragFinished = {
            coroutineScope.launch {
                viewModel.showSnackBar(UiText.DynamicString(chapters[viewModel.state.currentChapterIndex].title))
            }
            viewModel.updateChapterSliderIndex(currentIndex)
            viewModel.getChapter(chapters[viewModel.state.currentChapterIndex].id, source = source)
            coroutineScope.launch {
                scrollState.scrollTo(0)
            }
        },
        onSliderChange = {
            viewModel.updateChapterSliderIndex(it.toInt())
        },
        chapters = viewModel.state.chapters,
        currentChapter = chapter,
        currentChapterIndex = viewModel.state.currentChapterIndex
    )
    Row(modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically) {
        TopAppBarActionButton(imageVector = Icons.Default.Menu,
            title = "Chapter List Drawer",
            onClick = { scope.launch { scaffoldState.drawerState.open() } })
        TopAppBarActionButton(imageVector = Icons.Default.Settings,
            title = "Setting Drawer",
            onClick = { viewModel.toggleSettingMode(true) })
    }
}