package org.ireader.presentation.feature_sources.presentation.extension.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.ireader.domain.models.entities.CatalogLocal
import org.ireader.presentation.feature_sources.presentation.extension.CatalogItem
import org.ireader.presentation.ui.ExploreScreenSpec

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SourceListComposable(
    modifier: Modifier = Modifier,
    sources: List<CatalogLocal>,
    scrollState: LazyListState,
    navController: NavController,
) {
    LazyColumn(modifier = modifier
        .fillMaxSize(),
        state = scrollState,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
        items(sources.size) { index ->
            CatalogItem(
                catalog = sources[index],
                onClick = {
                    navController.navigate(ExploreScreenSpec.buildRoute(
                        sourceId = sources[index].sourceId,
                    ))
                })
//            ListItem(
//                modifier = Modifier
//                    .clickable {
//                        navController.navigate(ExploreScreenSpec.buildRoute(
//                            sourceId = sources[index].sourceId,
//                            exploreType = ExploreType.Latest.id
//                        ))
//                    }
//                    .height(60.dp),
//                text = { Text(sources[index].name) },
//                trailing = {},
//                secondaryText = {},
//                icon = {
//
//                    Box(modifier = Modifier
//                        .clip(RoundedCornerShape(2.dp))
//                        .shadow(elevation = 1.dp)
//                        .width(40.dp)
//                        .height(100.dp),
//                        contentAlignment = Alignment.Center) {
//                        BookImageComposable(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .padding(1.dp),
//                            //TODO need to add this later
//                            image = "https://github.com/LNReader/lnreader-sources/blob/main/src/en/comrademao/icon.png",
//                            contentScale = ContentScale.Inside,
//                            alignment = Alignment.Center,
//                            placeholder = org.ireader.core.R.drawable.ic_wallpaper
//                        )
//                    }
//                }
//            )
        }

    }
}

fun iconFinder(source: CatalogLocal): String {
    return "https://github.com/kazemcodes/IReader-Sources/tree/main/src/${source.source.lang}/${source.name}/icon.png"
}