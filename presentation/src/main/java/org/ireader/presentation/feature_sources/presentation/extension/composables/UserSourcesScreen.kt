package org.ireader.presentation.feature_sources.presentation.extension.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.ireader.presentation.feature_sources.presentation.extension.ExtensionViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserSourcesScreen(viewModel: ExtensionViewModel, navController: NavController) {
    val scrollState = rememberLazyListState()
    val sources = viewModel.state.value.sources

    SourceList(
        modifier = Modifier.fillMaxSize(),
        sources = sources,
        scrollState = scrollState,
        navController = navController
    )
}