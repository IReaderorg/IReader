

package org.ireader.core_api.http

import org.ireader.core_api.http.impl.RateBucketImpl
import org.ireader.core_api.http.impl.deserialize
import org.ireader.core_api.http.main.RateBucket
import org.ireader.core_api.prefs.Preference
import org.ireader.core_api.prefs.PreferenceStore

fun PreferenceStore.getRateBucket(key: String, capacity: Int, rate: Long): Preference<RateBucket> {
    return getObject(
        key = key,
        defaultValue = RateBucketImpl(capacity, rate),
        serializer = { it.serialize() },
        deserializer = { RateBucketImpl.Companion.deserialize(it) }
    )
}
