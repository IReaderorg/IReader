package ireader.ui.home.sources.extension.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.common.models.entities.Catalog
import ireader.common.models.entities.CatalogInstalled
import ireader.common.models.entities.SourceState
import ireader.common.models.entities.key
import ireader.ui.home.sources.extension.CatalogItem
import ireader.ui.home.sources.extension.ExtensionViewModel
import ireader.ui.home.sources.extension.SourceHeader

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun UserSourcesScreen(
    modifier: Modifier = Modifier,
    vm: ExtensionViewModel,
    onClickCatalog: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val usersSources = remember {
        derivedStateOf {
            vm.userSources.mapIndexed { index, sourceUiModel ->
                Pair(index.toLong(), sourceUiModel)
            }
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scrollState,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        items(
            items = usersSources.value,
            contentType = {
                return@items when (val uiModel = it.second) {
                    is SourceUiModel.Header -> "header"
                    is SourceUiModel.Item -> "item"
                }
            },
            key = {
                when (val uiModel = it.second) {
                    is SourceUiModel.Header -> it.hashCode()
                    is SourceUiModel.Item -> uiModel.source.key(uiModel.state, it.first)
                }
            },
        ) { catalog ->
            val catalogItem = remember {
                catalog.second
            }
            when (catalogItem) {
                is SourceUiModel.Header -> {
                    SourceHeader(
                        modifier = Modifier.animateItemPlacement(),
                        language = catalogItem.language,
                    )
                }
                is SourceUiModel.Item -> CatalogItem(
                    modifier = Modifier.animateItemPlacement(),
                    catalog = catalogItem.source,
                    installStep = if (catalogItem.source is CatalogInstalled) vm.installSteps[catalogItem.source.pkgName] else null,
                    onClick = { onClickCatalog(catalogItem.source) },
                    onPinToggle = { onClickTogglePinned(catalogItem.source) },
                )
            }
        }
    }
}

sealed class SourceUiModel {
    data class Item(val source: Catalog, val state: SourceState) : SourceUiModel()
    data class Header(val language: String) : SourceUiModel()
}
