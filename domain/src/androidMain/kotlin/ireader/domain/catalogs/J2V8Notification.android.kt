package ireader.domain.catalogs

import ireader.domain.js.loader.J2V8EngineHelper

/**
 * Android implementation - resets J2V8EngineHelper to allow retry.
 */
actual fun onJ2V8PluginAvailable() {
    J2V8EngineHelper.onJ2V8PluginAvailable()
}
