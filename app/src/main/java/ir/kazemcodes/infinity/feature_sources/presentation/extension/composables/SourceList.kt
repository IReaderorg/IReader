package ir.kazemcodes.infinity.feature_sources.presentation.extension.composables

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
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.presentation.components.BookImageComposable
import ir.kazemcodes.infinity.core.presentation.reusable_composable.SuperSmallTextComposable
import ir.kazemcodes.infinity.feature_activity.presentation.Screen
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreType

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SourceList(
    modifier: Modifier = Modifier,
    sources: List<Source>,
    scrollState: LazyListState,
    navController: NavController,
) {

    LazyColumn(modifier = modifier
        .fillMaxSize(),
        state = scrollState,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
        items(sources.size) { index ->
//            Row(modifier= modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
//                Box(modifier) {
//                    BookImageComposable(
//                        image = sources[index].iconUrl,
//                        modifier = modifier
//                            .clip(RoundedCornerShape(8.dp))
//                            .size(25.dp),
//                        contentScale = ContentScale.FillBounds,
//                        alignment = Alignment.Center
//                    )
//                }
//                Text(sources[index].name)
//                if (sources[index].supportsMostPopular) {
//                    Text(stringResource(R.string.popular_book),
//                        color = MaterialTheme.colors.primary,
//                        style = MaterialTheme.typography.subtitle2,
//                        modifier = Modifier.clickable {
//                            navController.navigate(Screen.Explore.passArgs(
//                                sourceId = sources[index].sourceId,
//                                exploreType = ExploreType.Popular.mode
//                            ))
//                        })
//                }
//            }
            ListItem(
                modifier = Modifier
                    .clickable {
                        navController.navigate(Screen.Explore.passArgs(
                            sourceId = sources[index].sourceId,
                            exploreType = ExploreType.Latest.mode
                        ))
                    }
                    .height(60.dp),
                text = { Text(sources[index].name) },
                trailing = {
                    if (sources[index].supportsMostPopular) {
                        Text(stringResource(R.string.popular_book),
                            color = MaterialTheme.colors.primary,
                            style = MaterialTheme.typography.subtitle2,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Explore.passArgs(
                                    sourceId = sources[index].sourceId,
                                    exploreType = ExploreType.Popular.mode
                                ))
                            })
                    }
                },
                secondaryText = {
                    SuperSmallTextComposable(title = "Created by ${sources[index].creator}",
                        color = MaterialTheme.colors.onBackground.copy(alpha = .4f))
                },
                icon = {
                    Box(modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .shadow(elevation = 1.dp)
                        .width(40.dp)
                        .height(100.dp),
                        contentAlignment = Alignment.Center){
                        BookImageComposable(
                            modifier = Modifier.fillMaxSize().padding(1.dp),
                            image = sources[index].iconUrl,
                            contentScale = ContentScale.Inside,
                            alignment = Alignment.Center,
                            placeholder = R.drawable.ic_wallpaper
                        )
                    }
                }
            )
        }

    }
}