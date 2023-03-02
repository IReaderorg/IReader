package ireader.presentation.ui.component.utils

expect inline fun <T, R> List<T>.fastMap(transform: (T) -> R): List<R>


expect inline fun <T> List<T>.fastForEach(action: (T) -> Unit)


expect inline fun <T, R : Comparable<R>> List<T>.fastMaxBy(selector: (T) -> R): T?