package org.ireader.presentation.feature_detail.presentation.book_detail.viewmodel

sealed class BookDetailEvent {
    object ToggleSummary : BookDetailEvent()
}