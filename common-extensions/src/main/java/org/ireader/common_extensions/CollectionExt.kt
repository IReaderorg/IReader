package org.ireader.common_extensions

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

fun <T> merge(first: List<T>, second: List<T>): List<T> {
    return object : ArrayList<T>() {
        init {
            addAll(first)
            addAll(second)
        }
    }
}

@Throws(IndexIsInvalidException::class,
    IndexLessThanZeroException::class,
    IndexGreaterThanCollectionSizeException::class)
inline fun <T, K> List<T>.next(mapBy: (T) -> K, currentItem: K): Pair<Int, T> {
    val items = this.map(mapBy)

    val index = items.indexOf(currentItem)
    return when {
        index == null -> {
            throw NullPointerException("index is $index")
        }
        index == -1 -> {
            throw IndexIsInvalidException("index is $index")
        }
        index < 0 -> {
            throw IndexLessThanZeroException("index is $index")
        }
        index > this.lastIndex -> {
            throw IndexGreaterThanCollectionSizeException("index is $index")
        }
        else -> {
            Pair(index + 1, this[index + 1])
        }
    }
}

class IndexLessThanZeroException(override val message: String? = null) : Exception(message)

class IndexGreaterThanCollectionSizeException(override val message: String? = null) :
    Exception(message)

class IndexIsInvalidException(override val message: String? = null) : Exception(message)

@Throws(IndexIsInvalidException::class,
    IndexLessThanZeroException::class,
    IndexGreaterThanCollectionSizeException::class)
inline fun <T, K> List<T>.previous(item: K, mapBy: (T) -> K): Pair<Int, T> {
    val items = this.map(mapBy)
    val index = items.indexOf(item)
    return when {
        index == -1 -> {
            throw IndexIsInvalidException("index is $index")
        }
        index < 0 -> {
            throw IndexLessThanZeroException("index is $index")
        }
        index > this.lastIndex -> {
            throw IndexGreaterThanCollectionSizeException("index is $index")
        }
        else -> {
            Pair(index - 1, this[index - 1])
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