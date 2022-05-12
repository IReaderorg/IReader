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
import org.ireader.common_resources.UiText
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.AppTextField
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.ui_sources.R

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
                BigSizeTextComposable(text = UiText.StringResource(R.string.extensions))
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
                        text = UiText.StringResource(R.string.close),
                        onClick = onClose,
                    )
                } else {
                    AppIconButton(
                        imageVector = Icons.Default.Search,
                        text = UiText.StringResource(R.string.search),
                        onClick = onSearchEnable,
                    )
                }
                AppIconButton(
                    imageVector = Icons.Default.Refresh,
                    text = UiText.StringResource(R.string.refresh),
                    onClick = onRefresh,
                )
            } else {
                if (searchMode) {
                    AppIconButton(
                        imageVector = Icons.Default.Close,
                        text = UiText.StringResource(R.string.close),
                        onClick = onSearchDisable,
                    )
                } else {
                    AppIconButton(
                        imageVector = Icons.Default.Search,
                        text = UiText.StringResource(R.string.search),
                        onClick = onSearchEnable,
                    )
                    AppIconButton(
                        imageVector = Icons.Default.TravelExplore,
                        text = UiText.StringResource(R.string.search),
                        onClick = onSearchNavigate,
                    )
                }
            }
        },
        navigationIcon = {
            if (searchMode) {

                    AppIconButton(
                        imageVector = Icons.Default.ArrowBack,
                        text = UiText.StringResource(R.string.toggle_search_mode_off),
                        onClick = onSearchDisable
                    )
            } else null
        }
    )
}