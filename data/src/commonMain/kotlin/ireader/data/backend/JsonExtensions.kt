package ireader.data.backend

import kotlinx.serialization.json.*

/**
 * Extension functions for safe JSON parsing without reflection
 */

// String extraction
fun JsonElement.stringOrNull(key: String): String? =
    this.jsonObject[key]?.jsonPrimitive?.contentOrNull

fun JsonElement.string(key: String, default: String = ""): String =
    stringOrNull(key) ?: default

// Int extraction
fun JsonElement.intOrNull(key: String): Int? =
    this.jsonObject[key]?.jsonPrimitive?.intOrNull

fun JsonElement.int(key: String, default: Int = 0): Int =
    intOrNull(key) ?: default

// Long extraction
fun JsonElement.longOrNull(key: String): Long? =
    this.jsonObject[key]?.jsonPrimitive?.longOrNull

fun JsonElement.long(key: String, default: Long = 0L): Long =
    longOrNull(key) ?: default

// Double extraction
fun JsonElement.doubleOrNull(key: String): Double? =
    this.jsonObject[key]?.jsonPrimitive?.doubleOrNull

fun JsonElement.double(key: String, default: Double = 0.0): Double =
    doubleOrNull(key) ?: default

// Boolean extraction
fun JsonElement.booleanOrNull(key: String): Boolean? =
    this.jsonObject[key]?.jsonPrimitive?.booleanOrNull

fun JsonElement.boolean(key: String, default: Boolean = false): Boolean =
    booleanOrNull(key) ?: default

// Nested object extraction
fun JsonElement.objectOrNull(key: String): JsonObject? =
    this.jsonObject[key]?.jsonObject

// Array extraction
fun JsonElement.arrayOrNull(key: String): JsonArray? =
    this.jsonObject[key]?.jsonArray

// Map to list
fun <T> JsonArray.mapToList(transform: (JsonElement) -> T): List<T> =
    this.map(transform)

// Safe first element
fun JsonArray.firstOrNull(): JsonElement? =
    this.getOrNull(0)

/**
 * Build a JSON object from key-value pairs
 */
fun buildJsonObject(vararg pairs: Pair<String, Any?>): JsonObject {
    return buildJsonObject {
        pairs.forEach { (key, value) ->
            when (value) {
                null -> put(key, JsonNull)
                is String -> put(key, value)
                is Int -> put(key, value)
                is Long -> put(key, value)
                is Double -> put(key, value)
                is Boolean -> put(key, value)
                is JsonElement -> put(key, value)
                else -> put(key, value.toString())
            }
        }
    }
}
