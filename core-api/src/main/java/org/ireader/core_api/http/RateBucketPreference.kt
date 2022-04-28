

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
