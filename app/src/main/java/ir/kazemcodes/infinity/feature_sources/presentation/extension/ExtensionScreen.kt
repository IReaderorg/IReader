package ir.kazemcodes.infinity.feature_sources.presentation.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import com.zhuinden.simplestackextensions.servicesktx.lookup
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.feature_activity.presentation.BrowserScreenKey
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants.DEFAULT_ELEVATION
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreType
import ir.kazemcodes.infinity.feature_library.presentation.components.TabItem
import ir.kazemcodes.infinity.feature_library.presentation.components.Tabs
import ir.kazemcodes.infinity.feature_library.presentation.components.TabsContent
import ir.kazemcodes.infinity.feature_sources.sources.Extensions


@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ExtensionScreen(modifier: Modifier = Modifier) {
    val backstack = LocalBackstack.current
    val extensions: Extensions = remember {
        backstack.lookup<Extensions>()
    }
    val viewModel = rememberService<ExtensionViewModel>()
    val pageState = rememberPagerState()

    val sources = extensions.getSources()
    viewModel.updateSource(sources)

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
            )
        }
    ) {
        MainExtensionScreen(viewModel = viewModel, pagerState = pageState)

//        LazyColumn {
//            items(sources.size) { index ->
//                Row(modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//                    .height(30.dp)
//                    .clickable {
//                        backstack.goTo(BrowserScreenKey(sources[index].name,
//                            exploreType = ExploreType.Latest.mode))
//                    },
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically) {
//                    Text(sources[index].name)
//                    if (sources[index].supportsMostPopular) {
//                        Text(stringResource(R.string.popular_book),
//                            color = MaterialTheme.colors.primary,
//                            style = MaterialTheme.typography.subtitle2,
//                            modifier = Modifier.clickable {
//                                backstack.goTo(BrowserScreenKey(sourceName = sources[index].name,
//                                    exploreType = ExploreType.Popular.mode))
//                            })
//                    }
//                }
//
//            }
//        }


    }
}

@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainExtensionScreen(modifier: Modifier = Modifier,viewModel: ExtensionViewModel,pagerState: PagerState) {
    val tabs = listOf<TabItem>(TabItem.Sources(viewModel),TabItem.CommunitySources(viewModel))
    Column(modifier = modifier.fillMaxSize()) {
        Tabs(libraryTabs = tabs, pagerState = pagerState)
        TabsContent(libraryTabs = tabs, pagerState = pagerState)
    }
}

@Composable
fun UserSourcesScreen(viewModel: ExtensionViewModel) {
    val sources = viewModel.state.value.sources
    val backstack = LocalBackstack.current
            LazyColumn {
            items(sources.size) { index ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(30.dp)
                    .clickable {
                        backstack.goTo(BrowserScreenKey(sources[index].name,
                            exploreType = ExploreType.Latest.mode))
                    },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(sources[index].name)
                    if (sources[index].supportsMostPopular) {
                        Text(stringResource(R.string.popular_book),
                            color = MaterialTheme.colors.primary,
                            style = MaterialTheme.typography.subtitle2,
                            modifier = Modifier.clickable {
                                backstack.goTo(BrowserScreenKey(sourceName = sources[index].name,
                                    exploreType = ExploreType.Popular.mode))
                            })
                    }
                }

            }
        }
}

