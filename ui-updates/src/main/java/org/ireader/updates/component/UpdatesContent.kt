

package org.ireader.updates.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.common_models.entities.UpdateWithInfo
import org.ireader.common_resources.UiText
import org.ireader.components.text_related.TextSection
import org.ireader.updates.viewmodel.UpdateState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdatesContent(
    state: UpdateState,
    onClickItem: (UpdateWithInfo) -> Unit,
    onLongClickItem: (UpdateWithInfo) -> Unit,
    onClickCover: (UpdateWithInfo) -> Unit,
    onClickDownload: (UpdateWithInfo) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                bottom = 16.dp,
                top = 8.dp
            )
        ) {
            state.updates.forEach { (date, updates) ->
                item {
                    TextSection(
                        text = UiText.DynamicString(date)
                    )
                }
                items(
                    count = updates.size,
                ) { index ->
                    UpdatesItem(
                        book = updates[index],
                        isSelected = updates[index].id in state.selection,
                        onClickItem = onClickItem,
                        onLongClickItem = onLongClickItem,
                        onClickCover = onClickCover,
                        onClickDownload = onClickDownload,
                        isDownloadable = !updates[index].downloaded
                    )
                }
            }
        }
    }
}
