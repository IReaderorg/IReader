package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.CatalogLocal
import ireader.presentation.ui.home.sources.extension.CatalogItem


@Composable
fun SourceListComposable(
    modifier: Modifier = Modifier,
    sources: List<CatalogLocal>,
    scrollState: LazyListState,
    onExploreNavigation: (index: Int) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        state = scrollState,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(sources.size, key = { sources[it].sourceId }) { index ->
            CatalogItem(
                catalog = sources[index],
                onClick = {
                    onExploreNavigation(index)
                }
            )
        }
    }
}

fun iconFinder(source: CatalogLocal): String {
    return "https://github.com/kazemcodes/IReader-Sources/tree/main/src/${source.source?.lang}/${source.name}/icon.png"
}
