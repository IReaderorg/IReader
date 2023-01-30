package ireader.presentation.ui.home.sources.global_search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Book
import ireader.core.source.Source
import ireader.presentation.R
import ireader.presentation.ui.component.list.layouts.BookImage
import ireader.presentation.ui.component.loading.DotsFlashing
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.SmallTextComposable
import ireader.presentation.ui.home.sources.global_search.viewmodel.GlobalSearchViewModel
import ireader.presentation.ui.home.sources.global_search.viewmodel.SearchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    vm: GlobalSearchViewModel,
    onPopBackStack: () -> Unit,
    onSearch: (query: String) -> Unit,
    onBook: (Book) -> Unit,
    onGoToExplore: (SearchItem) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?

) {


    Scaffold(
        topBar = {
            GlobalScreenTopBar(
                onPop = onPopBackStack,
                onSearch = onSearch,
                state = vm,
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(
                items = vm.withResult,
                key = { item ->
                    item.source.key(Types.WithResult)
                },
            ) { item ->
                GlobalSearchBookInfo(
                    item,
                    onBook = onBook,
                    goToExplore = { onGoToExplore(item) },
                    false
                )
            }
            items(items = vm.inProgress,
                key = { item ->
                    item.source.key(Types.InProgress)
                },) { item ->
                GlobalSearchBookInfo(
                    item,
                    onBook = onBook,
                    goToExplore = { onGoToExplore(item) },
                    true
                )
            }
            items(items = vm.noResult,
                key = { item ->
                    item.source.key(Types.NoResult)
                },) { item ->
                GlobalSearchBookInfo(
                    item,
                    onBook = onBook,
                    goToExplore = { onGoToExplore(item) },
                    false
                )
            }
        }
    }
//    }
}

private enum class Types {
    WithResult,
    NoResult,
    InProgress;
}


private fun Source.key(type:Types) :String {
   return when(type) {
        Types.InProgress -> "in_progress-${this.id}"
        Types.NoResult -> "no_result-${this.id}"
        Types.WithResult -> "with_result-${this.id}"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlobalSearchBookInfo(
    book: SearchItem,
    onBook: (Book) -> Unit,
    goToExplore: () -> Unit,
    loading:Boolean
) {
    val modifier = when (book.items.isNotEmpty()) {
        true ->
            Modifier
                .fillMaxWidth()

        else -> Modifier
    }
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                MidSizeTextComposable(text = book.source.name, fontWeight = FontWeight.Bold)
                SmallTextComposable(text = book.source.lang.uppercase())
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                DotsFlashing(loading)
                AppIconButton(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = stringResource(R.string.open_explore),
                    onClick = goToExplore
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        LazyRow(modifier = Modifier) {
            items(book.items.size) { index ->
                BookImage(
                    modifier = Modifier
                        .height(250.dp)
                        .aspectRatio(3f / 4f),
                    onClick = {
                        onBook(book.items[index])
                    },
                    book = book.items[index]
                ) {
                }
            }
        }
    }
}
