package ireader.domain.js.engine

/**
 * iOS implementation of JSEngine using JavaScriptCore
 * 
 * TODO: Implement using platform.JavaScriptCore
 */
actual class JSEngine {
    actual fun evaluate(script: String): JSValue? {
        // TODO: Implement using JSContext.evaluateScript
        return null
    }
    
    actual fun setGlobal(name: String, value: Any?) {
        // TODO: Implement using JSContext.setObject
    }
    
    actual fun getGlobal(name: String): JSValue? {
        // TODO: Implement using JSContext.objectForKeyedSubscript
        return null
    }
    
    actual fun close() {
        // TODO: Clean up JSContext
    }
}
