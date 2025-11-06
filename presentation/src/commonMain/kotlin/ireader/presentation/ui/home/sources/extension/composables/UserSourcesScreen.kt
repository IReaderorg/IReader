package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.*
import ireader.presentation.ui.home.sources.extension.CatalogItem
import ireader.presentation.ui.home.sources.extension.ExtensionViewModel
import ireader.presentation.ui.home.sources.extension.SourceHeader
import ireader.presentation.ui.home.sources.extension.SourceUiModel

@OptIn(ExperimentalFoundationApi::class)
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
                Pair((vm.userSources.size - index).toLong(), sourceUiModel)
            }
        }
    }
    LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
        item(key = "language_filter") {
            LanguageChipGroup(
                choices = vm.languageChoices,
                selected = vm.selectedUserSourceLanguage,
                onClick = { vm.selectedUserSourceLanguage = it },
                isVisible = vm.showLanguageFilter.value,
                onToggleVisibility = { visible ->
                    vm.uiPreferences.showLanguageFilter().set(visible)
                }
            )
        }

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
                        is SourceUiModel.Header -> it.second.hashCode()
                        is SourceUiModel.Item -> uiModel.source.key(uiModel.state, it.first, vm.defaultRepo.value)
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

