package ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import ir.kazemcodes.infinity.base_feature.util.Routes
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.BookDetailViewModel
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.ChapterState

@Composable
fun BookDetailChapterInfoComposable(viewModel: BookDetailViewModel,modifier : Modifier = Modifier, chapters : ChapterState ,navController : NavController,book : Book) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(40.dp)
    ) {

        if (chapters.chapters.isNotEmpty()) {
            Text(
                text = "${chapters.chapters.size} Chapters",
                color = MaterialTheme.colors.onBackground
            )
        }
        if (chapters.chapters.isEmpty()){
            Text(text = "0 Chapters", color = MaterialTheme.colors.onBackground)
        }
        Text(text = "Details" , color = MaterialTheme.colors.primary , modifier = modifier.clickable {
            viewModel.insertChaptersToLocal(chapterEntities = chapters.chapters.map { it.copy(bookName = viewModel.detailState.value.book.bookName).toChapterEntity() },bookName = viewModel.detailState.value.book.bookName)
            navController.navigate(Routes.ChapterDetailScreen.plus("/${viewModel.detailState.value.book.bookName}"))
        })

    }
}