package ireader.domain.js.engine

/**
 * iOS implementation of JSValue wrapper
 * 
 * TODO: Implement wrapping platform.JavaScriptCore.JSValue
 */
actual class JSValue {
    actual fun isNull(): Boolean = true
    actual fun isUndefined(): Boolean = true
    actual fun isBoolean(): Boolean = false
    actual fun isNumber(): Boolean = false
    actual fun isString(): Boolean = false
    actual fun isArray(): Boolean = false
    actual fun isObject(): Boolean = false
    
    actual fun toBoolean(): Boolean = false
    actual fun toInt(): Int = 0
    actual fun toLong(): Long = 0L
    actual fun toDouble(): Double = 0.0
    actual fun toStringValue(): String = ""
    actual fun toList(): List<JSValue> = emptyList()
    actual fun toMap(): Map<String, JSValue> = emptyMap()
    
    actual fun getProperty(name: String): JSValue? = null
    actual fun getIndex(index: Int): JSValue? = null
}
