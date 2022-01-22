package ir.kazemcodes.infinity.feature_sources.presentation.extension.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.core.presentation.reusable_composable.SuperSmallTextComposable
import ir.kazemcodes.infinity.feature_activity.presentation.BrowserScreenKey
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreType
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UserSourcesScreen(viewModel: ExtensionViewModel) {
    val scrollState = rememberLazyListState()
    val sources = viewModel.state.value.sources
    val backstack = LocalBackstack.current
    LazyColumn(modifier = Modifier
        .fillMaxSize(),
        state = scrollState,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
        items(sources.size) { index ->
            ListItem(
                text = { Text(sources[index].name) },
                trailing = {
                    if (sources[index].supportsMostPopular) {
                        Text(stringResource(R.string.popular_book),
                            color = MaterialTheme.colors.primary,
                            style = MaterialTheme.typography.subtitle2,
                            modifier = Modifier.clickable {
                                backstack.goTo(BrowserScreenKey(sourceName = sources[index].name,
                                    exploreType = ExploreType.Popular.mode))
                            })
                    }
                },
                secondaryText = { SuperSmallTextComposable(title = "Created by ${sources[index].creator}") },
                modifier = Modifier.clickable {
                    backstack.goTo(BrowserScreenKey(sources[index].name,
                        exploreType = ExploreType.Latest.mode))
                })
        }
    }
}