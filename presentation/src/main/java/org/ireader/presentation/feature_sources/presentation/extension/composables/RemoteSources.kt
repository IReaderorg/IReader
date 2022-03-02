package org.ireader.presentation.feature_sources.presentation.extension.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.ireader.domain.models.entities.Catalog
import org.ireader.domain.models.entities.CatalogInstalled
import org.ireader.domain.models.entities.CatalogLocal
import org.ireader.presentation.feature_sources.presentation.extension.CatalogItem
import org.ireader.presentation.feature_sources.presentation.extension.CatalogsState
import org.ireader.presentation.feature_sources.presentation.extension.ExtensionViewModel
import timber.log.Timber


@Composable
fun RemoteSourcesScreen(
    modifier: Modifier = Modifier,
    viewModel: ExtensionViewModel,
    state: CatalogsState,
    onRefreshCatalogs: () -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val swipeState = rememberSwipeRefreshState(isRefreshing = state.isRefreshing)
    val all = (state.pinnedCatalogs + state.unpinnedCatalogs)
    val catalogLocalItem: LazyListScope.(CatalogLocal) -> Unit = { catalog ->
        item(key = catalog.sourceId.toString() + catalog.name) {
            kotlin.runCatching {
                CatalogItem(
                    catalog = catalog,
                    installStep = if (catalog is CatalogInstalled) state.installSteps[catalog.pkgName] else null,
                    onInstall = { onClickInstall(catalog) }.takeIf { catalog.hasUpdate },
                    onUninstall = { onClickUninstall(catalog) }.takeIf { catalog is CatalogInstalled },
                )
            }.getOrElse {
                Timber.e(catalog.name + "Throws an error" + it.message)
            }
        }
    }

    SwipeRefresh(state = swipeState,
        onRefresh = { viewModel.refreshCatalogs() },
        indicator = { _, trigger ->
            SwipeRefreshIndicator(
                state = swipeState,
                refreshTriggerDistance = trigger,
                scale = true,
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.primaryVariant,
                elevation = 8.dp,
            )
        }) {
        LazyColumn(modifier = modifier
            .fillMaxSize(),
            state = scrollState,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (all.isNotEmpty()) {
                item(key = "h2") {
                    CatalogsSection(
                        text = "Installed",
                    )
                }

                for (catalog in all) {
                    catalogLocalItem(catalog)
                }

            }
            if (state.remoteCatalogs.isNotEmpty()) {
                item(key = "h3") {
                    CatalogsSection(
                        text = "Available",
                    )
                }

                item(key = "langs") {
                    LanguageChipGroup(
                        choices = state.languageChoices,
                        selected = state.selectedLanguage,
                        onClick = { state.selectedLanguage = it },
                        modifier = Modifier.padding(8.dp)
                    )
                }
                //ERROR : this lines may throws anexception
                kotlin.runCatching {
                    items(state.remoteCatalogs, key = { it.sourceId }) { catalog ->
                        CatalogItem(
                            catalog = catalog,
                            installStep = state.installSteps[catalog.pkgName],
                            onInstall = { onClickInstall(catalog) }
                        )
                    }
                }.getOrElse {
                    Timber.e(it.message)
                }
            }
        }
    }
}