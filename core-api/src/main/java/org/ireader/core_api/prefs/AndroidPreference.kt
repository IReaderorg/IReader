/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * An implementation of [Preference] backed by Androidx's [DataStore].
 *
 * Read operations are blocking, but writes are performed in the given [scope], which should be
 * an IO thread.
 */
internal sealed class AndroidPreference<K, T>(
  private val store: DataStore<Preferences>,
  private val scope: CoroutineScope,
  private val key: Key<K>,
  private val defaultValue: T
) : Preference<T> {

  /**
   * Reads the current value of this [key] in the given [preferences].
   */
  abstract fun read(preferences: Preferences, key: Key<K>): T?

  /**
   * Writes a new [value] to the [key] of the given [preferences].
   */
  abstract fun write(preferences: MutablePreferences, key: Key<K>, value: T)

  /**
   * Returns the key of this preference.
   */
  override fun key(): String {
    return key.name
  }

  /**
   * Returns the current value of this preference.
   */
  override fun get(): T {
    return runBlocking {
      read(store.data.first(), key) ?: defaultValue
    }
  }

  /**
   * Sets a new [value] for this preference.
   */
  override fun set(value: T) {
    scope.launch {
      store.edit { write(it, key, value) }
    }
  }

  /**
   * Returns whether there's an existing entry for this preference.
   */
  override fun isSet(): Boolean {
    return runBlocking {
      store.data.first().contains(key)
    }
  }

  /**
   * Deletes the entry of this preference.
   */
  override fun delete() {
    scope.launch {
      store.edit { it.remove(key) }
    }
  }

  /**
   * Returns the default value of this preference.
   */
  override fun defaultValue(): T {
    return defaultValue
  }

  /**
   * Returns a cold [Flow] of this preference to receive updates when its value changes.
   */
  override fun changes(): Flow<T> {
    return store.data
      .drop(1)
      .map { read(it, key) ?: defaultValue }
      .distinctUntilChanged()
  }

  /**
   * Returns a hot [StateFlow] of this preference bound to the given [scope], allowing to read the
   * current value and receive preference updates.
   */
  override fun stateIn(scope: CoroutineScope): StateFlow<T> {
    return changes().stateIn(scope, SharingStarted.Eagerly, get())
  }

  /**
   * Implementation of an [AndroidPreference] for the supported primitives.
   */
  internal class Primitive<T>(
    store: DataStore<Preferences>,
    scope: CoroutineScope,
    key: Key<T>,
    defaultValue: T
  ) : AndroidPreference<T, T>(store, scope, key, defaultValue) {

    override fun read(preferences: Preferences, key: Key<T>): T? {
      return preferences[key]
    }

    override fun write(preferences: MutablePreferences, key: Key<T>, value: T) {
      preferences[key] = value
    }

  }

  /**
   * Implementation of an [AndroidPreference] for any object that is serializable to a String.
   */
  internal class Object<T>(
    store: DataStore<Preferences>,
    scope: CoroutineScope,
    key: Key<String>,
    defaultValue: T,
    private val serializer: (T) -> String,
    private val deserializer: (String) -> T
  ) : AndroidPreference<String, T>(store, scope, key, defaultValue) {

    override fun read(preferences: Preferences, key: Key<String>): T? {
      return preferences[key]?.let(deserializer)
    }

    override fun write(preferences: MutablePreferences, key: Key<String>, value: T) {
      preferences[key] = serializer(value)
    }

  }

}
