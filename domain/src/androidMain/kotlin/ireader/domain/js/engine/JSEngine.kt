package ireader.domain.js.engine

/**
 * Android implementation of JSEngine.
 * 
 * NOTE: The J2V8 JavaScript engine has been moved to an optional plugin
 * (io.github.ireaderorg.plugins.j2v8-engine) to reduce app size.
 * 
 * Users who need JavaScript-based sources (LNReader plugins) should install
 * the J2V8 Engine plugin from the Feature Store.
 * 
 * This stub implementation throws NoJSEngineException to prompt users
 * to install the required plugin.
 */
actual class JSEngine {
    
    actual fun initialize() {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'J2V8 JavaScript Engine' " +
            "plugin from the Feature Store to use JavaScript-based sources."
        )
    }
    
    actual fun evaluateScript(script: String): Any? {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'J2V8 JavaScript Engine' " +
            "plugin from the Feature Store."
        )
    }
    
    actual fun callFunction(name: String, vararg args: Any?): Any? {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'J2V8 JavaScript Engine' " +
            "plugin from the Feature Store."
        )
    }
    
    actual fun setGlobalObject(name: String, value: Any) {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'J2V8 JavaScript Engine' " +
            "plugin from the Feature Store."
        )
    }
    
    actual fun getGlobalObject(name: String): Any? {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'J2V8 JavaScript Engine' " +
            "plugin from the Feature Store."
        )
    }
    
    actual fun dispose() {
        // No-op - nothing to dispose
    }
    
    companion object {
        /**
         * Check if a JavaScript engine is available.
         * Returns false since J2V8 is no longer bundled.
         */
        fun isAvailable(): Boolean = false
    }
}
