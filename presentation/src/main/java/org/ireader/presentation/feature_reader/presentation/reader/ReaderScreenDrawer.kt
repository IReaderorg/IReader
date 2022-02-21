package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import org.ireader.domain.models.entities.Chapter
import org.ireader.presentation.presentation.components.ChapterListItemComposable
import org.ireader.presentation.presentation.components.handlePagingChapterResult
import org.ireader.presentation.presentation.reusable_composable.ErrorTextWithEmojis
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle
import org.ireader.source.core.Source

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReaderScreenDrawer(
    modifier: Modifier = Modifier,
    chapter: Chapter,
    source: Source,
    onChapter: (chapter: Chapter) -> Unit,
    chapters: LazyPagingItems<Chapter>,
    onReverseIcon: () -> Unit,
) {
    val drawerScrollState = rememberLazyListState()

    Column(modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
        Spacer(modifier = modifier.height(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = modifier.fillMaxWidth()) {
            TopAppBarTitle(title = "Content", modifier = modifier.padding(start = 8.dp))
            Row {
                TopAppBarActionButton(imageVector = Icons.Default.Sort,
                    title = "Reverse list icon",
                    onClick = {
                        onReverseIcon()
                    })
            }
        }

        Spacer(modifier = modifier.height(5.dp))
        Divider(modifier = modifier.fillMaxWidth(), thickness = 1.dp)

        val result = handlePagingChapterResult(books = chapters, onEmptyResult = {
            Box(modifier = modifier.fillMaxSize()) {
                ErrorTextWithEmojis(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .align(Alignment.Center),
                    error = "There is no book is Library, you can add books in the Explore screen"
                )
            }

        })
        if (result) {
            AnimatedContent(chapters.loadState.refresh is LoadState.NotLoading) {
                LazyColumn(modifier = Modifier.fillMaxSize(),
                    state = drawerScrollState) {
                    items(items = chapters) { chapter ->
                        if (chapter != null && source != null) {
                            ChapterListItemComposable(modifier = modifier,
                                chapter = chapter, goTo = { onChapter(chapter) })
                        }
                    }
                }
            }
        }


    }
}