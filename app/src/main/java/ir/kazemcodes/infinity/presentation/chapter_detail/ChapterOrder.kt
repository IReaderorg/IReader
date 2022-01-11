package ir.kazemcodes.infinity.presentation.chapter_detail

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}
