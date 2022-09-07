package ireader.ui.sources.extension.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.common.models.entities.Catalog
import ireader.common.models.entities.CatalogInstalled
import ireader.common.models.entities.SourceState
import ireader.common.models.entities.key
import ireader.ui.sources.extension.CatalogItem
import ireader.ui.sources.extension.ExtensionViewModel
import ireader.ui.sources.extension.SourceHeader

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun UserSourcesScreen(
    modifier: Modifier = Modifier,
    state: ExtensionViewModel,
    onClickCatalog: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scrollState,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        items(
            items = state.userSources,
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
                is SourceUiModel.Item -> CatalogItem(
                    modifier = Modifier.animateItemPlacement(),
                    catalog = catalog.source,
                    installStep = if (catalog.source is CatalogInstalled) state.installSteps[catalog.source.pkgName] else null,
                    onClick = { onClickCatalog(catalog.source) },
                    onPinToggle = { onClickTogglePinned(catalog.source) },
                )
            }
        }
    }
}

sealed class SourceUiModel {
    data class Item(val source: Catalog, val state: SourceState) : SourceUiModel()
    data class Header(val language: String) : SourceUiModel()
}
