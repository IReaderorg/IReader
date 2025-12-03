package ireader.domain.js.engine

import platform.JavaScriptCore.JSValue as PlatformJSValue
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of JSValue wrapper
 */
@OptIn(ExperimentalForeignApi::class)
actual class JSValue(private val rawValue: Any? = null) {
    
    private val platformValue: PlatformJSValue? = rawValue as? PlatformJSValue
    
    actual fun asString(): String {
        return when {
            platformValue != null -> platformValue.toString() ?: ""
            rawValue is String -> rawValue
            rawValue != null -> rawValue.toString()
            else -> ""
        }
    }
    
    actual fun asInt(): Int {
        return when {
            platformValue != null -> platformValue.toInt32()
            rawValue is Number -> rawValue.toInt()
            rawValue is String -> rawValue.toIntOrNull() ?: 0
            else -> 0
        }
    }
    
    actual fun asBoolean(): Boolean {
        return when {
            platformValue != null -> platformValue.toBool()
            rawValue is Boolean -> rawValue
            rawValue is Number -> rawValue.toInt() != 0
            rawValue is String -> rawValue.lowercase() == "true"
            else -> false
        }
    }
    
    actual fun asMap(): Map<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        return (rawValue as? Map<String, Any?>) ?: emptyMap()
    }
    
    actual fun asList(): List<Any?> {
        @Suppress("UNCHECKED_CAST")
        return (rawValue as? List<Any?>) ?: emptyList()
    }
    
    actual fun isNull(): Boolean {
        return when {
            platformValue != null -> platformValue.isNull()
            else -> rawValue == null
        }
    }
    
    actual fun isUndefined(): Boolean {
        return when {
            platformValue != null -> platformValue.isUndefined()
            else -> rawValue == null
        }
    }
    
    actual fun getRaw(): Any? = rawValue
    
    actual companion object {
        actual fun from(value: Any?): JSValue = JSValue(value)
    }
}
