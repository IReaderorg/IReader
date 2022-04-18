package org.ireader.presentation.feature_history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import org.ireader.presentation.feature_history.viewmodel.HistoryViewModel
import org.ireader.presentation.presentation.Toolbar
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.AppTextField
import org.ireader.presentation.presentation.reusable_composable.BigSizeTextComposable

@Composable
fun HistoryTopAppBar(
    navController: NavController,
    vm: HistoryViewModel,
) {
    val focusManager = LocalFocusManager.current

    Toolbar(
        title = {
            if (!vm.searchMode) {
                BigSizeTextComposable(text = "History")
            } else {
                AppTextField(
                    query = vm.searchQuery,
                    onValueChange = {
                        vm.searchQuery = it
                        vm.getHistoryBooks()
                    },
                    onConfirm = {
                        focusManager.clearFocus()
                    },
                )
            }
        },
        actions = {
            if (vm.searchMode) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    title = "Close",
                    onClick = {
                        vm.searchMode = false
                        vm.searchQuery = ""
                        vm.getHistoryBooks()
                    },
                )
            }
            AppIconButton(
                imageVector = Icons.Default.Search,
                title = "Search",
                onClick = {
                    vm.searchMode = true
                },
            )
            AppIconButton(
                imageVector = Icons.Default.Delete,
                title = "Delete All Histories",
                onClick = {
                    vm.deleteAllHistories()
                },
            )


        },
        navigationIcon = if (vm.searchMode) {
            {
                AppIconButton(imageVector = Icons.Default.ArrowBack,
                    title = "Toggle search mode off",
                    onClick = { vm.searchMode = false })

            }
        } else null

    )
}