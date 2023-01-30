package ireader.presentation.ui.home.explore

import ireader.core.source.model.Filter

fun <T> List<Filter<T>>.update(position: Int, newItem: T): List<Filter<T>> {
    val newList = toMutableList()
    newList[position].value = newItem
    return newList
}
