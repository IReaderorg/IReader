package org.ireader.presentation.feature_updates

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.ireader.core.utils.convertLongToTime
import org.ireader.domain.models.entities.Update
import org.ireader.domain.utils.toast
import org.ireader.presentation.feature_sources.presentation.extension.composables.TextSection
import org.ireader.presentation.feature_updates.component.UpdatesItem
import org.ireader.presentation.feature_updates.component.UpdatesToolbar
import org.ireader.presentation.feature_updates.viewmodel.UpdatesViewModel
import org.ireader.presentation.ui.BookDetailScreenSpec

@Composable
fun UpdateScreen(
    navController: NavController,
    vm: UpdatesViewModel = hiltViewModel(),
) {
    val updates = vm.updates
    val context = LocalContext.current
    val dateFormat = "yyyy/MM/dd"
    val times =
        updates.map { convertLongToTime(it.date, dateFormat) }.distinct()
    val updatesItems: LazyListScope.(Update) -> Unit = { update ->
        item {
            UpdatesItem(
                book = update,
                onClickItem = {
                    if (vm.hasSelection) {
                        vm.addUpdate(it)
                    } else {
                        navController.navigate(
                            BookDetailScreenSpec.buildRoute(
                                update.sourceId,
                                update.bookId
                            )
                        )
                    }
                },
                isSelected = update.id in vm.selection,
                onClickCover = {
                    navController.navigate(BookDetailScreenSpec.buildRoute(
                        update.sourceId,
                        update.bookId
                    ))
                },
                onClickDownload = {
                    //TODO implement this later
                    context.toast("Not Implemented!")
                },
                onLongClickItem = {
                    vm.addUpdate(it)
                },
            )
        }

    }
    Scaffold(
        topBar = {
            UpdatesToolbar(
                state = vm,
                onClickCancelSelection = {
                    vm.selection.clear()
                },
                onClickSelectAll = {
                    val ids = (vm.selection + vm.updates.map { it.id }).distinct()
                    vm.selection.clear()
                    vm.selection.addAll(ids)
                },
                onClickFlipSelection = {
                    val ids = (vm.updates.map { update -> update.id }
                        .filterNot { it in vm.selection }).distinct()
                    vm.selection.clear()
                    vm.selection.addAll(ids)
                },
                onClickRefresh = {
                    //TODO implement this later
                    context.toast("Not Implemented!")
                },
            )
        }
    ) {
        LazyColumn {

            for (time in times) {
                item {
                    TextSection(
                        text = time,
                    )
                }
                for (update in updates.filter {
                    convertLongToTime(it.date,
                        format = dateFormat) == time
                }) {
                    updatesItems(update)
                }
            }


        }
    }
}