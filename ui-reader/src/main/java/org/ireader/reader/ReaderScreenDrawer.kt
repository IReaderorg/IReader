package org.ireader.reader

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ireader.common_models.entities.Chapter
import org.ireader.components.components.ChapterRow
import org.ireader.components.list.scrollbars.LazyColumnScrollbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.text_related.ErrorTextWithEmojis
import org.ireader.ui_reader.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReaderScreenDrawer(
    modifier: Modifier = Modifier,
    relativeTime:Int = 0,
    dateFormat: String = "",
    chapter: Chapter?,
    onChapter: (chapter: Chapter) -> Unit,
    chapters: List<Chapter>,
    onReverseIcon: () -> Unit,
    drawerScrollState: LazyListState,
    onMap: (LazyListState) -> Unit,
) {

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = modifier.height(5.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            BigSizeTextComposable(text =stringResource(R.string.content), modifier = Modifier.align(Alignment.Center))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = modifier.fillMaxWidth()
            ) {
                Box {}

                AppIconButton(imageVector = Icons.Filled.Place,contentDescription = stringResource(R.string.find_current_chapter), onClick = {
                    onMap(drawerScrollState)
                })
                AppIconButton(
                    imageVector = Icons.Default.Sort,
                    contentDescription = stringResource(R.string.reverse),
                    onClick = {
                        onReverseIcon()
                    }
                )
            }
        }

        Spacer(modifier = modifier.height(5.dp))
        Divider(modifier = modifier.fillMaxWidth(), thickness = 1.dp)
        LazyColumnScrollbar(listState = drawerScrollState) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = drawerScrollState
            ) {
                items(count = chapters.size) { index ->
                    ChapterRow(
                        modifier = modifier,
                        chapter = chapters[index],
                        onItemClick = { onChapter(chapters[index]) },
                        isLastRead = chapter?.id ==chapters[index].id,
                    )
                }
            }
        }
        if (chapters.isEmpty()) {
            ErrorTextWithEmojis(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.CenterHorizontally),
                error = "There is no book is Library, you can add books in the Explore screen"
            )
        }
    }
}
