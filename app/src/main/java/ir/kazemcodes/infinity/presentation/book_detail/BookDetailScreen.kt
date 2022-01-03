package ir.kazemcodes.infinity.presentation.book_detail


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.base_feature.navigation.ChapterDetailKey
import ir.kazemcodes.infinity.base_feature.navigation.ReaderScreenKey
import ir.kazemcodes.infinity.base_feature.navigation.WebViewKey
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.presentation.book_detail.components.ButtonWithIconAndText
import ir.kazemcodes.infinity.presentation.book_detail.components.CardTileComposable
import ir.kazemcodes.infinity.presentation.book_detail.components.DotsFlashing
import ir.kazemcodes.infinity.presentation.book_detail.components.ExpandingText
import ir.kazemcodes.infinity.presentation.screen.components.BookImageComposable
import ir.kazemcodes.infinity.util.getUrlWithoutDomain


@Composable
fun BookDetailScreen(
    modifier: Modifier = Modifier,
    book: Book = Book.create(),
    viewModel: BookDetailViewModel,
) {
    val detailState = viewModel.state.value
    val chapterState = viewModel.chapterState.value
    val backStack = LocalBackstack.current



    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.state.value.loaded) {
            BookDetailScreenLoadedComposable(
                modifier = modifier,
                viewModel = viewModel
            )

        } else {
            Scaffold(modifier = Modifier.fillMaxSize(),topBar = {
                TopAppBar(title = {},
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
            }) {
                Box(modifier = Modifier.fillMaxSize()) {
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
        }


    }


}


@Composable
fun BookDetailScreenLoadedComposable(
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel,
) {
    val source = viewModel.getSource()
    val backStack = LocalBackstack.current
    val inLibrary = viewModel.state.value.inLibrary
    val book = viewModel.state.value.book
    val chapters = viewModel.chapterState.value.chapters
    val context = LocalContext.current
    Scaffold(topBar = {
        TopAppBar(
            title = {},
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.onBackground,
            elevation = 0.dp,
            actions = {
                /** ERROR: This may cause error later: mismatch between baseurl and book link**/
                IconButton(onClick = { backStack.goTo(WebViewKey(source.baseUrl + getUrlWithoutDomain(book.link))) }) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "WebView",
                        tint = MaterialTheme.colors.onBackground,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { backStack.goBack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "back Button",
                        tint = MaterialTheme.colors.onBackground
                    )
                }

            }
        )
    }, bottomBar = {
        BottomAppBar(
            modifier = modifier.fillMaxWidth(),
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.onBackground,
            elevation = 8.dp,
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically

            ) {
                ButtonWithIconAndText(
                    text = if (!inLibrary) "Add to Library" else "Added To Library",
                    imageVector = if (!inLibrary) Icons.Default.AddCircleOutline else Icons.Default.Check,
                    onClick = {
                        if (!inLibrary) {
                            viewModel.insertBookDetailToLocal(
                                book.copy(inLibrary = true, source = source.name).toBookEntity()
                            )
                            val chapterEntities = chapters.map {
                                it.copy(bookName = book.bookName, source = source.name)
                                    .toChapterEntity()
                            }
                            viewModel.insertChaptersToLocal(chapterEntities)
                            viewModel.onEvent(BookDetailEvent.ToggleInLibrary)
                        } else {
                            viewModel.deleteLocalBook(book.bookName)
                            viewModel.deleteLocalChapters(book.bookName)
                            viewModel.onEvent(BookDetailEvent.ToggleInLibrary)
                        }
                    },
                )
                ButtonWithIconAndText(
                    text = if (viewModel.chapterState.value.lastChapter != viewModel.chapterState.value.chapters.getOrNull(
                            0)
                    ) "Continue Reading" else "Read",
                    imageVector = Icons.Default.AutoStories,
                    onClick = {
                        if (viewModel.chapterState.value.lastChapter != null) {
                            backStack.goTo(ReaderScreenKey(chapter = viewModel.chapterState.value.lastChapter!!,
                                book = book,
                                sourceName = viewModel.getSource().name,
                                chapters = viewModel.chapterState.value.chapters))
                        } else if (viewModel.chapterState.value.chapters.isNotEmpty()) {
                            backStack.goTo(ReaderScreenKey(chapter = viewModel.chapterState.value.chapters.first(),
                                book = book,
                                sourceName = viewModel.getSource().name,
                                chapters = viewModel.chapterState.value.chapters))
                        }
                    }
                )
                ButtonWithIconAndText(
                    text = "Download",
                    imageVector = Icons.Default.FileDownload,
                    onClick = {
                        //context.toast("Not Supported",)
                        viewModel.startDownloadService(context)
                    }
                )
            }
        }
    }) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            /** Image and Book Information **/
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                /** Book Image **/
                BookImageComposable(
                    image = book.coverLink ?: "",
                    modifier = modifier
                        .width(120.dp)
                        .height(180.dp)
                        .shadow(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = modifier.width(8.dp))
                /** Book Info **/
                Column {
                    Text(
                        text = book.bookName,
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Author: ${book.author}",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Source: ${book.source}",
                        color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.subtitle2,
                    )
                    Text(
                        text = "Genre: ${book.category}",
                        color = MaterialTheme.colors.onBackground.copy(alpha = .5f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.subtitle2,
                    )

                }


            }
            Divider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            /** Book Summary **/
            Text(
                text = "Synopsis", fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.h6
            )
            ExpandingText(text = book.description.joinToString("\n\n") ?: "Unknown")
            Divider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            /** Chapter Content **/
            CardTileComposable(
                modifier = modifier.clickable {
                    backStack.goTo(ChapterDetailKey(chapters = chapters,
                        book = book,
                        sourceName = viewModel.getSource().name))
                },
                title = "Contents",
                subtitle = "${chapters.size} Chapters",
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "", color = MaterialTheme.colors.onBackground,
                            style = MaterialTheme.typography.subtitle2
                        )
                        if (viewModel.chapterState.value.isLoading) {
                            DotsFlashing()
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Contents Detail",
                            tint = MaterialTheme.colors.onBackground,
                        )
                    }
                })
            Spacer(modifier = modifier.height(60.dp))
        }
    }
}