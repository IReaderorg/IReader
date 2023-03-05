package ireader.presentation.ui.component.utils

import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy


actual inline fun <T, R> List<T>.fastMap(transform: (T) -> R): List<R> {
    return fastMap(transform)
}



actual inline fun <T> List<T>.fastForEach(action: (T) -> Unit) {
    return fastForEach(action)
}


actual inline fun <T, R : Comparable<R>> List<T>.fastMaxBy(selector: (T) -> R): T? {
    return fastMaxBy(selector)
}

actual inline fun <T> List<T>.fastFirstOrNull(predicate: (T) -> Boolean): T? {
    return fastMaxBy(predicate)
}

actual inline fun <T> List<T>.fastAll(predicate: (T) -> Boolean): Boolean {
    return fastAll(predicate)
}

actual inline fun <T> List<T>.fastAny(predicate: (T) -> Boolean): Boolean {
    return fastAny(predicate)
}