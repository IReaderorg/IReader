package org.ireader.presentation.feature_sources.presentation.global_search

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.domain.models.entities.Book
import org.ireader.domain.view_models.global_search.GlobalSearchViewModel
import org.ireader.domain.view_models.global_search.SearchItem
import org.ireader.presentation.feature_detail.presentation.book_detail.components.DotsFlashing
import org.ireader.presentation.presentation.layouts.BookImage
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.SmallTextComposable
import org.ireader.presentation.ui.BookDetailScreenSpec
import org.ireader.presentation.ui.ExploreScreenSpec
import timber.log.Timber

@Composable
fun GlobalSearchScreen(
    navController: NavController = rememberNavController(),
    vm: GlobalSearchViewModel = hiltViewModel(),
) {

    val scrollState = rememberLazyListState()
    val query = vm.query
    Scaffold(
        topBar = {
            GlobalScreenTopBar(
                onPop = {
                    navController.popBackStack()
                },
                onSearch = {
                    vm.searchBooks(query = query)
                },
                onValueChange = {
                    vm.query = it
                },
                query = query
            )
        }
    ) {
        LazyColumn(state = scrollState) {
            items(vm.searchItems.size) { index ->
                GlobalSearchBookInfo(
                    vm.searchItems[index],
                    onBook = {
                        try {
                            navController.navigate(
                                BookDetailScreenSpec.buildRoute(
                                    sourceId = it.sourceId,
                                    bookId = it.id,
                                )
                            )

                        } catch (e: Exception) {
                            Timber.e(e.localizedMessage)
                        }
                    },
                    goToExplore = {
                        try {
                            if (query.isNotBlank()) {
                                navController.navigate(
                                    ExploreScreenSpec.buildRoute(vm.searchItems[index].source.id,
                                        query = query)
                                )
                            }
                        } catch (e: Exception) {
                            Timber.e(e.localizedMessage)
                        }

                    }
                )
            }


        }


    }

}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun GlobalSearchBookInfo(
    book: SearchItem,
    onBook: (Book) -> Unit,
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
                if (book.loading) {
                    DotsFlashing()
                }
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
