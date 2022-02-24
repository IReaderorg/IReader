package org.ireader.presentation.feature_sources.presentation.extension.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.ireader.core.R
import org.ireader.domain.models.ExploreType
import org.ireader.presentation.presentation.components.BookImageComposable
import org.ireader.presentation.ui.ExploreScreenSpec
import org.ireader.source.core.HttpSource

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SourceListComposable(
    modifier: Modifier = Modifier,
    sources: List<HttpSource>,
    scrollState: LazyListState,
    navController: NavController,
) {

    LazyColumn(modifier = modifier
        .fillMaxSize(),
        state = scrollState,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
        items(sources.size) { index ->
            ListItem(
                modifier = Modifier
                    .clickable {
                        if (sources[index]
                                .getListings()
                                .find { it.name == "popular" } != null
                        ) {
                            navController.navigate(ExploreScreenSpec.buildRoute(
                                sourceId = sources[index].id,
                                exploreType = ExploreType.Popular.id
                            ))
                        }
                    }
                    .height(60.dp),
                text = { Text(sources[index].name) },
                trailing = {
                    if (sources[index].getListings().find { it.name == "latest" } != null) {
                        Text(stringResource(R.string.latest_book),
                            color = MaterialTheme.colors.primary,
                            style = MaterialTheme.typography.subtitle2,
                            modifier = Modifier.clickable {
                                navController.navigate(ExploreScreenSpec.buildRoute(
                                    sourceId = sources[index].id,
                                    exploreType = ExploreType.Latest.id
                                ))
                            })
                    }
                },
                secondaryText = {

                },
                icon = {
                    Box(modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .shadow(elevation = 1.dp)
                        .width(40.dp)
                        .height(100.dp),
                        contentAlignment = Alignment.Center) {
                        BookImageComposable(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(1.dp),
                            //TODO change icon later
                            image = "",
                            contentScale = ContentScale.Inside,
                            alignment = Alignment.Center,
                            placeholder = org.ireader.core.R.drawable.ic_wallpaper
                        )
                    }
                }
            )
        }

    }
}