package org.ireader.presentation.feature_sources.presentation.extension


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import org.ireader.core.utils.Constants.DEFAULT_ELEVATION
import org.ireader.presentation.feature_library.presentation.components.TabItem
import org.ireader.presentation.feature_sources.presentation.extension.composables.UserSourcesScreen
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle


@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ExtensionScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: ExtensionViewModel = hiltViewModel(),
) {

    val pageState = rememberPagerState()

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
        UserSourcesScreen(viewModel, navController)
        val tabs = listOf<TabItem>(TabItem.Sources(viewModel, navController),
            TabItem.CommunitySources(viewModel, navController))
//        Column(modifier = modifier
//            .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Top) {
//            Tabs(libraryTabs = tabs, pagerState = pageState)
//            TabsContent(libraryTabs = tabs, pagerState = pageState)
//        }
    }
}


