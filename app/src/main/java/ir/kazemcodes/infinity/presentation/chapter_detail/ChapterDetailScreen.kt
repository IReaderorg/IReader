package ir.kazemcodes.infinity.presentation.chapter_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.base_feature.navigation.ReaderScreenKey
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.network.models.HttpSource
import timber.log.Timber


@Composable
fun ChapterDetailScreen(
    modifier: Modifier = Modifier,
    chapters: List<Chapter>,
    book: Book,
    api: HttpSource
) {
    val viewModel = rememberService<ChapterDetailViewModel>()
    val backStack = LocalBackstack.current
    val chapterState = viewModel.state.value.chapters
    if (chapterState.isEmpty() || chapterState.size != chapters.size && chapterState.last().bookName != chapters.last().bookName) {
        viewModel.onEvent(ChapterDetailEvent.UpdateChapters(chapters = chapters))
    }
    val state = viewModel.state.value
    Timber.d("Timber: ChapterDetail Screen was Loaded Successfully with ${chapterState.size} chapters")
    Box(modifier = modifier.fillMaxSize()) {

        if (state.chapters.isNotEmpty()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = CenterVertically,
                            ) {
                                Text(
                                    text = "Content",
                                    color = MaterialTheme.colors.onBackground,
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        modifier = modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.onBackground,
                        elevation = 8.dp,
                        actions = {
                            IconButton(onClick = {
                                viewModel.onEvent(ChapterDetailEvent.ToggleOrder)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort Icon"
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { backStack.goBack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back Icon"
                                )
                            }
                        }
                    )
                }
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(count = state.chapters.size) { index ->
                        Row(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .height(40.dp)
                                .clickable {
                                    backStack.goTo(
                                        ReaderScreenKey(
                                            book = book,
                                            chapter = state.chapters[index],
                                            api = api
                                        )
                                    )
                                },
                            verticalAlignment = CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = state.chapters[index].title,
                                color = MaterialTheme.colors.onBackground,
                                style = MaterialTheme.typography.subtitle2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (state.chapters[index].content != null) "Cached" else "",
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colors.onBackground,
                                style = MaterialTheme.typography.subtitle2
                            )
                        }
                    }

                }
            }

        }

        if (viewModel.state.value.error.isNotBlank()) {
            Text(
                text = viewModel.state.value.error,
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
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

