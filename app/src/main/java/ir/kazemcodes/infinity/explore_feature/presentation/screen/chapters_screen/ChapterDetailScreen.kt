package ir.kazemcodes.infinity.explore_feature.presentation.screen.chapters_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.api_feature.HttpSource
import ir.kazemcodes.infinity.base_feature.navigation.ReadingContentKey
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import timber.log.Timber


@Composable
fun ChapterDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: ChapterDetailViewModel = hiltViewModel(),
    chapters : List<Chapter>,
    book:Book,
    api:HttpSource
) {
    val backStack = LocalBackstack.current
    Box(modifier = modifier.fillMaxSize()) {
        Timber.d("TAG Chapters: " + chapters)
        if (chapters.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(count = chapters.size) { index ->
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .height(40.dp)
                            .clickable {
                                backStack.goTo(ReadingContentKey(book = book, chapter = chapters[index], api = api))
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = chapters[index].title,
                            color = MaterialTheme.colors.onBackground
                        )
                        Text(
                            text = if (chapters[index].content != null) "Cached" else "",
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colors.onBackground
                        )
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

