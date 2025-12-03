package ireader.core.http

/**
 * iOS implementation of JS engine using JavaScriptCore
 * 
 * TODO: Implement using JavaScriptCore framework
 */
actual class JS {
    
    actual fun evaluateAsString(script: String): String {
        // TODO: Implement using JSContext
        return ""
    }
    
    actual fun evaluateAsInt(script: String): Int {
        // TODO: Implement using JSContext
        return 0
    }
    
    actual fun evaluateAsDouble(script: String): Double {
        // TODO: Implement using JSContext
        return 0.0
    }
    
    actual fun evaluateAsBoolean(script: String): Boolean {
        // TODO: Implement using JSContext
        return false
    }
    
    actual fun close() {
        // Clean up JSContext
    }
}
