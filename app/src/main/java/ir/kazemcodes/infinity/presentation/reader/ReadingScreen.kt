package ir.kazemcodes.infinity.presentation.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.kazemcodes.infinity.domain.network.models.HttpSource
import ir.kazemcodes.infinity.presentation.book_detail.DEFAULT.MAX_BRIGHTNESS
import ir.kazemcodes.infinity.presentation.book_detail.DEFAULT.MIN_BRIGHTNESS
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.presentation.reader.components.FontMenuComposable
import ir.kazemcodes.infinity.presentation.reader.components.FontSizeChangerComposable
import kotlin.math.abs


@ExperimentalMaterialApi
@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
    book: Book = Book.create(),
    chapter: Chapter = Chapter.create(),
    viewModel: ReadingScreenViewModel = hiltViewModel(),
    api: HttpSource
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
                        title = { Text(text = book.bookName) },
                        elevation = 8.dp
                    )
                }
            },
            bottomBar = {
                if (!readMode) {
                    BottomAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        elevation = 8.dp,
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
        /**  I done this because the slider range start from 0f until 1f so i need to reverse the number
         * so when the slider goes to left decrease the brightness and when it goes to right it will increase the brightness
         * **/
        Box(modifier = modifier.fillMaxSize().background(color = Color.Black.copy(abs(viewModel.brightness.value-MAX_BRIGHTNESS)))){}
    }
}


