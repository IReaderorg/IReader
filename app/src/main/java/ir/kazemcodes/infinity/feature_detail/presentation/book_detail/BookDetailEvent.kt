package ir.kazemcodes.infinity.feature_detail.presentation.book_detail

sealed class BookDetailEvent {
    object ToggleSummary : BookDetailEvent()
}