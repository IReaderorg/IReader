package org.ireader.sources.extension.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.ireader.common_models.entities.Catalog
import org.ireader.common_models.entities.CatalogInstalled
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.common_models.entities.key
import org.ireader.sources.extension.CatalogItem
import org.ireader.sources.extension.ExtensionViewModel
import org.ireader.sources.extension.SourceHeader

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteSourcesScreen(
    modifier: Modifier = Modifier,
    state: ExtensionViewModel,
    onRefreshCatalogs: () -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
) {
    val scrollState = rememberLazyListState()
    val swipeState = rememberSwipeRefreshState(isRefreshing = state.isRefreshing)

    SwipeRefresh(
        state = swipeState,
        onRefresh = { onRefreshCatalogs() },
        indicator = { _, trigger ->
            SwipeRefreshIndicator(
                state = swipeState,
                refreshTriggerDistance = trigger,
                scale = true,
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primaryContainer,
                elevation = 8.dp,
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            items(
                items = state.remoteSources,
                contentType = {
                    return@items when (it) {
                        is SourceUiModel.Header -> "header"
                        is SourceUiModel.Item -> "item"
                    }
                },
                key = {
                    when (it) {
                        is SourceUiModel.Header -> it.hashCode()
                        is SourceUiModel.Item -> it.source.key(it.state)
                    }
                },
            ) { catalog ->
                when (catalog) {
                    is SourceUiModel.Header -> {
                        SourceHeader(
                            modifier = Modifier.animateItemPlacement(),
                            language = catalog.language,
                        )
                    }
                    is SourceUiModel.Item -> {
                        if (catalog.source is CatalogLocal) {
                            CatalogItem(
                                catalog = catalog.source,
                                installStep = if (catalog.source is CatalogInstalled) state.installSteps[catalog.source.pkgName] else null,
                                onInstall = { onClickInstall(catalog.source) }.takeIf { catalog.source.hasUpdate },
                                onUninstall = { onClickUninstall(catalog.source) }.takeIf { catalog.source is CatalogInstalled },
                                onCancelInstaller = {
                                    if (onCancelInstaller != null) {
                                        onCancelInstaller(it)
                                    }
                                },
                                showLoading = true
                            )
                        } else if (catalog.source is CatalogRemote) {
                            CatalogItem(
                                catalog = catalog.source,
                                installStep = if (catalog.source is CatalogInstalled) state.installSteps[catalog.source.pkgName] else null,
                                onInstall = { onClickInstall(catalog.source) },
                                onCancelInstaller = onCancelInstaller,
                                showLoading = true

                            )
                        }
                    }

                }
            }
        }
    }
}
