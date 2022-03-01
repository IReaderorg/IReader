package org.ireader.presentation.feature_sources.presentation.extension


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.ireader.core.utils.Constants.DEFAULT_ELEVATION
import org.ireader.domain.models.entities.Catalog
import org.ireader.domain.models.entities.CatalogLocal
import org.ireader.presentation.feature_sources.presentation.extension.composables.RemoteSourcesScreen
import org.ireader.presentation.feature_sources.presentation.extension.composables.UserSourcesScreen
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle


@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ExtensionScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ExtensionViewModel,
    onRefreshCatalogs: () -> Unit,
    onClickCatalog: (Catalog) -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onClickTogglePinned: (CatalogLocal) -> Unit,
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    val pages = listOf<String>(
        "Sources",
        "Extensions"
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TopAppBarTitle(title = "Extensions")
                },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = DEFAULT_ELEVATION,
                actions = {

                }
            )
        },
    ) {
        // UserSourcesScreen(viewModel, navController)
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                    )
                },

                ) {
                // Add tabs for all of our pages
                pages.forEachIndexed { index, title ->
                    Tab(
                        text = { MidSizeTextComposable(text = title) },
                        selected = pagerState.currentPage == index,

                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    )
                }
            }

            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        UserSourcesScreen(
                            onClickCatalog = onClickCatalog,
                            onClickTogglePinned = onClickTogglePinned,
                            state = viewModel
                        )
                    }
                    else -> {
                        RemoteSourcesScreen(
                            viewModel = viewModel,
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
}


