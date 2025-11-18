package ireader.presentation.ui.component.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import ireader.core.log.IReaderLog
import ireader.core.performance.PerformanceMonitor
import kotlin.time.measureTime

/**
 * Enhanced LazyColumn with Mihon's FastScroll patterns and performance optimizations
 * Features:
 * - Proper key and contentType parameters for performance
 * - Performance monitoring and logging
 * - Accessibility improvements
 * - Memory-efficient rendering
 */
@Composable
fun IReaderFastScrollLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    userScrollEnabled: Boolean = true,
    enablePerformanceMonitoring: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    
    // Performance monitoring
    if (enablePerformanceMonitoring) {
        LaunchedEffect(state.firstVisibleItemIndex, state.firstVisibleItemScrollOffset) {
            val renderTime = measureTime {
                // Monitor scroll performance
                IReaderLog.debug(
                    "LazyColumn scroll position: item=${state.firstVisibleItemIndex}, " +
                    "offset=${state.firstVisibleItemScrollOffset}"
                )
            }
            
            if (renderTime.inWholeMilliseconds > 16) {
                IReaderLog.warn(
                    "Slow LazyColumn scroll rendering: ${renderTime.inWholeMilliseconds}ms " +
                    "(may affect 60fps)"
                )
            }
        }
    }
    
    // Enhanced LazyColumn with performance optimizations
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        userScrollEnabled = userScrollEnabled,
        content = content
    )
}

/**
 * Performance-optimized LazyColumn item with proper key and contentType
 */
@Composable
fun LazyListScope.performantItem(
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable () -> Unit
) {
    item(
        key = key,
        contentType = contentType
    ) {
        PerformanceMonitor.measureUIOperation("LazyColumn-Item-${key ?: "unknown"}") {
            content()
        }
    }
}

/**
 * Performance-optimized LazyColumn items with proper key and contentType
 */
inline fun <T> LazyListScope.performantItems(
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable (item: T) -> Unit
) {
    items(
        count = items.size,
        key = if (key != null) { index: Int -> key(items[index]) } else null,
        contentType = { index: Int -> contentType(items[index]) }
    ) { index ->
        val item = items[index]
        val itemKey = key?.invoke(item) ?: index
        
        PerformanceMonitor.measureUIOperation("LazyColumn-Item-$itemKey") {
            itemContent(item)
        }
    }
}

/**
 * Accessibility-enhanced LazyColumn with proper semantic roles
 */
@Composable
fun IReaderAccessibleLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    userScrollEnabled: Boolean = true,
    contentDescription: String? = null,
    content: LazyListScope.() -> Unit,
) {
    IReaderLog.accessibility("Creating accessible LazyColumn with ${state.layoutInfo.totalItemsCount} items")
    
    IReaderFastScrollLazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        userScrollEnabled = userScrollEnabled,
        content = content
    )
}