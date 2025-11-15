package ireader.domain.js.engine

import app.cash.quickjs.QuickJs

/**
 * Android implementation of JSValue using QuickJS.
 */
actual class JSValue private constructor(private val value: Any?) {
    
    actual fun asString(): String {
        return when (value) {
            is String -> value
            null -> throw JSException("Cannot convert null to String")
            else -> value.toString()
        }
    }
    
    actual fun asInt(): Int {
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: throw JSException("Cannot convert '$value' to Int")
            null -> throw JSException("Cannot convert null to Int")
            else -> throw JSException("Cannot convert ${value::class.simpleName} to Int")
        }
    }
    
    actual fun asBoolean(): Boolean {
        return when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            is Number -> value.toInt() != 0
            null -> false
            else -> throw JSException("Cannot convert ${value::class.simpleName} to Boolean")
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    actual fun asMap(): Map<String, Any?> {
        return when (value) {
            is Map<*, *> -> value as Map<String, Any?>
            null -> throw JSException("Cannot convert null to Map")
            else -> throw JSException("Cannot convert ${value::class.simpleName} to Map")
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    actual fun asList(): List<Any?> {
        return when (value) {
            is List<*> -> value as List<Any?>
            is Array<*> -> value.toList()
            null -> throw JSException("Cannot convert null to List")
            else -> throw JSException("Cannot convert ${value::class.simpleName} to List")
        }
    }
    
    actual fun isNull(): Boolean {
        return value == null
    }
    
    actual fun isUndefined(): Boolean {
        // QuickJS represents undefined as null in Kotlin
        return value == null
    }
    
    actual fun getRaw(): Any? {
        return value
    }
    
    actual companion object {
        actual fun from(value: Any?): JSValue {
            return JSValue(value)
        }
    }
}
