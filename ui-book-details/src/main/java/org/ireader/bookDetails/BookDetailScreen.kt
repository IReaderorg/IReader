package org.ireader.bookDetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.ireader.bookDetails.viewmodel.ChapterState
import org.ireader.bookDetails.viewmodel.DetailState
import org.ireader.common_models.entities.Book
import org.ireader.components.components.ShowLoading
import org.ireader.core_ui.ui_components.CardTile
import org.ireader.core_ui.ui_components.DotsFlashing

@OptIn(
    ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    detailState: DetailState,
    modalBottomSheetState: ModalBottomSheetState,
    chapterState: ChapterState,
    onSummaryExpand: () -> Unit,
    onSwipeRefresh: () -> Unit,
    onChapterContent: () -> Unit,
    book: Book?,
    onTitle: (String) -> Unit,
    snackBarHostState: SnackbarHostState,
) {

    val swipeRefreshState =
        rememberSwipeRefreshState(isRefreshing = detailState.detailIsLoading)

    if (detailState.detailIsLoading) {
        ShowLoading()
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            onSwipeRefresh()
        },
        indicatorPadding = PaddingValues(vertical = 40.dp),
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                scale = true,
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primaryContainer,
                elevation = 8.dp,
            )
        }
    ) {
        Box(modifier = modifier) {

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                BookDetailScreenLoadedComposable(
                    onTitle = onTitle,
                    onSummaryExpand = onSummaryExpand,
                    isSummaryExpanded = detailState.expandedSummary,
                    book = book?:Book(key = "", sourceId = 0, title = ""),
                    source = detailState.source,
                )

                CardTile(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    onClick = onChapterContent,
                    title = "Contents",
                    subtitle = "${chapterState.chapters.size} Chapters",
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            DotsFlashing(chapterState.chapterIsLoading)

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Contents Detail",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
