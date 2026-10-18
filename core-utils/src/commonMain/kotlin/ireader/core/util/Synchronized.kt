package ireader.core.util

/**
 * Executes the given function [block] under the lock on the specified [lock] object.
 */
expect inline fun <R> synchronized(lock: Any, block: () -> R): R
