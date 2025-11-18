package ireader.presentation.ui.component.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Book
import ireader.presentation.ui.component.enhanced.AccessibleBookListItem
import ireader.presentation.ui.component.list.IReaderFastScrollLazyColumn
import ireader.presentation.ui.component.list.performantItems
import ireader.core.log.IReaderLog
import ireader.core.performance.PerformanceMonitor

/**
 * Performance-optimized book list with Mihon's patterns
 * Features:
 * - Proper key and contentType parameters for optimal performance
 * - Performance monitoring and logging
 * - Accessibility support
 * - Memory-efficient rendering
 */
@Composable
fun PerformantBookList(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    showAuthor: Boolean = true,
    showDescription: Boolean = false,
    enablePerformanceMonitoring: Boolean = true,
) {
    // Performance monitoring
    if (enablePerformanceMonitoring) {
        LaunchedEffect(books.size) {
            IReaderLog.debug("Rendering book list with ${books.size} items")
            
            if (books.size > 1000) {
                IReaderLog.warn("Large book list detected: ${books.size} items may affect performance")
            }
        }
    }
    
    IReaderFastScrollLazyColumn(
        modifier = modifier.fillMaxSize(),
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        enablePerformanceMonitoring = enablePerformanceMonitoring
    ) {
        performantItems(
            items = books,
            key = { book -> book.id },
            contentType = { "book_item" }
        ) { book ->
            AccessibleBookListItem(
                book = book,
                onClick = { onBookClick(book) },
                showAuthor = showAuthor,
                showDescription = showDescription,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Performance-optimized grid book list
 */
@Composable
fun PerformantBookGrid(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    enablePerformanceMonitoring: Boolean = true,
) {
    // Group books into rows for grid layout
    val bookRows = remember(books, columns) {
        books.chunked(columns)
    }
    
    if (enablePerformanceMonitoring) {
        LaunchedEffect(books.size, columns) {
            IReaderLog.debug("Rendering book grid: ${books.size} items in ${bookRows.size} rows ($columns columns)")
        }
    }
    
    IReaderFastScrollLazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        enablePerformanceMonitoring = enablePerformanceMonitoring
    ) {
        performantItems(
            items = bookRows,
            key = { row -> row.firstOrNull()?.id ?: 0 },
            contentType = { "book_row" }
        ) { bookRow ->
            BookGridRow(
                books = bookRow,
                onBookClick = onBookClick,
                columns = columns
            )
        }
    }
}

/**
 * Single row in book grid
 */
@Composable
private fun BookGridRow(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    columns: Int,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        books.forEach { book ->
            AccessibleBookListItem(
                book = book,
                onClick = { onBookClick(book) },
                showAuthor = false,
                showDescription = false,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Fill remaining columns with empty space
        repeat(columns - books.size) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Performance metrics for book lists
 */
object BookListPerformance {
    
    /**
     * Benchmark book list rendering performance
     */
    fun benchmarkBookListRendering(
        bookCount: Int,
        iterations: Int = 10
    ) {
        PerformanceMonitor.measureUIOperation("BookList-Rendering-$bookCount") {
            // This would be called during actual rendering
            IReaderLog.benchmark(
                "Book list rendering benchmark",
                mapOf(
                    "bookCount" to bookCount,
                    "iterations" to iterations
                )
            )
        }
    }
    
    /**
     * Monitor scroll performance
     */
    fun monitorScrollPerformance(
        firstVisibleItem: Int,
        scrollOffset: Int,
        totalItems: Int
    ) {
        val scrollPercentage = if (totalItems > 0) {
            (firstVisibleItem.toFloat() / totalItems * 100).toInt()
        } else 0
        
        IReaderLog.debug(
            "Book list scroll: item $firstVisibleItem/$totalItems (${scrollPercentage}%), offset: $scrollOffset"
        )
    }
}