package ir.kazemcodes.infinity.feature_sources.presentation.extension

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import ir.kazemcodes.infinity.feature_sources.presentation.extension.composables.MainExtensionScreen
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
        MainExtensionScreen(viewModel = viewModel, pagerState = pageState)
    }
}



