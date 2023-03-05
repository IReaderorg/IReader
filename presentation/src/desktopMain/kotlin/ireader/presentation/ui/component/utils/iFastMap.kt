package ireader.presentation.ui.component.utils


actual inline fun <T, R> List<T>.fastMap(transform: (T) -> R): List<R> {
    return map(transform)
}

actual inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    return forEach(action)
}


actual inline fun <T, R : Comparable<R>> List<T>.fastMaxBy(selector: (T) -> R): T? {
    return maxBy(selector)
}

actual inline fun <T> List<T>.fastFirstOrNull(predicate: (T) -> Boolean): T? {
    return firstOrNull(predicate)
}

actual inline fun <T> List<T>.fastAll(predicate: (T) -> Boolean): Boolean {
    return all(predicate)
}

actual inline fun <T> List<T>.fastAny(predicate: (T) -> Boolean): Boolean {
    return any(predicate)
}