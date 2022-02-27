package org.ireader.presentation.feature_sources.presentation.extension.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.ireader.domain.models.entities.CatalogLocal
import org.ireader.presentation.feature_sources.presentation.extension.ExtensionViewModel
import timber.log.Timber


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserSourcesScreen(viewModel: ExtensionViewModel, navController: NavController) {
    val allSource = mutableListOf<CatalogLocal>()
    val scrollState = rememberLazyListState()
    viewModel.UiState.catalogLocal.forEach {
        allSource.add(it)
    }
    allSource.forEach {
        Timber.e(it.toString())
    }

    SourceListComposable(
        modifier = Modifier.fillMaxSize(),
        sources = allSource.map { it.source },
        scrollState = scrollState,
        navController = navController
    )
}