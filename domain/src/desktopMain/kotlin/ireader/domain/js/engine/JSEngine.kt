package ireader.domain.js.engine

/**
 * Desktop implementation of JSEngine.
 * 
 * NOTE: The GraalVM JavaScript engine has been moved to an optional plugin
 * (io.github.ireaderorg.plugins.graalvm-engine) to reduce app size.
 * 
 * Users who need JavaScript-based sources (LNReader plugins) should install
 * the GraalVM Engine plugin from the Feature Store.
 * 
 * This stub implementation throws NoJSEngineException to prompt users
 * to install the required plugin.
 */
actual class JSEngine {
    
    actual fun initialize() {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'GraalVM JavaScript Engine' " +
            "plugin from the Feature Store to use JavaScript-based sources."
        )
    }
    
    actual fun evaluateScript(script: String): Any? {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'GraalVM JavaScript Engine' " +
            "plugin from the Feature Store."
        )
    }
    
    actual fun callFunction(name: String, vararg args: Any?): Any? {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'GraalVM JavaScript Engine' " +
            "plugin from the Feature Store."
        )
    }
    
    actual fun setGlobalObject(name: String, value: Any) {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'GraalVM JavaScript Engine' " +
            "plugin from the Feature Store."
        )
    }
    
    actual fun getGlobalObject(name: String): Any? {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'GraalVM JavaScript Engine' " +
            "plugin from the Feature Store."
        )
    }
    
    actual fun dispose() {
        // No-op - nothing to dispose
    }
    
    companion object {
        /**
         * Check if a JavaScript engine is available.
         * Returns false since GraalVM is no longer bundled.
         */
        fun isAvailable(): Boolean = false
    }
}
