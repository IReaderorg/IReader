package ireader.presentation.core.util

import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Collection utilities optimized for Compose, following Mihon's patterns.
 * 
 * These functions use Compose's fast* variants which are optimized for
 * random-access lists and avoid iterator allocation.
 * 
 * IMPORTANT: Only use for collections created by our code that support
 * random access. Do not use for collections from external APIs.
 */

/**
 * Returns a list containing all elements not matching the given [predicate].
 * Uses Compose's fastFilter internally for better performance.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastFilterNot(predicate: (T) -> Boolean): List<T> {
    contract { callsInPlace(predicate) }
    return fastFilter { !predicate(it) }
}

/**
 * Splits the list into pair of lists where first contains elements
 * matching predicate and second contains elements not matching.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastPartition(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    contract { callsInPlace(predicate) }
    val first = ArrayList<T>()
    val second = ArrayList<T>()
    fastForEach {
        if (predicate(it)) {
            first.add(it)
        } else {
            second.add(it)
        }
    }
    return Pair(first, second)
}

/**
 * Returns the number of entries not matching the given [predicate].
 */
@OptIn(ExperimentalContracts::class)
inline fun <T> List<T>.fastCountNot(predicate: (T) -> Boolean): Int {
    contract { callsInPlace(predicate) }
    var count = size
    fastForEach { if (predicate(it)) --count }
    return count
}

/**
 * Inserts separators between elements based on generator function.
 * Useful for adding date headers between list items.
 */
fun <T : R, R : Any> List<T>.insertSeparators(
    generator: (before: T?, after: T?) -> R?,
): List<R> {
    if (isEmpty()) return emptyList()
    val newList = mutableListOf<R>()
    for (i in -1..lastIndex) {
        val before = getOrNull(i)
        before?.let(newList::add)
        val after = getOrNull(i + 1)
        val separator = generator.invoke(before, after)
        separator?.let(newList::add)
    }
    return newList
}

/**
 * Similar to insertSeparators but iterates from last to first element.
 */
fun <T : R, R : Any> List<T>.insertSeparatorsReversed(
    generator: (before: T?, after: T?) -> R?,
): List<R> {
    if (isEmpty()) return emptyList()
    val newList = mutableListOf<R>()
    for (i in size downTo 0) {
        val after = getOrNull(i)
        after?.let(newList::add)
        val before = getOrNull(i - 1)
        val separator = generator.invoke(before, after)
        separator?.let(newList::add)
    }
    return newList.asReversed()
}

/**
 * Adds or removes value from HashSet based on condition.
 */
fun <E> HashSet<E>.addOrRemove(value: E, shouldAdd: Boolean) {
    if (shouldAdd) {
        add(value)
    } else {
        remove(value)
    }
}
