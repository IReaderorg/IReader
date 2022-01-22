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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import com.zhuinden.simplestackextensions.servicesktx.lookup
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarTitle
import ir.kazemcodes.infinity.feature_activity.presentation.ExtensionCreatorScreenKey
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants.DEFAULT_ELEVATION
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
                actions = {
                    TopAppBarActionButton(imageVector = Icons.Default.Add, title = "Adding Sources Button", onClick = { backstack.goTo(ExtensionCreatorScreenKey()) })
                }
            )
        }
    ) {
        val tabs = listOf<TabItem>(TabItem.Sources(viewModel), TabItem.CommunitySources(viewModel))
        Column(modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top) {
            Tabs(libraryTabs = tabs, pagerState = pageState)
            TabsContent(libraryTabs = tabs, pagerState = pageState)
        }
    }
}



