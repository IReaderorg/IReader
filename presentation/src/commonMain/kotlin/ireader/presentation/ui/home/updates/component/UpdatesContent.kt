

package ireader.presentation.ui.home.updates.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    // Collect state reactively - this is the key fix!
    val screenState by state.state.collectAsState()
    val updates = screenState.updates
    val selection = screenState.selectedChapterIds
    val updateHistory = screenState.updateHistory
    
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                bottom = 16.dp,
                top = 8.dp
            )
        ) {
            // New Updates Section
            if (updates.isNotEmpty()) {
                item {
                    TextSection(text = localizeHelper.localize(Res.string.new_updates))
                }
            }
            
            updates.forEach { (date, updatesList) ->
                item(key = "date_${date.date}") {
                    TextSection(
                        text = date.date.asRelativeTimeString()
                    )
                }
                items(
                    count = updatesList.size,
                    key = { index -> "update_${updatesList[index].chapterId}" },
                    contentType = { "update_item" }
                ) { index ->
                    val update = updatesList[index]
                    UpdatesItem(
                        book = update,
                        isSelected = update.chapterId in selection,
                        onClickItem = onClickItem,
                        onLongClickItem = onLongClickItem,
                        onClickCover = onClickCover,
                        onClickDownload = onClickDownload,
                        isDownloadable = !update.downloaded
                    )
                }
            }
            
            // Update History Section
            if (updateHistory.isNotEmpty()) {
                item(key = "history_header") {
                    TextSection(text = localizeHelper.localize(Res.string.update_history))
                }
                items(
                    count = updateHistory.size,
                    key = { index -> "history_${updateHistory[index].id}" },
                    contentType = { "history_item" }
                ) { index ->
                    UpdateHistoryItem(
                        history = updateHistory[index]
                    )
                }
            }
        }
    }
}
