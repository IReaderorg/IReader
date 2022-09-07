package ireader.ui.chapter.viewmodel

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}
