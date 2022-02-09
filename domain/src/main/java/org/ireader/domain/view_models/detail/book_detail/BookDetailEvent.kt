package org.ireader.domain.view_models.detail.book_detail

sealed class BookDetailEvent {
    object ToggleSummary : BookDetailEvent()
}