package org.ireader.presentation.feature_sources.presentation.extension.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.ireader.common_models.entities.Catalog
import org.ireader.common_models.entities.CatalogInstalled
import org.ireader.core_api.log.Log
import org.ireader.core_ui.ui_components.TextSection
import org.ireader.presentation.feature_sources.presentation.extension.CatalogItem
import org.ireader.presentation.feature_sources.presentation.extension.CatalogsState


@Composable
fun RemoteSourcesScreen(
    modifier: Modifier = Modifier,
    state: CatalogsState,
    onRefreshCatalogs: () -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val swipeState = rememberSwipeRefreshState(isRefreshing = state.isRefreshing)
    val allCatalogs = (state.pinnedCatalogs + state.unpinnedCatalogs)
    SwipeRefresh(state = swipeState,
        onRefresh = { onRefreshCatalogs() },
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
            if (allCatalogs.isNotEmpty()) {
                item {
                    TextSection(
                        text = "Installed",
                    )
                }
                items(allCatalogs.size) { index ->
                    val catalog = allCatalogs[index]
                    CatalogItem(
                        catalog = catalog,
                        installStep = if (catalog is CatalogInstalled) state.installSteps[catalog.pkgName] else null,
                        onInstall = { onClickInstall(catalog) }.takeIf { catalog.hasUpdate },
                        onUninstall = { onClickUninstall(catalog) }.takeIf { catalog is CatalogInstalled },
                    )
                }
            }
            if (state.remoteCatalogs.isNotEmpty()) {
                item {
                    TextSection(
                        text = "Available",
                    )
                }

                item {
                    LanguageChipGroup(
                        choices = state.languageChoices,
                        selected = state.selectedLanguage,
                        onClick = { state.selectedLanguage = it },
                        modifier = Modifier.padding(8.dp)
                    )
                }
                //ERROR : this lines may throws anexception
                kotlin.runCatching {
                    items(state.remoteCatalogs.size) { index ->
                        val catalog = state.remoteCatalogs[index]
                        CatalogItem(
                            catalog = catalog,
                            installStep = state.installSteps[catalog.pkgName],
                            onInstall = { onClickInstall(catalog) }
                        )
                    }
                }.getOrElse {
                    Log.error(it,"Remote Sources throws an exception")
                    Log.error { it.message.toString() }
                }
            }
        }
    }
}