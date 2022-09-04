/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.http

import org.ireader.core_api.prefs.Preference
import org.ireader.core_api.prefs.PreferenceStore

fun PreferenceStore.getRateBucket(key: String, capacity: Int, rate: Long): Preference<RateBucket> {
  return getObject(
    key = key,
    defaultValue = RateBucket(capacity, rate),
    serializer = { it.serialize() },
    deserializer = { RateBucket.deserialize(it) }
  )
}
