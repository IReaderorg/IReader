package xyz.nulldev.androidcompat.io.sharedprefs

import com.russhwolf.settings.Settings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

inline fun <reified T> Settings.encodeValue(
    serializer: KSerializer<T>,
    key: String,
    value: T,
) {
    putString(key, Json.encodeToString(serializer, value))
}

inline fun <reified T> Settings.decodeValue(
    serializer: KSerializer<T>,
    key: String,
    defaultValue: T,
): T {
    val stringValue = getStringOrNull(key) ?: return defaultValue
    return try {
        Json.decodeFromString(serializer, stringValue)
    } catch (e: SerializationException) {
        defaultValue
    }
}

inline fun <reified T> Settings.decodeValueOrNull(
    serializer: KSerializer<T>,
    key: String,
): T? {
    val stringValue = getStringOrNull(key) ?: return null
    return try {
        Json.decodeFromString(serializer, stringValue)
    } catch (e: SerializationException) {
        null
    }
}
