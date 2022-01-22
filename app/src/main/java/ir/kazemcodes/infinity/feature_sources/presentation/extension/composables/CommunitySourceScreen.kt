package ir.kazemcodes.infinity.feature_sources.presentation.extension.composables

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CommunitySourceScreen(viewModel: ExtensionViewModel) {
    val scrollState = rememberLazyListState()
    val sources = viewModel.state.value.communitySources
    val backstack = LocalBackstack.current
    SourceList(sources = sources,scrollState=scrollState)
}

