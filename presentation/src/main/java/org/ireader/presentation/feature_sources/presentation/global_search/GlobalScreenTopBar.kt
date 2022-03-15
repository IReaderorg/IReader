package org.ireader.presentation.feature_sources.presentation.global_search

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalFocusManager
import org.ireader.core.utils.Constants
import org.ireader.core_ui.theme.AppColors
import org.ireader.presentation.presentation.ToolBar
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.AppTextField


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun GlobalScreenTopBar(
    onSearch: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onPop: () -> Unit,
    query: String,
) {

    val focusManager = LocalFocusManager.current

    var searchMode by remember {
        mutableStateOf(false)
    }


    ToolBar(
        title = {
            AppTextField(
                query = query,
                onValueChange = onValueChange,
                onConfirm = {
                    onSearch(query)
                    focusManager.clearFocus()
                },
            )
        },
        backgroundColor = AppColors.current.bars,
        contentColor = AppColors.current.onBars,
        elevation = Constants.DEFAULT_ELEVATION,
        actions = {
            AppIconButton(
                imageVector = Icons.Default.Search,
                title = "Search",
                onClick = {
                    searchMode = true
                },
            )
        },
        navigationIcon = {
            AppIconButton(imageVector = Icons.Default.ArrowBack,
                title = "Toggle search mode off",
                onClick = {
                    onPop()
                })
        }

    )
}