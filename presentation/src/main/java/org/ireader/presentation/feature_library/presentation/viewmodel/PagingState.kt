package org.ireader.presentation.feature_library.presentation.viewmodel

import androidx.annotation.Keep


data class PagingState<T>(
    val items: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val endReached: Boolean = false,
)