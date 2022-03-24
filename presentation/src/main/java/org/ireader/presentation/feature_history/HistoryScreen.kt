package org.ireader.presentation.feature_history

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.ireader.core.utils.convertLongToTime
import org.ireader.domain.feature_services.io.HistoryWithRelations
import org.ireader.domain.models.entities.History
import org.ireader.presentation.feature_history.viewmodel.HistoryViewModel
import org.ireader.presentation.feature_sources.presentation.extension.composables.TextSection
import org.ireader.presentation.ui.BookDetailScreenSpec
import org.ireader.presentation.ui.ReaderScreenSpec

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    vm: HistoryViewModel = hiltViewModel(),
) {
    val histories = vm.history
    val dateFormat = "yyyy/MM/dd"
    val times =
        histories.map { convertLongToTime(it.readAt, dateFormat) }.distinct()
    val historyItem: LazyListScope.(HistoryWithRelations) -> Unit = { history ->
        item {
            HistoryItem(history = history,
                onClickItem = {
                    navController.navigate(
                        BookDetailScreenSpec.buildRoute(
                            history.sourceId,
                            history.bookId
                        )
                    )
                },
                onClickDelete = {
                    vm.deleteHistory(History(
                        history.bookId,
                        history.chapterId,
                        history.readAt))
                },
                onClickPlay = {
                    navController.navigate(
                        ReaderScreenSpec.buildRoute(
                            history.bookId,
                            history.sourceId,
                            history.chapterId
                        )
                    )
                })
        }

    }
    Scaffold(
        topBar = { HistoryTopAppBar(navController = navController, vm = vm) }
    ) {
        LazyColumn {

            for (time in times) {
                item {
                    TextSection(
                        text = time,
                    )
                }
                for (history in histories.filter {
                    convertLongToTime(it.readAt,
                        format = dateFormat) == time
                }) {
                    historyItem(history)
                }
            }


        }
    }
}