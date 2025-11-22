package ireader.domain.js.loader

import ireader.domain.js.bridge.JSBridgeService
import ireader.domain.js.bridge.LNReaderPlugin
import ireader.domain.js.engine.AndroidJSEngine

/**
 * Android implementation using QuickJS.
 */
actual fun createEngine(bridgeService: JSBridgeService): JSEngine {
    return AndroidJSEngineWrapper(bridgeService)
}

/**
 * Wrapper that adapts AndroidJSEngine to the JSEngine interface.
 */
private class AndroidJSEngineWrapper(
    private val bridgeService: JSBridgeService
) : JSEngine {
    
    private val engine = AndroidJSEngine(bridgeService)
    
    override suspend fun loadPlugin(jsCode: String, pluginId: String): LNReaderPlugin {
        return engine.loadPlugin(jsCode, pluginId)
    }
    
    override fun close() {
        engine.close()
    }
    
    override fun isLoaded(): Boolean {
        return engine.isLoaded()
    }
}
