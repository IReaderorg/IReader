package org.ireader.core.utils

/**
 * Remove repeated items from list.
 */
fun <T, K> removeSameItemsFromList(
    oldList: List<T>,
    newList: List<T>,
    differentiateBy: (T) -> K,
): List<T> {
    val sum: List<T> = oldList + newList

    val uniqueList = sum.distinctBy {
        differentiateBy(it)
    }


    return uniqueList
}