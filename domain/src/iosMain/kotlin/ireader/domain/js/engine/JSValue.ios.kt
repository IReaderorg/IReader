package ireader.domain.js.engine

/**
 * iOS implementation of JSValue wrapper
 * 
 * TODO: Full implementation wrapping platform.JavaScriptCore.JSValue
 */
actual class JSValue(private val rawValue: Any? = null) {
    
    actual fun asString(): String = rawValue?.toString() ?: ""
    
    actual fun asInt(): Int = when (rawValue) {
        is Number -> rawValue.toInt()
        is String -> rawValue.toIntOrNull() ?: 0
        else -> 0
    }
    
    actual fun asBoolean(): Boolean = when (rawValue) {
        is Boolean -> rawValue
        is Number -> rawValue.toInt() != 0
        is String -> rawValue.lowercase() == "true"
        else -> false
    }
    
    actual fun asMap(): Map<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        return (rawValue as? Map<String, Any?>) ?: emptyMap()
    }
    
    actual fun asList(): List<Any?> {
        @Suppress("UNCHECKED_CAST")
        return (rawValue as? List<Any?>) ?: emptyList()
    }
    
    actual fun isNull(): Boolean = rawValue == null
    
    actual fun isUndefined(): Boolean = rawValue == null
    
    actual fun getRaw(): Any? = rawValue
    
    actual companion object {
        actual fun from(value: Any?): JSValue = JSValue(value)
    }
}
