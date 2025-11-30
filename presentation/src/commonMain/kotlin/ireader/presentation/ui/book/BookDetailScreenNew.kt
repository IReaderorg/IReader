package ireader.presentation.ui.book

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import ireader.presentation.core.ui.IReaderLoadingScreen
import ireader.presentation.core.ui.getIViewModel
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import ireader.presentation.ui.core.ui.EmptyScreen
import kotlinx.coroutines.launch
import org.koin.core.parameter.parametersOf
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Example implementation of BookDetailScreen using the new StateScreenModel pattern.
 * This demonstrates how to use the new architecture following Mihon's patterns.
 */
data class BookDetailScreenNew(
    private val bookId: Long
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val vm: BookDetailViewModel = getIViewModel(parameters = { parametersOf(BookDetailViewModel.Param(bookId)) })
        val book = vm.booksState.book

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(book?.title ?: "Loading...") },
                    actions = {
                        IconButton(onClick = { 
                            book?.let { vm.scope.launch { vm.getRemoteChapterDetail(it, vm.catalogSource) } }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = localizeHelper.localize(Res.string.refresh))
                        }
                    }
                )
            },
            floatingActionButton = {
                book?.let { currentBook ->
                    ExtendedFloatingActionButton(
                        onClick = { vm.toggleInLibrary(currentBook) },
                        icon = {
                            Icon(
                                if (currentBook.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (currentBook.favorite) "Remove from Library" else "Add to Library"
                            )
                        },
                        text = {
                            Text(if (currentBook.favorite) "Remove from Library" else "Add to Library")
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    vm.booksState.book == null && !vm.detailIsLoading -> {
                        EmptyScreen(
                            text = localizeHelper.localize(Res.string.book_not_found)
                        )
                    }
                    
                    vm.booksState.book == null -> {
                        IReaderLoadingScreen()
                    }
                    
                    book != null -> {
                        BookDetailContent(
                            book = book,
                            chapters = vm.chapters,
                            isRefreshing = vm.detailIsLoading,
                            isTogglingFavorite = vm.inLibraryLoading
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookDetailContent(
    book: ireader.domain.models.entities.Book,
    chapters: List<ireader.domain.models.entities.Chapter>,
    isRefreshing: Boolean,
    isTogglingFavorite: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column {
        // Book information
        Text(
            text = "Title: ${book.title}",
            modifier = Modifier.padding(1.dp)
        )
        
        if (book.author.isNotBlank()) {
            Text(
                text = "Author: ${book.author}",
                modifier = Modifier.padding(horizontal = 1.dp)
            )
        }
        
        if (book.description.isNotBlank()) {
            Text(
                text = "Description: ${book.description}",
                modifier = Modifier.padding(1.dp)
            )
        }
        
        // Chapter count
        Text(
            text = "Chapters: ${chapters.size}",
            modifier = Modifier.padding(1.dp)
        )
        
        // Status indicators
        if (isRefreshing) {
            Text(
                text = localizeHelper.localize(Res.string.refreshing_1),
                modifier = Modifier.padding(1.dp)
            )
        }
        
        if (isTogglingFavorite) {
            Text(
                text = localizeHelper.localize(Res.string.updating_favorite_status),
                modifier = Modifier.padding(1.dp)
            )
        }
    }
}