package org.ireader.core.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems

@ExperimentalFoundationApi
fun <T : Any> LazyGridScope.items(
    lazyPagingItems: LazyPagingItems<T>,
    itemContent: @Composable LazyGridScope.(value: T?) -> Unit,
) {
    items(lazyPagingItems.itemCount) { index ->
        itemContent(lazyPagingItems[index])
    }
}