package ireader.domain.js.loader

import ireader.domain.js.bridge.JSBridgeService
import ireader.domain.js.bridge.LNReaderPlugin
import ireader.domain.js.engine.GraalVMJSEngine

/**
 * Desktop implementation using GraalVM.
 */
actual fun createEngine(bridgeService: JSBridgeService): JSEngine {
    return GraalVMEngineWrapper(GraalVMJSEngine(bridgeService))
}

/**
 * Wrapper to adapt GraalVMJSEngine to JSEngine interface.
 */
private class GraalVMEngineWrapper(private val engine: GraalVMJSEngine) : JSEngine {
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
