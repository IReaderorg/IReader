package ir.kazemcodes.infinity.explore_feature.presentation.screen.browse_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.base_feature.navigation.BookDetailKey
import ir.kazemcodes.infinity.explore_feature.presentation.screen.components.LinearViewList


@ExperimentalMaterialApi
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel = hiltViewModel(),
    navController : NavController = rememberNavController()
) {
    val backstack = LocalBackstack.current
    val state = viewModel.state.value
    Box(
            modifier = Modifier.fillMaxSize()
        ) {
        val context = LocalContext.current
        if (state.books.isNotEmpty()) {
        LinearViewList(books = state.books , onClick = {index->
            backstack.goTo(BookDetailKey(state.books[index]))
        })
        }


        if (state.error.isNotBlank()) {
            Text(
                text = state.error,
                color = MaterialTheme.colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.Center)
            )
        }
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


