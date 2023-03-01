/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.http

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

fun PreferenceStore.getRateBucket(key: String, capacity: Int, rate: Long): Preference<RateBucket> {
  return getObject(
    key = key,
    defaultValue = RateBucket(capacity, rate),
    serializer = { it.serialize() },
    deserializer = { RateBucket.deserialize(it) }
  )
}
