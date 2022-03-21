package org.ireader.presentation.feature_history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import org.ireader.core.utils.Constants
import org.ireader.core_ui.theme.AppColors
import org.ireader.domain.view_models.history.HistoryViewModel
import org.ireader.presentation.presentation.ToolBar
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.AppTextField
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle

@Composable
fun HistoryTopAppBar(
    navController: NavController,
    vm: HistoryViewModel,
) {
    val focusManager = LocalFocusManager.current

    ToolBar(
        title = {
            if (!vm.searchMode) {
                TopAppBarTitle(title = "History")
            } else {
                AppTextField(
                    query = vm.searchQuery,
                    onValueChange = {
                        vm.searchQuery = it
                    },
                    onConfirm = {
                        focusManager.clearFocus()
                    },
                )
            }
        },
        backgroundColor = AppColors.current.bars,
        contentColor = AppColors.current.onBars,
        elevation = Constants.DEFAULT_ELEVATION,
        actions = {
            if (vm.searchMode) {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    title = "Close",
                    onClick = {
                        vm.searchMode = false
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