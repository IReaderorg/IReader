package org.ireader.presentation.feature_detail.presentation.chapter_detail

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}
