

package ireader.presentation.ui.home.updates.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.core.util.asRelativeTimeString
import ireader.presentation.ui.component.text_related.TextSection
import ireader.presentation.ui.home.updates.viewmodel.UpdatesViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdatesContent(
        state: UpdatesViewModel,
        onClickItem: (UpdatesWithRelations) -> Unit,
        onLongClickItem: (UpdatesWithRelations) -> Unit,
        onClickCover: (UpdatesWithRelations) -> Unit,
        onClickDownload: (UpdatesWithRelations) -> Unit,
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

                        text = date.date.asRelativeTimeString()
                    )
                }
                items(
                    count = updates.size,
                ) { index ->
                    UpdatesItem(
                        book = updates[index],
                        isSelected = updates[index].chapterId in state.selection,
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
