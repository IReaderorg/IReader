/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.prefs

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ireader.core_api.util.decodeBase64
import org.ireader.core_api.util.encodeBase64
import java.util.prefs.Preferences

internal object StringAdapter : JvmPreference.Adapter<String> {
  override fun get(key: String, preferences: Preferences): String {
    return preferences.get(key, null)!! // Not called unless key is present.
  }

  override fun set(key: String, value: String, preferences: Preferences) {
    preferences.put(key, value)
  }
}

internal object LongAdapter : JvmPreference.Adapter<Long> {
  override fun get(key: String, preferences: Preferences): Long {
    return preferences.getLong(key, 0)
  }

  override fun set(key: String, value: Long, preferences: Preferences) {
    preferences.putLong(key, value)
  }
}

internal object IntAdapter : JvmPreference.Adapter<Int> {
  override fun get(key: String, preferences: Preferences): Int {
    return preferences.getInt(key, 0)
  }

  override fun set(key: String, value: Int, preferences: Preferences) {
    preferences.putInt(key, value)
  }
}

internal object FloatAdapter : JvmPreference.Adapter<Float> {
  override fun get(key: String, preferences: Preferences): Float {
    return preferences.getFloat(key, 0f)
  }

  override fun set(key: String, value: Float, preferences: Preferences) {
    preferences.putFloat(key, value)
  }
}

internal object BooleanAdapter : JvmPreference.Adapter<Boolean> {
  override fun get(key: String, preferences: Preferences): Boolean {
    return preferences.getBoolean(key, false)
  }

  override fun set(key: String, value: Boolean, preferences: Preferences) {
    preferences.putBoolean(key, value)
  }
}

internal object StringSetAdapter : JvmPreference.Adapter<Set<String>> {
  @Suppress("UNCHECKED_CAST")
  override fun get(key: String, preferences: Preferences): Set<String> {
    val encoded = preferences.get(key, null) ?: return emptySet()
    val json = encoded.decodeBase64().utf8()
    return Json.Default.decodeFromString(json)
  }

  override fun set(key: String, value: Set<String>, preferences: Preferences) {
    val encoded = Json.Default.encodeToString(value).encodeBase64()
    preferences.put(key, encoded)
  }
}

internal class ObjectAdapter<T>(
  private val serializer: (T) -> String,
  private val deserializer: (String) -> T
) : JvmPreference.Adapter<T> {

  override fun get(key: String, preferences: Preferences): T {
    return deserializer(preferences.get(key, null)!!) // Not called unless key is present.
  }

  override fun set(key: String, value: T, preferences: Preferences) {
    preferences.put(key, serializer(value))
  }

}
