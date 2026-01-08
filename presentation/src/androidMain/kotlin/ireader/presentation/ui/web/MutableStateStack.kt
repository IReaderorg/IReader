package ireader.presentation.ui.web

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * A mutable stack implementation backed by SnapshotStateList for Compose state observation.
 * This allows the UI to automatically recompose when the stack changes.
 */
class MutableStateStack<T>(
    items: List<T> = emptyList()
) {
    private val _items: SnapshotStateList<T> = mutableStateListOf<T>().apply {
        addAll(items)
    }
    
    /**
     * Read-only view of the items in the stack (bottom to top)
     */
    val items: List<T> get() = _items.toList()
    
    /**
     * Number of items in the stack
     */
    val size: Int get() = _items.size
    
    /**
     * Whether the stack is empty
     */
    val isEmpty: Boolean get() = _items.isEmpty()
    
    /**
     * Whether the stack is not empty
     */
    val isNotEmpty: Boolean get() = _items.isNotEmpty()
    
    /**
     * Get the item at the top of the stack, or null if empty
     */
    val lastItemOrNull: T? get() = _items.lastOrNull()
    
    /**
     * Get the item at the top of the stack
     * @throws NoSuchElementException if the stack is empty
     */
    val lastItem: T get() = _items.last()
    
    /**
     * Push an item onto the top of the stack
     */
    fun push(item: T) {
        _items.add(item)
    }
    
    /**
     * Pop the top item from the stack
     * @return the popped item, or null if the stack is empty
     */
    fun pop(): T? {
        return if (_items.isNotEmpty()) {
            _items.removeAt(_items.lastIndex)
        } else {
            null
        }
    }
    
    /**
     * Pop all items except the bottom one
     */
    fun popToRoot() {
        while (_items.size > 1) {
            _items.removeAt(_items.lastIndex)
        }
    }
    
    /**
     * Clear all items from the stack
     */
    fun clear() {
        _items.clear()
    }
    
    /**
     * Replace all items in the stack
     */
    fun replaceAll(items: List<T>) {
        _items.clear()
        _items.addAll(items)
    }
    
    /**
     * Check if the stack contains an item
     */
    operator fun contains(item: T): Boolean = item in _items
}

/**
 * Create a MutableStateStack with the given initial items
 */
fun <T> mutableStateStackOf(vararg items: T): MutableStateStack<T> {
    return MutableStateStack(items.toList())
}
