package org.ireader.presentation.feature_explore.presentation.browse

import org.ireader.core_api.source.model.Filter

fun <T> List<Filter<T>>.update(position: Int, newItem: T): List<Filter<T>> {
    val newList = toMutableList()
    newList[position].value = newItem
    return newList
}
