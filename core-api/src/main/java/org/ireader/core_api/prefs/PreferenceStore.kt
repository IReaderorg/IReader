/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.prefs

/**
 * A wrapper around an application preferences store. Implementations of this interface should
 * persist these preferences on disk.
 */
interface PreferenceStore {

    /**
     * Returns a [String] preference for this [key].
     */
    fun getString(key: String, defaultValue: String = ""): Preference<String>

    /**
     * Returns a [Long] preference for this [key].
     */
    fun getLong(key: String, defaultValue: Long = 0): Preference<Long>

    /**
     * Returns an [Int] preference for this [key].
     */
    fun getInt(key: String, defaultValue: Int = 0): Preference<Int>

    /**
     * Returns a [Float] preference for this [key].
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Preference<Float>

    /**
     * Returns a [Boolean] preference for this [key].
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Preference<Boolean>

    /**
     * Returns a [Set<String>] preference for this [key].
     */
    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Preference<Set<String>>

    /**
     * Returns a preference of type [T] for this [key]. The [serializer] and [deserializer] function
     * must be provided.
     */
    fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T
    ): Preference<T>

}

/**
 * Returns an enum preference of type [T] for this [key].
 */
inline fun <reified T : Enum<T>> PreferenceStore.getEnum(
    key: String,
    defaultValue: T
): Preference<T> {
    return getObject(key, defaultValue, { it.name }, {
        try {
            enumValueOf(it)
        } catch (e: IllegalArgumentException) {
            defaultValue
        }
    })
}
