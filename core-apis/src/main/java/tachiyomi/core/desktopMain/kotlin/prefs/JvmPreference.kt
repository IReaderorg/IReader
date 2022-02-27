/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.prefs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import java.util.prefs.Preferences

/**
 * An implementation of [Preference] backed by Java [Preferences].
 */
class JvmPreference<T>(
    private val preferences: Preferences,
    private val key: String,
    private val defaultValue: T,
    private val adapter: Adapter<T>,
    private val changes: Flow<String>,
) : Preference<T> {

    /**
     * Adapter used to read and write preferences of a given type [T].
     */
    interface Adapter<T> {
        fun get(key: String, preferences: Preferences): T
        fun set(key: String, value: T, preferences: Preferences)
    }

    /**
     * Returns the key of this preference.
     */
    override fun key(): String {
        return key
    }

    /**
     * Returns the current value of this preference.
     */
    override fun get(): T {
        return if (!isSet()) {
            defaultValue
        } else {
            adapter.get(key, preferences)
        }
    }

    /**
     * Sets a new [value] for this preference.
     */
    override fun set(value: T) {
        adapter.set(key, value, preferences)
    }

    /**
     * Returns whether there's an existing entry for this preference.
     */
    override fun isSet(): Boolean {
        return key in preferences.keys()
    }

    /**
     * Deletes the entry of this preference.
     */
    override fun delete() {
        preferences.remove(key)
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
        return changes.map { get() }
    }

    /**
     * Returns a hot [StateFlow] of this preference bound to the given [scope], allowing to read the
     * current value and receive preference updates.
     */
    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return changes.map { get() }.stateIn(scope, SharingStarted.Eagerly, get())
    }

}
