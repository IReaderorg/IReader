package org.ireader.sources.extension

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Catalog
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText
import org.ireader.components.components.component.pagerTabIndicatorOffset
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.utils.horizontalPadding
import org.ireader.sources.extension.composables.RemoteSourcesScreen
import org.ireader.sources.extension.composables.UserSourcesScreen
import org.ireader.ui_sources.R

@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExtensionScreen(
    modifier: Modifier = Modifier,
    viewModel: ExtensionViewModel,
    onRefreshCatalogs: () -> Unit,
    onClickCatalog: (Catalog) -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    snackBarHostState: androidx.compose.material3.SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
                else -> {}
            }
        }
    }
    LaunchedEffect(key1 = true) {
        viewModel.clearExploreMode()
    }
    val pages = remember {
        listOf<UiText>(
            UiText.StringResource(R.string.sources),
            UiText.StringResource(R.string.extensions),
        )
    }
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = AppColors.current.bars,
            contentColor = AppColors.current.onBars,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        ) {
            pages.forEachIndexed { index, title ->
                Tab(
                    text = {
                        MidSizeTextComposable(text = title, color = Color.Unspecified)
                    },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onBackground,
                )
            }
        }

        HorizontalPager(
            count = pages.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            viewModel.currentPagerPage = page
            when (page) {
                0 -> {
                    UserSourcesScreen(
                        onClickCatalog = onClickCatalog,
                        onClickTogglePinned = onClickTogglePinned,
                        state = viewModel,
                    )
                }
                1 -> {
                    RemoteSourcesScreen(
                        state = viewModel,
                        onRefreshCatalogs = onRefreshCatalogs,
                        onClickInstall = onClickInstall,
                        onClickUninstall = onClickUninstall,
                    )
                }
            }
        }
    }
}

@Composable
fun SourceHeader(
    modifier: Modifier = Modifier,
    language: String,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = LocaleHelper.getSourceDisplayName(language, context),
            modifier = modifier
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}