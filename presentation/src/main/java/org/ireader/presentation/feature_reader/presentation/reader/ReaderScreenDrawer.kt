package org.ireader.presentation.feature_reader.presentation.reader

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.core_api.source.Source
import org.ireader.domain.models.entities.Chapter
import org.ireader.presentation.presentation.components.ChapterListItemComposable
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.ErrorTextWithEmojis

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReaderScreenDrawer(
    modifier: Modifier = Modifier,
    chapter: Chapter?,
    source: Source,
    onChapter: (chapter: Chapter) -> Unit,
    chapters: List<Chapter>,
    onReverseIcon: () -> Unit,
    drawerScrollState: LazyListState,
    onMap:(LazyListState) -> Unit
) {

    Column(modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
        Spacer(modifier = modifier.height(5.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            BigSizeTextComposable(text = "Content", modifier = Modifier.align(Alignment.Center))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = modifier.fillMaxWidth()) {
                Box {}
                AppIconButton(imageVector = Icons.Default.Sort,
                    title = "Reverse list icon",
                    onClick = {
                        onReverseIcon()
                    })
                AppIconButton(imageVector = Icons.Filled.Place, title = "", onClick = {
                    onMap(drawerScrollState)
                })

            }
        }

        Spacer(modifier = modifier.height(5.dp))
        Divider(modifier = modifier.fillMaxWidth(), thickness = 1.dp)
        LazyColumn(modifier = Modifier.fillMaxSize(),
            state = drawerScrollState) {
            items(items = chapters) { chapterItem ->
                    ChapterListItemComposable(modifier = modifier,
                        chapter = chapterItem,
                        onItemClick = { onChapter(chapterItem) },
                        isLastRead = chapter?.id == chapterItem.id)
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