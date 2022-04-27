package org.ireader.presentation.feature_sources.presentation.global_search

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.common_models.entities.BaseBook
import org.ireader.presentation.feature_detail.presentation.book_detail.components.DotsFlashing
import org.ireader.presentation.feature_sources.presentation.global_search.viewmodel.GlobalSearchState
import org.ireader.presentation.feature_sources.presentation.global_search.viewmodel.SearchItem
import org.ireader.core_ui.ui_components.layouts.BookImage
import org.ireader.core_ui.ui_components.reusable_composable.AppIconButton
import org.ireader.core_ui.ui_components.reusable_composable.MidSizeTextComposable
import org.ireader.core_ui.ui_components.reusable_composable.SmallTextComposable

@Composable
fun GlobalSearchScreen(
    vm: GlobalSearchState,
    onPopBackStack: () -> Unit,
    onSearch: (query: String) -> Unit,
    onBook: (BaseBook) -> Unit,
    onGoToExplore: (Int) -> Unit,

    ) {

    val uiSearch = vm.searchItems.filter { it.items.isNotEmpty() }
    val emptySearches = vm.searchItems.filter { it.items.isEmpty() }
    val allSearch = uiSearch + emptySearches
    val scrollState = rememberLazyListState()
    Scaffold(
        topBar = {
            GlobalScreenTopBar(
                onPop = onPopBackStack,
                onSearch = onSearch,
                state = vm
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            /**
             * TODO need to wait for this issue to be close before using lazycolumn
             * https://issuetracker.google.com/issues/229752147
             */
            allSearch.forEachIndexed { index, _ ->
                GlobalSearchBookInfo(
                    allSearch[index],
                    onBook = onBook,
                    goToExplore = { onGoToExplore(index) }
                )
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun GlobalSearchBookInfo(
    book: SearchItem,
    onBook: (BaseBook) -> Unit,
    goToExplore: () -> Unit,
) {
    val modifier = when (book.items.isNotEmpty()) {
        true -> Modifier
            .fillMaxWidth()
            .animateContentSize()
        else -> Modifier
    }
    Column(modifier = modifier) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {

            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start) {
                MidSizeTextComposable(text = book.source.name, fontWeight = FontWeight.Bold)
                SmallTextComposable(text = book.source.lang.uppercase())
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                DotsFlashing(book.loading)
                AppIconButton(imageVector = Icons.Default.ArrowForward,
                    title = "open explore",
                    onClick = goToExplore)
            }

        }
        Spacer(modifier = Modifier.height(20.dp))
        LazyRow {
            items(book.items.size) { index ->
                BookImage(
                    modifier = Modifier
                        .height(250.dp)
                        .aspectRatio(3f / 4f),
                    onClick = {
                        onBook(it)
                    },
                    book = book.items[index]) {
                }
            }
        }
    }


}