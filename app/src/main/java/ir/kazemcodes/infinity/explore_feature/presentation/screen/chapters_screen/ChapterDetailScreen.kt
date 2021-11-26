package ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import ir.kazemcodes.infinity.base_feature.util.Routes
import ir.kazemcodes.infinity.explore_feature.domain.util.encodeString
import ir.kazemcodes.infinity.explore_feature.presentation.screen.chapters_screen.ChapterDetailViewModel


@Composable
fun ChapterDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: ChapterDetailViewModel = hiltViewModel()
) {



    Box(modifier = modifier.fillMaxSize()) {
    val chapters = viewModel.state.value.chapters
        if (viewModel.state.value.chapters.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(count = chapters.size) { index ->
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                navController.navigate(Routes.ReadingScreen.plus("?url=${encodeString(chapters[index].link)}&name=${chapters[index].bookName}&chapterNumber=${chapters[index].index}"))
                            }
                    ) {
                        Spacer(modifier = modifier.width(8.dp))
                        Text(text = chapters[index].bookName)
                    }
                }

            }
        }

        if (viewModel.state.value.error.isNotBlank()) {
            Text(
                text = viewModel.state.value.error,
                color = MaterialTheme.colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.Center)
            )
        }
        if (viewModel.state.value.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

