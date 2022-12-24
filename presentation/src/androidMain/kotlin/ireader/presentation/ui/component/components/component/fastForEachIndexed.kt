package ireader.presentation.ui.component.components.component

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal inline fun <T> List<T>.fastForEachIndexed(action: (Int, T) -> Unit) {
    contract { callsInPlace(action) }
    for (index in indices) {
        val item = get(index)
        action(index, item)
    }
}
