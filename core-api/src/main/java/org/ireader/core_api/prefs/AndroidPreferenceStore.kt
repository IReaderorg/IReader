package org.ireader.core_api.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ireader.core_api.prefs.AndroidPreference.Primitive

/**
 * An implementation of a [PreferenceStore] backed by Androidx [DataStore].
 *
 * Note: there MUST be only one instance of this class per data store file.
 */
class AndroidPreferenceStore(
    private val context: Context,
    private val name: String,
    autoInit: Boolean = true
) : PreferenceStore {

    /**
     * Scope where the store handles IO.
     */
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * The internal [DataStore] instance.
     */
    private val store = PreferenceDataStoreFactory.create(
        corruptionHandler = null,
        migrations = listOf(),
        scope = scope,
        produceFile = { context.preferencesDataStoreFile(name) }
    )

    init {
        // Initialize preferences on an IO thread
        if (autoInit) {
            scope.launch {
                store.data.first()
            }
        }
    }

    /**
     * Returns a [String] preference for this [key].
     */
    override fun getString(key: String, defaultValue: String): Preference<String> {
        return AndroidPreference.Primitive(store, scope, stringPreferencesKey(key), defaultValue)
    }

    /**
     * Returns a [Long] preference for this [key].
     */
    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return AndroidPreference.Primitive(store, scope, longPreferencesKey(key), defaultValue)
    }

    /**
     * Returns an [Int] preference for this [key].
     */
    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return AndroidPreference.Primitive(store, scope, intPreferencesKey(key), defaultValue)
    }

    /**
     * Returns a [Float] preference for this [key].
     */
    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return Primitive(store, scope, floatPreferencesKey(key), defaultValue)
    }

    /**
     * Returns a [Boolean] preference for this [key].
     */
    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return AndroidPreference.Primitive(store, scope, booleanPreferencesKey(key), defaultValue)
    }

    /**
     * Returns a [Set<String>] preference for this [key].
     */
    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return AndroidPreference.Primitive(store, scope, stringSetPreferencesKey(key), defaultValue)
    }

    /**
     * Returns a preference of type [T] for this [key]. The [serializer] and [deserializer] function
     * must be provided.
     */
    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T
    ): Preference<T> {
        return AndroidPreference.Object(
            store, scope, stringPreferencesKey(key), defaultValue, serializer, deserializer
        )
    }
}
