package org.ireader.core.utils

fun <T> merge(first: List<T>, second: List<T>): List<T> {
    return object : ArrayList<T>() {
        init {
            addAll(first)
            addAll(second)
        }
    }
}


/**
 * Returns first index of [element], or -1 if the collection does not contain element.
 */
fun <T> Iterable<T>.indexOf(element: T): Int? {
    if (this is List) return this.indexOf(element)
    var index = 0
    for (item in this) {
        if (index < 0) return null
        if (element == item)
            return index
        index++
    }
    return null
}
