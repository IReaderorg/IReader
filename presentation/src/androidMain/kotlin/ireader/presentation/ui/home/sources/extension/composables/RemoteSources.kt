package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.*
import ireader.presentation.ui.home.sources.extension.CatalogItem
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.extension.SourceHeader
import ireader.presentation.ui.home.sources.extension.SourceKeys


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun RemoteSourcesScreen(
    modifier: Modifier = Modifier,
    vm: ExtensionViewModel,
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
    val scrollState = rememberLazyListState()

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
                        is SourceUiModel.Item -> catalog.source.key(catalog.state,it.first.toLong(),vm.defaultRepo.value)
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
