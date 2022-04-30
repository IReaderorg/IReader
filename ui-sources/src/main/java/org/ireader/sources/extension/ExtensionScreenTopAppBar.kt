package org.ireader.sources.extension

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.runtime.Composable
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.components.reusable_composable.BigSizeTextComposable

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ExtensionScreenTopAppBar(
    searchMode:Boolean,
    query:String,
    onValueChange:(query:String) -> Unit,
    onConfirm:() -> Unit,
    pagerState:PagerState,
    onClose:() -> Unit,
    onSearchDisable:() -> Unit,
    onSearchEnable:() -> Unit,
    onRefresh:() -> Unit,
    onSearchNavigate: () -> Unit
) {
    Toolbar(
        title = {
            if (!searchMode) {
                BigSizeTextComposable(text = "Extensions")
            } else {
                AppTextField(
                    query = query,
                    onValueChange =onValueChange,
                    onConfirm = onConfirm,
                )
            }
        },
        actions = {
            if (pagerState.currentPage == 1) {
                if (searchMode) {
                    AppIconButton(
                        imageVector = Icons.Default.Close,
                        title = "Close",
                        onClick = onClose,
                    )
                } else {
                    AppIconButton(
                        imageVector = Icons.Default.Search,
                        title = "Search",
                        onClick = onSearchEnable,
                    )
                }
                AppIconButton(
                    imageVector = Icons.Default.Refresh,
                    title = "Refresh",
                    onClick = onRefresh,
                )
            } else {
                if (searchMode) {
                    AppIconButton(
                        imageVector = Icons.Default.Close,
                        title = "Close",
                        onClick = onSearchDisable,
                    )
                } else {
                    AppIconButton(
                        imageVector = Icons.Default.Search,
                        title = "Search",
                        onClick = onSearchEnable,
                    )
                    AppIconButton(
                        imageVector = Icons.Default.TravelExplore,
                        title = "Search",
                        onClick = onSearchNavigate,
                    )
                }
            }
        },
        navigationIcon = if (searchMode) {
            {
                AppIconButton(
                    imageVector = Icons.Default.ArrowBack,
                    title = "Disable Search",
                    onClick = onSearchDisable
                )
            }
        } else null
    )
}