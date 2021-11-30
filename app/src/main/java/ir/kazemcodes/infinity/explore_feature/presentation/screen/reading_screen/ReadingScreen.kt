package ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.kazemcodes.infinity.base_feature.theme.InfinityTheme
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen.components.FontMenuComposable
import ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen.components.FontSizeChangerComposable

@ExperimentalMaterialApi
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
    book: Book = Book.create(),
    chapter: Chapter = Chapter.create(),
    viewModel: ReadingScreenViewModel = hiltViewModel(),
) {

    val context = LocalContext.current
    val state = viewModel.state.value
    var readMode by remember {
        mutableStateOf(true)
    }


    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (!readMode) {
                    TopAppBar(backgroundColor = MaterialTheme.colors.background.copy(.8f),
                        title = { Text(text = book.bookName) }
                    )
                }
            },
            bottomBar = {
                if (!readMode) {
                    BottomAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .border(1.dp, color = MaterialTheme.colors.onBackground.copy(.4f)),
                        backgroundColor = MaterialTheme.colors.background.copy(.8f)
                    ) {
                        Column(
                            modifier = modifier
                                .fillMaxSize()
                                .padding(8.dp),
                        ) {
                            FontSizeChangerComposable(
                                onFontDecease = {
                                    viewModel.decreaseFontSize()
                                },
                                ontFontIncrease = {
                                    viewModel.increaseFontsSize()
                                },
                                fontSize = viewModel.fontSize.value
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FontMenuComposable(
                                onClick = { selectedFont, fds ->
                                    viewModel.setFont(selectedFont)
                                },
                                viewModel = viewModel
                            )


                        }
                    }
                }


            }
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .clickable { readMode = !readMode }
                    .padding(16.dp)
            ) {

//
                LaunchedEffect(key1 = true) {
                    viewModel.getReadingContent(chapter.copy(bookName = book.bookName))
                    viewModel.readFromDatastore()
                }
                if (!state.chapter.content.isNullOrBlank()) {
                    Text(
                        text = state.chapter.content ?: "",
                        fontSize = viewModel.fontSize.value.sp,
                        fontFamily = viewModel.fontState.value
                    )
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
    }
}


@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun ReadingLight() {
    InfinityTheme {

        ReadingScreen()
    }
}


@ExperimentalMaterialApi
@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ReadingDark() {
    InfinityTheme {
        ReadingScreen()
    }
}

