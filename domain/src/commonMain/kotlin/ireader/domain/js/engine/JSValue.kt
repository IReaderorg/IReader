package ireader.domain.js.engine

/**
 * Wrapper class representing a JavaScript value.
 * Provides type-safe conversion methods for accessing JavaScript objects from Kotlin.
 */
expect class JSValue {
    
    /**
     * Converts this value to a String.
     * @throws JSException if conversion fails
     */
    fun asString(): String
    
    /**
     * Converts this value to an Int.
     * @throws JSException if conversion fails
     */
    fun asInt(): Int
    
    /**
     * Converts this value to a Boolean.
     * @throws JSException if conversion fails
     */
    fun asBoolean(): Boolean
    
    /**
     * Converts this value to a Map<String, Any?>.
     * @throws JSException if conversion fails
     */
    fun asMap(): Map<String, Any?>
    
    /**
     * Converts this value to a List<Any?>.
     * @throws JSException if conversion fails
     */
    fun asList(): List<Any?>
    
    /**
     * Checks if this value is null.
     */
    fun isNull(): Boolean
    
    /**
     * Checks if this value is undefined.
     */
    fun isUndefined(): Boolean
    
    /**
     * Gets the raw underlying value.
     */
    fun getRaw(): Any?
    
    companion object {
        /**
         * Creates a JSValue from a raw value.
         */
        fun from(value: Any?): JSValue
    }
}
