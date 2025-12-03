package ireader.core.http

/**
 * iOS implementation of JS engine
 * 
 * TODO: Implement using JavaScriptCore
 */
actual class JS {
    
    actual fun evaluate(script: String): Any? {
        // TODO: Implement using JSContext
        return null
    }
    
    actual fun evaluateAsString(script: String): String {
        // TODO: Implement using JSContext
        return ""
    }
    
    actual fun evaluateAsInt(script: String): Int {
        // TODO: Implement using JSContext
        return 0
    }
    
    actual fun evaluateAsBoolean(script: String): Boolean {
        // TODO: Implement using JSContext
        return false
    }
    
    actual fun evaluateAsList(script: String): List<Any?> {
        // TODO: Implement using JSContext
        return emptyList()
    }
    
    actual fun close() {
        // Clean up JSContext
    }
}
