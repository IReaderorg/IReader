package ir.kazemcodes.infinity.feature_sources.presentation.extension.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import ir.kazemcodes.infinity.feature_library.presentation.components.TabItem
import ir.kazemcodes.infinity.feature_library.presentation.components.Tabs
import ir.kazemcodes.infinity.feature_library.presentation.components.TabsContent
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionViewModel


@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainExtensionScreen(
    modifier: Modifier = Modifier,
    viewModel: ExtensionViewModel,
    pagerState: PagerState,
) {
    val tabs = listOf<TabItem>(TabItem.Sources(viewModel), TabItem.CommunitySources(viewModel))
    Column(modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
        Tabs(libraryTabs = tabs, pagerState = pagerState)
        TabsContent(libraryTabs = tabs, pagerState = pagerState)
    }
}

