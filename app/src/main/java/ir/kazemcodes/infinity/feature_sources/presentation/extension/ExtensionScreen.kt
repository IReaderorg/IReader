package ir.kazemcodes.infinity.feature_sources.presentation.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackextensions.servicesktx.lookup
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.feature_activity.presentation.BrowserScreenKey
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants.DEFAULT_ELEVATION
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreType
import ir.kazemcodes.infinity.feature_sources.sources.Extensions


@Composable
fun ExtensionScreen(modifier: Modifier = Modifier) {
    val backstack = LocalBackstack.current
    val extensions: Extensions = remember {
        backstack.lookup<Extensions>()
    }

    val sources = extensions.getSources()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Extensions",
                        color = MaterialTheme.colors.onBackground,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = DEFAULT_ELEVATION,
            )
        }
    ) {
        LazyColumn {
            items(sources.size) { index ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(30.dp)
                    .clickable {
                        backstack.goTo(BrowserScreenKey(sources[index].name, exploreType = ExploreType.Latest.mode))
                    },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(sources[index].name)
                    if (sources[index].supportsMostPopular) {
                        Text(stringResource(R.string.popular_book),
                            color = MaterialTheme.colors.primary,
                            style = MaterialTheme.typography.subtitle2,
                            modifier = Modifier.clickable {
                                backstack.goTo(BrowserScreenKey(sourceName = sources[index].name,
                                    exploreType = ExploreType.Popular.mode))
                            })
                    }
                }

            }
        }


    }
}