package org.ireader.chapterDetails.viewmodel

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}
