package org.ireader.presentation.feature_detail.presentation.chapter_detail.viewmodel

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}
