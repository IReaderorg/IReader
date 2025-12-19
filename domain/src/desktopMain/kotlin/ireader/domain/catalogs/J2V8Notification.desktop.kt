package ireader.domain.catalogs

import ireader.domain.js.loader.GraalVMEngineHelper

/**
 * Desktop implementation - resets GraalVMEngineHelper to allow retry.
 */
actual fun onJ2V8PluginAvailable() {
    // On Desktop, we use GraalVM instead of J2V8
    GraalVMEngineHelper.onGraalVMPluginAvailable()
}
