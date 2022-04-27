package org.ireader.library.viewmodel


data class PagingState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val endReached: Boolean = false,
)