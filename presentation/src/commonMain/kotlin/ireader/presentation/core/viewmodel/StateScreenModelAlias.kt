package ireader.presentation.core.viewmodel

/**
 * Type alias for compatibility with Voyager's StateScreenModel pattern.
 * This allows code written for Voyager to work with IReaderStateScreenModel.
 */
typealias StateScreenModel<T> = IReaderStateScreenModel<T>

/**
 * Extension function to provide mutableState-like functionality.
 * Updates the state using the provided transform function.
 * Note: updateState is public in IReaderStateScreenModel, so this works correctly.
 */
fun <T> IReaderStateScreenModel<T>.mutableState(transform: (T) -> T) {
    updateState(transform)
}
