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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import ireader.presentation.core.components.IReaderErrorScreen
import ireader.presentation.core.components.IReaderLoadingScreen
import ireader.presentation.ui.book.viewmodel.BookDetailScreenModelNew
import org.koin.core.parameter.parametersOf

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
        val screenModel = getScreenModel<BookDetailScreenModelNew> { parametersOf(bookId) }
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(state.book?.title ?: "Loading...") },
                    actions = {
                        IconButton(onClick = { screenModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            },
            floatingActionButton = {
                state.book?.let { book ->
                    ExtendedFloatingActionButton(
                        onClick = { screenModel.toggleBookFavorite() },
                        icon = {
                            Icon(
                                if (book.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (book.favorite) "Remove from Library" else "Add to Library"
                            )
                        },
                        text = {
                            Text(if (book.favorite) "Remove from Library" else "Add to Library")
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
                    state.isLoading -> {
                        IReaderLoadingScreen()
                    }
                    
                    state.error != null -> {
                        IReaderErrorScreen(
                            message = state.error!!,
                            onRetry = { screenModel.retry() },
                            onDismiss = { screenModel.clearError() }
                        )
                    }
                    
                    state.book != null -> {
                        BookDetailContent(
                            book = state.book!!,
                            chapters = state.chapters,
                            isRefreshing = state.isRefreshing,
                            isTogglingFavorite = state.isTogglingFavorite
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
    Column {
        // Book information
        Text(
            text = "Title: ${book.title}",
            modifier = Modifier.padding(16.dp)
        )
        
        if (book.author.isNotBlank()) {
            Text(
                text = "Author: ${book.author}",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        if (book.description.isNotBlank()) {
            Text(
                text = "Description: ${book.description}",
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Chapter count
        Text(
            text = "Chapters: ${chapters.size}",
            modifier = Modifier.padding(16.dp)
        )
        
        // Status indicators
        if (isRefreshing) {
            Text(
                text = "Refreshing...",
                modifier = Modifier.padding(16.dp)
            )
        }
        
        if (isTogglingFavorite) {
            Text(
                text = "Updating favorite status...",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}