package ir.kazemcodes.infinity.library_feature.presentation.screen.library_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ir.kazemcodes.infinity.presentation.screen.components.LinearViewList

@Composable
fun LibraryScreen(
    navController : NavController = rememberNavController(),
    viewModel: LibraryViewModel = hiltViewModel()
) {

    val state = viewModel.state.value


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.books.isNotEmpty()) {
            LinearViewList(books = state.books, navController)
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

