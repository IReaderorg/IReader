package org.ireader.presentation.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.presentation.presentation.reusable_composable.ErrorTextWithEmojis

@Composable
fun handlePagingResult(
    modifier: Modifier = Modifier,
    books: LazyPagingItems<Book>,
    onEmptyResult: @Composable () -> Unit,
    onErrorResult: @Composable (error: String) -> Unit = { error ->
        Box(modifier = modifier) {
            ErrorTextWithEmojis(error = error.toString(), modifier = modifier
                .fillMaxWidth()
                .padding(20.dp)
                .wrapContentSize(Alignment.Center)
                .align(Alignment.Center))
        }
    },
): Boolean {
    val context = LocalContext.current
    books.apply {
        val error = when {
            loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
            loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
            loadState.append is LoadState.Error -> loadState.append as LoadState.Error
            else -> null
        }

        return when {
            loadState.refresh is LoadState.Loading -> {
                showLoading()
                false
            }
            error != null && books.itemCount < 1 -> {
                onErrorResult(UiText.ExceptionString(error.error).asString(context = context))
                false
            }
            books.itemCount < 1 -> {
                onEmptyResult()
                false
            }
            else -> true
        }
    }
}

@Composable
fun handlePagingChapterResult(
    modifier: Modifier = Modifier,
    books: LazyPagingItems<Chapter>,
    onEmptyResult: @Composable () -> Unit,
    onErrorResult: @Composable (error: String) -> Unit = { error ->
        Box(modifier = modifier) {
            ErrorTextWithEmojis(error = error, modifier = modifier
                .fillMaxWidth()
                .padding(20.dp)
                .wrapContentSize(Alignment.Center)
                .align(Alignment.Center))
        }
    },
): Boolean {
    val context = LocalContext.current
    books.apply {
        val error = when {
            loadState.refresh is LoadState.Error -> loadState.refresh as LoadState.Error
            loadState.prepend is LoadState.Error -> loadState.prepend as LoadState.Error
            loadState.append is LoadState.Error -> loadState.append as LoadState.Error
            else -> null
        }

        return when {
            loadState.refresh is LoadState.Loading -> {
                showLoading()
                false
            }
            error != null -> {
                onErrorResult(UiText.ExceptionString(error.error).asString(context = context))
                false
            }
            books.itemCount < 1 -> {
                onEmptyResult()
                false
            }
            else -> true
        }
    }
}