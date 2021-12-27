package ir.kazemcodes.infinity.presentation.extension

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.base_feature.navigation.BrowserScreenKey
import ir.kazemcodes.infinity.data.network.sources

@Composable
fun ExtensionScreen(modifier: Modifier = Modifier) {
    val backStack = LocalBackstack.current
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
                elevation = 8.dp,
            )
        }
    ) {
        LazyColumn {
            items(sources.size) { index ->
                Text(sources[index].name, modifier = modifier
                    .padding(16.dp)
                    .clickable {
                        backStack.goTo(BrowserScreenKey(sources[index].name))
                    })
            }
        }


    }
}