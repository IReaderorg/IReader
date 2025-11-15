package ireader.domain.js.engine

import org.graalvm.polyglot.Value

/**
 * Desktop implementation of JSValue using GraalVM.
 */
actual class JSValue private constructor(
    private val value: Value?,
    private val kotlinValue: Any? = null
) {
    
    actual fun asString(): String {
        // Handle Kotlin values
        if (value == null && kotlinValue != null) {
            return kotlinValue.toString()
        }
        
        if (value == null || value.isNull) {
            throw JSException("Cannot convert null to String")
        }
        return value.asString()
    }
    
    actual fun asInt(): Int {
        // Handle Kotlin values
        if (value == null && kotlinValue != null) {
            return when (kotlinValue) {
                is Number -> kotlinValue.toInt()
                is String -> kotlinValue.toIntOrNull() ?: throw JSException("Cannot convert '$kotlinValue' to Int")
                else -> throw JSException("Cannot convert ${kotlinValue.javaClass.simpleName} to Int")
            }
        }
        
        if (value == null || value.isNull) {
            throw JSException("Cannot convert null to Int")
        }
        return when {
            value.fitsInInt() -> value.asInt()
            value.fitsInLong() -> value.asLong().toInt()
            value.fitsInDouble() -> value.asDouble().toInt()
            value.isString -> value.asString().toIntOrNull() 
                ?: throw JSException("Cannot convert '${value.asString()}' to Int")
            else -> throw JSException("Cannot convert value to Int")
        }
    }
    
    actual fun asBoolean(): Boolean {
        // Handle Kotlin values
        if (value == null && kotlinValue != null) {
            return when (kotlinValue) {
                is Boolean -> kotlinValue
                is Number -> kotlinValue.toInt() != 0
                is String -> kotlinValue.toBoolean()
                else -> true
            }
        }
        
        if (value == null || value.isNull) {
            return false
        }
        return when {
            value.isBoolean -> value.asBoolean()
            value.isString -> value.asString().toBoolean()
            value.isNumber -> value.asInt() != 0
            else -> throw JSException("Cannot convert value to Boolean")
        }
    }
    
    actual fun asMap(): Map<String, Any?> {
        // Handle Kotlin values
        if (value == null && kotlinValue != null) {
            return when (kotlinValue) {
                is Map<*, *> -> kotlinValue as? Map<String, Any?> 
                    ?: kotlinValue.mapKeys { it.key.toString() }.mapValues { it.value }
                else -> throw JSException("Cannot convert ${kotlinValue.javaClass.simpleName} to Map")
            }
        }
        
        if (value == null || value.isNull) {
            throw JSException("Cannot convert null to Map")
        }
        
        if (!value.hasMembers()) {
            throw JSException("Value is not an object")
        }
        
        val map = mutableMapOf<String, Any?>()
        value.memberKeys.forEach { key ->
            map[key] = convertValue(value.getMember(key))
        }
        return map
    }
    
    actual fun asList(): List<Any?> {
        // Handle Kotlin values
        if (value == null && kotlinValue != null) {
            return when (kotlinValue) {
                is List<*> -> kotlinValue
                is Array<*> -> kotlinValue.toList()
                else -> throw JSException("Cannot convert ${kotlinValue.javaClass.simpleName} to List")
            }
        }
        
        if (value == null || value.isNull) {
            throw JSException("Cannot convert null to List")
        }
        
        if (!value.hasArrayElements()) {
            throw JSException("Value is not an array")
        }
        
        val list = mutableListOf<Any?>()
        for (i in 0 until value.arraySize) {
            list.add(convertValue(value.getArrayElement(i)))
        }
        return list
    }
    
    actual fun isNull(): Boolean {
        if (value == null && kotlinValue != null) {
            return kotlinValue == null
        }
        return value == null || value.isNull
    }
    
    actual fun isUndefined(): Boolean {
        if (value == null && kotlinValue != null) {
            return kotlinValue == null
        }
        return value == null || (value.isNull && !value.isHostObject)
    }
    
    actual fun getRaw(): Any? {
        return kotlinValue ?: value
    }
    
    private fun convertValue(v: Value?): Any? {
        if (v == null || v.isNull) return null
        
        return when {
            v.isBoolean -> v.asBoolean()
            v.isString -> v.asString()
            v.fitsInInt() -> v.asInt()
            v.fitsInLong() -> v.asLong()
            v.fitsInDouble() -> v.asDouble()
            v.hasArrayElements() -> {
                val list = mutableListOf<Any?>()
                for (i in 0 until v.arraySize) {
                    list.add(convertValue(v.getArrayElement(i)))
                }
                list
            }
            v.hasMembers() -> {
                val map = mutableMapOf<String, Any?>()
                v.memberKeys.forEach { key ->
                    map[key] = convertValue(v.getMember(key))
                }
                map
            }
            else -> v.toString()
        }
    }
    
    actual companion object {
        actual fun from(value: Any?): JSValue {
            return when (value) {
                is Value -> JSValue(value, null)
                is JSValue -> value
                // For Kotlin types (Map, List, etc.), store them in kotlinValue
                else -> JSValue(null, value)
            }
        }
    }
}
