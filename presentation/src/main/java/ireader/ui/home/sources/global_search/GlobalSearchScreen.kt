package ireader.ui.home.sources.global_search

import androidx.compose.animation.animateContentSize
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
import ireader.common.models.entities.BaseBook
import ireader.common.models.entities.Book
import ireader.ui.component.list.layouts.BookImage
import ireader.ui.component.reusable_composable.AppIconButton
import ireader.ui.component.reusable_composable.MidSizeTextComposable
import ireader.ui.component.reusable_composable.SmallTextComposable
import ireader.ui.component.loading.DotsFlashing
import ireader.ui.home.sources.global_search.viewmodel.GlobalSearchState
import ireader.ui.home.sources.global_search.viewmodel.SearchItem
import ireader.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    vm: GlobalSearchState,
    onPopBackStack: () -> Unit,
    onSearch: (query: String) -> Unit,
    onBook: (Book) -> Unit,
    onGoToExplore: (Int) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?

) {

    val uiSearch = vm.searchItems.filter { it.items.isNotEmpty() }
    val emptySearches = vm.searchItems.filter { it.items.isEmpty() }
    val allSearch = uiSearch + emptySearches

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
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .verticalScroll(rememberScrollState()),
//        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(count = allSearch.size) { index ->
                GlobalSearchBookInfo(
                    allSearch[index],
                    onBook = onBook,
                    goToExplore = { onGoToExplore(index) }
                )
            }
        }
    }
//    }
}

@Composable
fun GlobalSearchBookInfo(
    book: SearchItem,
    onBook: (Book) -> Unit,
    goToExplore: () -> Unit,
) {
    val modifier = when (book.items.isNotEmpty()) {
        true ->
            Modifier
                .fillMaxWidth()
                .animateContentSize()
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
                DotsFlashing(book.loading)
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
