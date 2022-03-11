package org.ireader.core.utils

/**
 * Returns a new list that replaces the item at the given [position] with [newItem].
 */
fun <T> List<T>.replace(position: Int, newItem: T): List<T> {
    val newList = toMutableList()
    newList[position] = newItem
    return newList
}

fun <T> List<T>.replaceAll(newItems: List<T>): List<T> {
    var newList = toMutableList()
    newList = newItems.toMutableList()
    return newList
}

/**
 * Returns a new list that replaces the first occurrence that matches the given [predicate] with
 * [newItem]. If no item matches the predicate, the same list is returned (and unmodified).
 */
inline fun <T> List<T>.replaceFirst(predicate: (T) -> Boolean, newItem: T): List<T> {
    forEachIndexed { index, element ->
        if (predicate(element)) {
            return replace(index, newItem)
        }
    }
    return this
}

/**
 * Removes the first item of this collection that matches the given [predicate].
 */
inline fun <T> MutableCollection<T>.removeFirst(predicate: (T) -> Boolean): T? {
    val iter = iterator()
    while (iter.hasNext()) {
        val element = iter.next()
        if (predicate(element)) {
            iter.remove()
            return element
        }
    }
    return null
}
