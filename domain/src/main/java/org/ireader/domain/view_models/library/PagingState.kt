package org.ireader.domain.view_models.library

data class PagingState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val endReached: Boolean = false,
)