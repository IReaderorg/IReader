package ireader.domain.models.entities

import androidx.compose.ui.state.ToggleableState

/**
 * Represents the state of a checkbox for category selection.
 * Supports both simple checked/unchecked state and tri-state for bulk operations.
 * 
 * Following Mihon's CheckboxState pattern for consistent category selection UI.
 * 
 * @param T The type of value associated with the checkbox (typically Category)
 */
sealed class CheckboxState<out T>(open val value: T) {
    
    /**
     * Cycle to the next state (works for both State and TriState).
     */
    abstract fun next(): CheckboxState<T>
    
    /**
     * Simple checked/unchecked state for single item selection.
     */
    sealed class State<out T>(override val value: T) : CheckboxState<T>(value) {
        data class Checked<out T>(override val value: T) : State<T>(value) {
            override fun next(): State<T> = None(value)
        }
        data class None<out T>(override val value: T) : State<T>(value) {
            override fun next(): State<T> = Checked(value)
        }
        
        val isChecked: Boolean get() = this is Checked
    }
    
    /**
     * Tri-state for bulk operations where items may have mixed states.
     * - Include: All selected items have this category
     * - Exclude: No selected items have this category  
     * - None: Some selected items have this category (indeterminate)
     */
    sealed class TriState<out T>(override val value: T) : CheckboxState<T>(value) {
        data class Include<out T>(override val value: T) : TriState<T>(value) {
            override fun next(): TriState<T> = Exclude(value)
        }
        data class Exclude<out T>(override val value: T) : TriState<T>(value) {
            override fun next(): TriState<T> = None(value)
        }
        data class None<out T>(override val value: T) : TriState<T>(value) {
            override fun next(): TriState<T> = Include(value)
        }
    }
}

/**
 * Convert CheckboxState to Compose ToggleableState for UI rendering.
 */
fun <T> CheckboxState<T>.asToggleableState(): ToggleableState = when (this) {
    is CheckboxState.State.Checked -> ToggleableState.On
    is CheckboxState.State.None -> ToggleableState.Off
    is CheckboxState.TriState.Include -> ToggleableState.On
    is CheckboxState.TriState.Exclude -> ToggleableState.Off
    is CheckboxState.TriState.None -> ToggleableState.Indeterminate
}

/**
 * Map a list of items to CheckboxState based on a predicate.
 * Useful for initializing category selection dialogs.
 * 
 * @param predicate Returns true if the item should be checked
 */
fun <T> List<T>.mapAsCheckboxState(predicate: (T) -> Boolean): List<CheckboxState.State<T>> {
    return map { item ->
        if (predicate(item)) {
            CheckboxState.State.Checked(item)
        } else {
            CheckboxState.State.None(item)
        }
    }
}

/**
 * Map a list of items to TriState CheckboxState based on selection counts.
 * Used for bulk operations where items may have mixed states.
 * 
 * @param selectedCount Returns the count of selected items that have this item
 * @param totalSelected Total number of selected items
 */
fun <T> List<T>.mapAsTriStateCheckboxState(
    selectedCount: (T) -> Int,
    totalSelected: Int
): List<CheckboxState.TriState<T>> {
    return map { item ->
        val count = selectedCount(item)
        when {
            count == 0 -> CheckboxState.TriState.Exclude(item)
            count == totalSelected -> CheckboxState.TriState.Include(item)
            else -> CheckboxState.TriState.None(item)
        }
    }
}
