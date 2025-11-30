

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
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UpdatesContent(
        state: UpdatesViewModel,
        onClickItem: (UpdatesWithRelations) -> Unit,
        onLongClickItem: (UpdatesWithRelations) -> Unit,
        onClickCover: (UpdatesWithRelations) -> Unit,
        onClickDownload: (UpdatesWithRelations) -> Unit,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                bottom = 16.dp,
                top = 8.dp
            )
        ) {
            // New Updates Section
            if (state.updates.isNotEmpty()) {
                item {
                    TextSection(text = localizeHelper.localize(Res.string.new_updates))
                }
            }
            
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
            
            // Update History Section
            if (state.updateHistory.isNotEmpty()) {
                item {
                    TextSection(text = localizeHelper.localize(Res.string.update_history))
                }
                items(
                    count = state.updateHistory.size
                ) { index ->
                    UpdateHistoryItem(
                        history = state.updateHistory[index]
                    )
                }
            }
        }
    }
}
