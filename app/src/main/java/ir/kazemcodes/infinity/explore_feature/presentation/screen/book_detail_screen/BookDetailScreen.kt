package ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen


import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ir.kazemcodes.infinity.base_feature.theme.InfinityTheme
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.components.BookDetailChapterInfoComposable
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.components.BookDetailToggleButtonsComposable
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.components.BookImageInfoComposable
import ir.kazemcodes.infinity.presentation.screen.components.ExpandableCardComposable
import kotlinx.coroutines.CoroutineScope


@ExperimentalMaterialApi
@Composable
fun BookDetailScreen(
    book: Book = Book.create(),
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel = hiltViewModel(),
    scrollState: ScrollState = rememberScrollState(),
    navController: NavController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    LaunchedEffect(key1 = true) {
        viewModel.getBookData(book = book)
    }
    val detailState = viewModel.detailState.value
    val chapterState = viewModel.chapterState.value
    val aBook = detailState.book

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                TopAppBar(
                    backgroundColor = MaterialTheme.colors.background,
                    elevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 16.dp, top = 16.dp),
                        Arrangement.End
                    ) {

                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Icon",
                            tint = MaterialTheme.colors.onBackground,
                            modifier = modifier.clickable {
                                viewModel.getRemoteBookDetail()
                            }
                        )
                    }
                }
            }
        ) {
            if (!aBook.description.isNullOrEmpty()) {
                Column(
                    modifier = modifier
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    BookImageInfoComposable(bookDetail = aBook)
                    Spacer(modifier = modifier.height(16.dp))
                    BookDetailToggleButtonsComposable(
                        aBook,
                        chapters = chapterState.chapters,
                        viewModel = viewModel
                    )
                    Spacer(modifier = modifier.height(16.dp))
                    ExpandableCardComposable(
                        title = "Summary",
                        description = aBook.description ?: "Unknown"
                    )
                    Spacer(modifier = modifier.height(16.dp))
                    BookDetailChapterInfoComposable(
                        chapters = chapterState,
                        viewModel = viewModel,
                        book = aBook
                    )

                }


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
        BookDetailScreen()
    }
}

