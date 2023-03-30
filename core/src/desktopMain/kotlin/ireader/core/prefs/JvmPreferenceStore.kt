/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.prefs.PreferenceChangeListener
import java.util.prefs.Preferences

/**
 * An implementation of a [PreferenceStore] backed by Java's [Preferences].
 *
 * TODO(inorichi): this will write to the registry on Windows. Consider creating a custom
 *   implementation that writes to disk and has better support for string sets.
 */
class JvmPreferenceStore(name: String) : PreferenceStore {

  /**
   * The [Preferences] instance where they're saved.
   */
  private val store = Preferences.userRoot().node("ireader/$name")

  /**
   * Scope where the store handles IO.
   */
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  /**
   * Shared flow to listen for preference updates
   */
  private val changedKeys = callbackFlow<String> {
    val listener = PreferenceChangeListener {
      trySend(it.key)
    }
    store.addPreferenceChangeListener(listener)
    awaitClose { store.removePreferenceChangeListener(listener) }
  }.shareIn(scope, SharingStarted.WhileSubscribed())

  /**
   * Returns a [String] preference for this [key].
   */
  override fun getString(key: String, defaultValue: String): Preference<String> {
    return JvmPreference(store, key, defaultValue, StringAdapter, getChanges(key))
  }

  /**
   * Returns a [Long] preference for this [key].
   */
  override fun getLong(key: String, defaultValue: Long): Preference<Long> {
    return JvmPreference(store, key, defaultValue, LongAdapter, getChanges(key))
  }

  /**
   * Returns an [Int] preference for this [key].
   */
  override fun getInt(key: String, defaultValue: Int): Preference<Int> {
    return JvmPreference(store, key, defaultValue, IntAdapter, getChanges(key))
  }

  /**
   * Returns a [Float] preference for this [key].
   */
  override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
    return JvmPreference(store, key, defaultValue, FloatAdapter, getChanges(key))
  }

  /**
   * Returns a [Boolean] preference for this [key].
   */
  override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
    return JvmPreference(store, key, defaultValue, BooleanAdapter, getChanges(key))
  }

  /**
   * Returns a [Set<String>] preference for this [key].
   */
  override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
    return JvmPreference(store, key, defaultValue, StringSetAdapter, getChanges(key))
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
    val adapter = ObjectAdapter(serializer, deserializer)
    return JvmPreference(store, key, defaultValue, adapter, getChanges(key))
  }

  /**
   * Returns a flow for the preference updates of the given [key].
   */
  private fun getChanges(key: String): Flow<String> {
    return changedKeys.filter { it == key }
  }

}
