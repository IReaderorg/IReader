package org.ireader.presentation.feature_sources.presentation.extension.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.ireader.presentation.feature_sources.presentation.extension.ExtensionViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CommunitySourceScreen(viewModel: ExtensionViewModel, navController: NavController) {
    val scrollState = rememberLazyListState()
    val sources = viewModel.state.value.communitySources

    SourceList(modifier = Modifier.padding(bottom = 50.dp),
        sources = sources,
        scrollState = scrollState,
        navController = navController)
}

