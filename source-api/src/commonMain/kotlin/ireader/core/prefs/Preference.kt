package ireader.core.prefs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


/**
 * A wrapper around application preferences without knowing implementation details. Instances of
 * this interface must be provided through a [PreferenceStore].
 */
interface Preference<T> {

    /**
     * Returns the key of this preference.
     */
    fun key(): String

    /**
     * Returns the current value of this preference.
     */
    fun get(): T

    /**
     * Returns the current value of this preference.
     */
    suspend fun read(): T

    /**
     * Sets a new [value] for this preference.
     */
    fun set(value: T)

    /**
     * Returns whether there's an existing entry for this preference.
     */
    fun isSet(): Boolean

    /**
     * Deletes the entry of this preference.
     */
    fun delete()

    /**
     * Returns the default value of this preference.
     */
    fun defaultValue(): T

    /**
     * Returns a cold [Flow] of this preference to receive updates when its value changes.
     */
    fun changes(): Flow<T>

    /**
     * Returns a hot [StateFlow] of this preference bound to the given [scope], allowing to read the
     * current value and receive preference updates.
     */
    fun stateIn(scope: CoroutineScope): StateFlow<T>
}
