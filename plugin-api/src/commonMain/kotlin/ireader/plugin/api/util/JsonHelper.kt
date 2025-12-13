package ireader.plugin.api.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * JSON helper for plugin serialization.
 * Provides convenient methods for JSON encoding/decoding.
 */
object JsonHelper {
    
    /**
     * Default JSON configuration - lenient and ignores unknown keys.
     */
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }
    
    /**
     * Pretty-printing JSON configuration.
     */
    val prettyJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }
    
    /**
     * Serialize object to JSON string.
     */
    inline fun <reified T> encode(value: T): String {
        return json.encodeToString(value)
    }
    
    /**
     * Deserialize JSON string to object.
     */
    inline fun <reified T> decode(jsonString: String): T {
        return json.decodeFromString(jsonString)
    }
    
    /**
     * Serialize object to pretty-printed JSON string.
     */
    inline fun <reified T> encodePretty(value: T): String {
        return prettyJson.encodeToString(value)
    }
    
    /**
     * Try to deserialize JSON string, returning null on failure.
     */
    inline fun <reified T> decodeOrNull(jsonString: String): T? {
        return try {
            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            null
        }
    }
}
