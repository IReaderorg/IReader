package ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ir.kazemcodes.infinity.base_feature.util.BookTest
import ir.kazemcodes.infinity.base_feature.util.Routes
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.explore_feature.domain.util.encodeUrl


@Composable
fun ChapterDetailScreen(
    modifier: Modifier = Modifier,
    chapters: List<Chapter> = emptyList(),
    navController: NavController = rememberNavController()
    //viewModel: BookDetailViewModel = hiltViewModel()
) {


    //chapters = BookTest.chapters//viewModel.chapterState.value.chapters

    Box(modifier = modifier.fillMaxSize()) {



        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(count = chapters.size) { index ->
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            navController.navigate(Routes.ReadingScreen.plus("?url=${encodeUrl(chapters[index].link)}&name=${chapters[index].title}&chapterNumber=${chapters[index].index}"))
                        }
                ) {
                    Spacer(modifier = modifier.width(8.dp))
                    Text(text = chapters[index].title)
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChapterDetailPreview() {
    ChapterDetailScreen(chapters = BookTest.chapters)
}