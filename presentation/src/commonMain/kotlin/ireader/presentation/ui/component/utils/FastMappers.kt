package ireader.presentation.ui.component.utils

expect inline fun <T, R> List<T>.fastMap(transform: (T) -> R): List<R>


expect inline fun <T> List<T>.fastForEach(action: (T) -> Unit)


expect inline fun <T, R : Comparable<R>> List<T>.fastMaxBy(selector: (T) -> R): T?

expect inline fun <T> List<T>.fastFirstOrNull(predicate: (T) -> Boolean): T?

expect inline fun <T> List<T>.fastAll(predicate: (T) -> Boolean): Boolean

expect inline fun <T> List<T>.fastAny(predicate: (T) -> Boolean): Boolean
fun <T> MutableList<T>.removeIf(predicate: (T) -> Boolean) {
    this.toList().fastMap {
        if (predicate(it)) {
            this.remove(it)
        }
    }
}