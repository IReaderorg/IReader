package ir.kazemcodes.infinity.presentation.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.presentation.book_detail.DEFAULT.MAX_BRIGHTNESS
import ir.kazemcodes.infinity.presentation.book_detail.DEFAULT.MIN_BRIGHTNESS
import ir.kazemcodes.infinity.presentation.reader.components.FontMenuComposable
import ir.kazemcodes.infinity.presentation.reader.components.FontSizeChangerComposable
import ir.kazemcodes.infinity.util.findActivity


@Composable
fun ReadingScreen(
    modifier: Modifier = Modifier,
    book: Book = Book.create(),
    chapter: Chapter = Chapter.create(),
) {

    val viewModel = rememberService<ReaderScreenViewModel>()
    val backStack = LocalBackstack.current
    val  context = LocalContext.current
    val activity = context.findActivity()
    val state = viewModel.state.value


    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (!state.isReaderModeEnable && state.isLoaded) {
                    TopAppBar(
                        title = {
                            Text(
                                text = chapter.title,
                                color = MaterialTheme.colors.onBackground,
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.onBackground,
                        elevation = 8.dp,
                        navigationIcon = {
                            IconButton(onClick = { backStack.goBack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "ArrowBack",
                                    tint = MaterialTheme.colors.onBackground,
                                )
                            }
                        }
                    )
                } else if (!state.isLoaded) {
                    TopAppBar(title = {},
                        elevation = 0.dp,
                        backgroundColor = MaterialTheme.colors.background,
                        navigationIcon = {
                            IconButton(onClick = { backStack.goBack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "ArrowBack",
                                    tint = MaterialTheme.colors.onBackground,
                                )
                            }
                        })
                }
            },
            bottomBar = {
                if (!state.isReaderModeEnable && state.isLoaded) {
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
                                viewModel.state.value.brightness,
                                {

                                    viewModel.onEvent(ReaderEvent.ChangeBrightness(it))

                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                valueRange = MIN_BRIGHTNESS..MAX_BRIGHTNESS,
                            )
                            FontSizeChangerComposable(
                                onFontDecease = {
                                    viewModel.onEvent(ReaderEvent.ChangeFontSize(FontSizeEvent.Decrease))
                                },
                                ontFontIncrease = {
                                    viewModel.onEvent(ReaderEvent.ChangeFontSize(FontSizeEvent.Increase))
                                },
                                fontSize = viewModel.state.value.fontSize
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FontMenuComposable(
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
                    .clickable { viewModel.onEvent(ReaderEvent.ToggleReaderMode(!state.isReaderModeEnable)) }
                    .padding(16.dp)
            ) {
                if (state.chapter.isChapterNotEmpty()) {
                    Text(
                        text = state.chapter.content.joinToString("\n\n") ?: "",
                        fontSize = viewModel.state.value.fontSize.sp,
                        fontFamily = viewModel.state.value.font.fontFamily
                    )
                }
            }
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

        if (viewModel.state.value.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colors.primary
            )
        }

    }
}


