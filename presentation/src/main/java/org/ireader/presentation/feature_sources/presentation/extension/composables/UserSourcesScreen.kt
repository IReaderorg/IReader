package org.ireader.presentation.feature_sources.presentation.extension.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.ireader.domain.models.entities.Catalog
import org.ireader.domain.models.entities.CatalogInstalled
import org.ireader.domain.models.entities.CatalogLocal
import org.ireader.presentation.feature_sources.presentation.extension.CatalogItem
import org.ireader.presentation.feature_sources.presentation.extension.CatalogsState
import timber.log.Timber


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserSourcesScreen(
    modifier: Modifier = Modifier,
    state: CatalogsState,
    onClickCatalog: (Catalog) -> Unit,
    onClickTogglePinned: (CatalogLocal) -> Unit,
) {
    val scrollState = rememberLazyListState()

    val langs = state.unpinnedCatalogs.map { it.source.lang }.distinct()

    val catalogLocalItem: LazyListScope.(CatalogLocal) -> Unit = { catalog ->
        kotlin.runCatching {
            item {
                CatalogItem(
                    catalog = catalog,
                    installStep = if (catalog is CatalogInstalled) state.installSteps[catalog.pkgName] else null,
                    onClick = { onClickCatalog(catalog) },
                    onPinToggle = { onClickTogglePinned(catalog) }
                )
            }
        }.getOrElse {
            Timber.e(catalog.name + "Throws an error" + it.message)
        }
    }
    LazyColumn(modifier = modifier
        .fillMaxSize(),
        state = scrollState,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
        kotlin.runCatching {
            if (state.pinnedCatalogs.isNotEmpty()) {
                item {
                    CatalogsSection(
                        text = "Pinned",
                    )
                }
                for (catalog in state.pinnedCatalogs) {
                    catalogLocalItem(catalog)
                }
            }

            if (state.unpinnedCatalogs.isNotEmpty()) {
                for (lang in langs) {
                    item {
                        CatalogsSection(
                            text = lang,
                        )
                    }
                    for (catalog in state.unpinnedCatalogs.filter { it.source.lang == lang }) {
                        catalogLocalItem(catalog)
                    }
                }


            }
        }.getOrElse {
            Timber.e("Throws an error" + it.message)
        }
    }
}