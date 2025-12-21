package ireader.presentation.ui.reader

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.ChapterRow
import ireader.presentation.ui.component.list.scrollbars.IVerticalFastScroller
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.text_related.ErrorTextWithEmojis

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReaderScreenDrawer(
        modifier: Modifier = Modifier,
        relativeTime: Int = 0,
        dateFormat: String = "",
        chapter: Chapter?,
        onChapter: (chapter: Chapter) -> Unit,
        chapters: List<Chapter>,
        onReverseIcon: () -> Unit,
        drawerScrollState: LazyListState,
        onMap: (LazyListState) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(.9f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = modifier.height(5.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            BigSizeTextComposable(text = localize(Res.string.content), modifier = Modifier.align(Alignment.Center))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = modifier.fillMaxWidth()
            ) {
                Box {}

                AppIconButton(imageVector = Icons.Filled.Place, contentDescription = localize(Res.string.find_current_chapter), onClick = {
                    onMap(drawerScrollState)
                })
                AppIconButton(
                    imageVector = Icons.Default.Sort,
                    contentDescription = localize(Res.string.reverse),
                    onClick = {
                        onReverseIcon()
                    }
                )
            }
        }

        Spacer(modifier = modifier.height(5.dp))
        Divider(modifier = modifier.fillMaxWidth(), thickness = 1.dp)
        IVerticalFastScroller(listState = drawerScrollState) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = drawerScrollState
            ) {
                items(
                    count = chapters.size,
                    key = { index -> chapters[index].id }
                ) { index ->
                    ChapterRow(
                        modifier = modifier,
                        chapter = chapters[index],
                        onItemClick = { onChapter(chapters[index]) },
                        isLastRead = chapter?.id == chapters[index].id,
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
