package ir.kazemcodes.infinity.feature_sources.presentation.extension

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.core.utils.Constants.DEFAULT_ELEVATION
import ir.kazemcodes.infinity.feature_activity.presentation.Screen
import ir.kazemcodes.infinity.feature_library.presentation.components.TabItem
import ir.kazemcodes.infinity.feature_library.presentation.components.Tabs
import ir.kazemcodes.infinity.feature_library.presentation.components.TabsContent


@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ExtensionScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: ExtensionViewModel = hiltViewModel(),
) {

    val pageState = rememberPagerState()

    val sources = viewModel.state.value.sources
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
                actions = {
                    TopAppBarActionButton(
                        imageVector = Icons.Default.Add,
                        title = "Adding Sources Button",
                        onClick = { navController.navigate(Screen.ExtensionCreator.route) }
                    )
                }
            )
        }
    ) {
        val tabs = listOf<TabItem>(TabItem.Sources(viewModel,navController), TabItem.CommunitySources(viewModel,navController))
        Column(modifier = modifier
            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
            Tabs(libraryTabs = tabs, pagerState = pageState)
            TabsContent(libraryTabs = tabs, pagerState = pageState)
        }
    }
}



