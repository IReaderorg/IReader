package ireader.domain.js.loader

import ireader.domain.js.bridge.JSBridgeService
import ireader.domain.js.bridge.LNReaderPlugin
import ireader.domain.js.engine.NoJSEngineException

/**
 * Desktop implementation.
 * 
 * NOTE: GraalVM JavaScript engine has been moved to an optional plugin
 * (io.github.ireaderorg.plugins.graalvm-engine) to reduce app size.
 * 
 * This stub throws NoJSEngineException to prompt users to install the plugin.
 */
actual fun createEngine(bridgeService: JSBridgeService): JSEngine {
    return StubJSEngine()
}

/**
 * Stub engine that throws NoJSEngineException.
 * Users need to install the GraalVM Engine plugin from Feature Store.
 */
private class StubJSEngine : JSEngine {
    override suspend fun loadPlugin(jsCode: String, pluginId: String): LNReaderPlugin {
        throw NoJSEngineException(
            "JavaScript engine not available. Please install the 'GraalVM JavaScript Engine' " +
            "plugin from the Feature Store to use JavaScript-based sources."
        )
    }
    
    override fun close() {
        // No-op
    }
    
    override fun isLoaded(): Boolean = false
}
