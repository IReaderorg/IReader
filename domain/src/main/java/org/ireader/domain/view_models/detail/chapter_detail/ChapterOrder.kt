package org.ireader.domain.view_models.detail.chapter_detail

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}
