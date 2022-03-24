package org.ireader.presentation.feature_library.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.CoroutineScope
import org.ireader.presentation.feature_library.presentation.viewmodel.LibraryViewModel

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun BottomTabComposable(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel,
    pagerState: PagerState,
    scope: CoroutineScope,
    navController: NavController,
) {
    val tabs = listOf(TabItem.Filter(viewModel = viewModel),
        TabItem.Sort(viewModel, navController),
        TabItem.Display(viewModel = viewModel))

    /** There is Some issue here were sheet content is not need , not sure why**/
    Column(modifier = modifier.fillMaxSize()) {
        Tabs(libraryTabs = tabs, pagerState = pagerState)
        TabsContent(libraryTabs = tabs, pagerState = pagerState, viewModel)

    }


}



