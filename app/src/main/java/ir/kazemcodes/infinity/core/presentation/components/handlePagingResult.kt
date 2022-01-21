package ir.kazemcodes.infinity.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.presentation.reusable_composable.ErrorTextWithEmojis
import ir.kazemcodes.infinity.core.utils.UiText

@Composable
fun handlePagingResult(
    modifier: Modifier =Modifier,
    books: LazyPagingItems<Book>,
    onEmptyResult:@Composable () -> Unit,
    onErrorResult:@Composable (error:String) -> Unit = {error ->
        Box(modifier=modifier) {
            ErrorTextWithEmojis(error =error, modifier = modifier
                .fillMaxWidth()
                .padding(20.dp)
                .wrapContentSize(Alignment.Center)
                .align(Alignment.Center))
        }
    }
): Boolean {
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
                onErrorResult(error.error.localizedMessage?: UiText.unknownError())
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
