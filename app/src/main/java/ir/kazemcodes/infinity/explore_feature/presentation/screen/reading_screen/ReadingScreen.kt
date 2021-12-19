package ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.kazemcodes.infinity.api_feature.HttpSource
import ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen.DEFAULT.MAX_BRIGHTNESS
import ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen.DEFAULT.MIN_BRIGHTNESS
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen.components.FontMenuComposable
import ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen.components.FontSizeChangerComposable
import kotlin.math.abs


@ExperimentalMaterialApi
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
    book: Book = Book.create(),
    chapter: Chapter = Chapter.create(),
    viewModel: ReadingScreenViewModel = hiltViewModel(),
    api:HttpSource
) {
    
    val state = viewModel.state.value
    var readMode by remember {
        mutableStateOf(true)
    }
    Box(modifier = modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                if (!readMode) {
                    TopAppBar(backgroundColor = MaterialTheme.colors.background,
                        modifier = modifier.border(1.dp, color = MaterialTheme.colors.onBackground.copy(.4f)),
                        title = { Text(text = book.bookName) }
                    )
                }
            },
            bottomBar = {
                if (!readMode) {
                    BottomAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .border(1.dp, color = MaterialTheme.colors.onBackground.copy(.4f)),
                        backgroundColor = MaterialTheme.colors.background
                    ) {
                        Column(
                            modifier = modifier
                                .fillMaxSize()
                                .padding(8.dp),
                        ) {
                            Slider(
                                viewModel.brightness.value, { viewModel.changeBrightness(it) },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                valueRange = MIN_BRIGHTNESS..MAX_BRIGHTNESS
                            )
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
                                onClick = { selectedFont ->
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
                    viewModel.getReadingContent(chapter.copy(bookName = book.bookName) )
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
        /**  I done this because the slider range start from 0f until 1f so i need to reverse the number **/
        Box(modifier = modifier.fillMaxSize().background(color = Color.Black.copy(abs(viewModel.brightness.value-MAX_BRIGHTNESS)))){}
    }
}


