package ir.kazemcodes.infinity.presentation.screen


import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ir.kazemcodes.infinity.base_feature.theme.InfinityTheme
import ir.kazemcodes.infinity.base_feature.util.BookTest
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.BookDetailViewModel
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.components.BookDetailChapterInfoComposable
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.components.BookDetailToggleButtonsComposable
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.components.BookImageInfoComposable
import ir.kazemcodes.infinity.presentation.screen.components.ExpandableCardComposable


@ExperimentalMaterialApi
@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel = hiltViewModel(),
    book: Book = Book.create(),
    scrollState: ScrollState = rememberScrollState(),
    navController: NavController = rememberNavController()
) {
    val detailState = viewModel.detailState.value
    val chapterState = viewModel.chapterState.value

    var isDetailScreen by remember {
        mutableStateOf(true)
    }

    Box(modifier = Modifier.fillMaxSize()) {


        if (!detailState.book.description.isNullOrEmpty()) {
            val bookDetail = detailState.book



            Column(
                modifier = modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                BookImageInfoComposable(bookDetail = bookDetail)
                Spacer(modifier = modifier.height(16.dp))
                BookDetailToggleButtonsComposable(bookDetail , navController)
                Spacer(modifier = modifier.height(16.dp))
                ExpandableCardComposable(
                    title = "Summary",
                    description = bookDetail.description ?: "Unknown"
                )
                Spacer(modifier = modifier.height(16.dp))
                BookDetailChapterInfoComposable(chapterState = chapterState , navController = navController)

            }

        }


        if (detailState.error.isNotBlank()) {
            Text(
                text = detailState.error,
                color = MaterialTheme.colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .align(Alignment.Center)
            )
        }
        if (detailState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

}


@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun BookDetailScreenPreview() {
    InfinityTheme {
        BookDetailScreen()

    }
}

@ExperimentalMaterialApi
@Preview(showBackground = true, name = "Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun BookDetailScreenPreviewDark() {
    InfinityTheme {
        BookDetailScreen(book = BookTest.bookTest)
    }
}

