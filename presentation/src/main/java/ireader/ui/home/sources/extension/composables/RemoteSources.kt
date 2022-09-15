package ireader.ui.home.sources.extension.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ireader.common.models.entities.Catalog
import ireader.common.models.entities.CatalogInstalled
import ireader.common.models.entities.CatalogLocal
import ireader.common.models.entities.CatalogRemote
import ireader.common.models.entities.SourceState
import ireader.common.models.entities.key
import ireader.ui.home.sources.extension.CatalogItem
import ireader.ui.home.sources.extension.ExtensionViewModel
import ireader.ui.home.sources.extension.SourceHeader
import ireader.ui.home.sources.extension.SourceKeys


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteSourcesScreen(
    modifier: Modifier = Modifier,
    vm: ExtensionViewModel,
    onRefreshCatalogs: () -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
) {
    val allCatalogs = remember {
        derivedStateOf {
            vm.pinnedCatalogs + vm.unpinnedCatalogs
        }
    }
    val remotesCatalogs = remember {
        derivedStateOf {

            vm.remoteCatalogs
        }
    }
    val installed = remember {
        derivedStateOf {
            listOf(SourceUiModel.Header(SourceKeys.INSTALLED_KEY)) + allCatalogs.value.map {
                SourceUiModel.Item(it, SourceState.Installed)
            }
        }
    }
    val remotes = remember {
        derivedStateOf {
            listOf(SourceUiModel.Header(SourceKeys.AVAILABLE)) + remotesCatalogs.value.map {
                SourceUiModel.Item(it, SourceState.Remote)
            }
        }
    }

    val remoteSources = remember {
        derivedStateOf {
            (installed.value + remotes.value).mapIndexed { index, sourceUiModel ->  Pair(index,sourceUiModel) }
        }
    }
    ireader.core.api.log.Log.error { allCatalogs.value.toString()}

    val scrollState = rememberLazyListState()
    val swipeState = rememberSwipeRefreshState(isRefreshing = vm.isRefreshing)

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
                items = remoteSources.value,
                contentType = {
                    return@items when (it.second) {
                        is SourceUiModel.Header -> "header"
                        is SourceUiModel.Item -> "item"
                    }
                },
                key = {
                    when (val catalog : SourceUiModel = it.second) {
                        is SourceUiModel.Header -> it.second.hashCode()
                        is SourceUiModel.Item -> catalog.source.key(catalog.state,it.first.toLong())
                    }
                },
            ) { catalog ->
                val catalog = remember {
                    catalog.second
                }
                when (catalog) {
                    is SourceUiModel.Header -> {
                        SourceHeader(
                            modifier = Modifier.animateItemPlacement(),
                            language = catalog.language,
                        )
                    }
                    is SourceUiModel.Item -> {
                        when (catalog.source) {
                            is CatalogLocal -> {
                                CatalogItem(
                                    catalog = catalog.source,
                                    installStep = if (catalog.source is CatalogInstalled) vm.installSteps[catalog.source.pkgName] else null,
                                    onInstall = { onClickInstall(catalog.source) }.takeIf { catalog.source.hasUpdate },
                                    onUninstall = { onClickUninstall(catalog.source) }.takeIf { catalog.source is CatalogInstalled },
                                    onCancelInstaller = {
                                        if (onCancelInstaller != null) {
                                            onCancelInstaller(it)
                                        }
                                    },
                                )
                            }
                            is CatalogRemote -> {
                                CatalogItem(
                                    catalog = catalog.source,
                                    installStep = vm.installSteps[catalog.source.pkgName],
                                    onInstall = { onClickInstall(catalog.source) },
                                    onCancelInstaller = {
                                        if (onCancelInstaller != null) {
                                            onCancelInstaller(it)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
