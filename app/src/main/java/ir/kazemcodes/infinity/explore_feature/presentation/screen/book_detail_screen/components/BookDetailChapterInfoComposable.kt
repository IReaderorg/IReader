package ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.base_feature.navigation.ChapterDetailKey
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.BookDetailViewModel
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.ChapterState

@Composable
fun BookDetailChapterInfoComposable(
    viewModel: BookDetailViewModel,
    modifier: Modifier = Modifier,
    chapters: ChapterState,
    book: Book
) {
    val backStack = LocalBackstack.current
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
            //navController.navigate(Routes.ChapterDetailScreen.plus("/${viewModel.detailState.value.book.bookName}"))
            backStack.goTo(ChapterDetailKey(chapters = chapters.chapters, book = book))
        })

    }
}