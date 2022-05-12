package org.ireader.core_ui.utils

import androidx.compose.foundation.lazy.LazyListState

fun LazyListState.isScrolledToEnd() = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1
