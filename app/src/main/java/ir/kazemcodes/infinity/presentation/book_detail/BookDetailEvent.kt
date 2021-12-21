package ir.kazemcodes.infinity.presentation.book_detail

sealed class BookDetailEvent {
    object ToggleInLibrary : BookDetailEvent()
}