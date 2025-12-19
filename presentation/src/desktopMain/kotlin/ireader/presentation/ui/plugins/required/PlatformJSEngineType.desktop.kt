package ireader.presentation.ui.plugins.required

/**
 * Desktop uses GraalVM JavaScript engine.
 */
actual fun getPlatformJSEngineType(): RequiredPluginType = RequiredPluginType.GRAALVM_ENGINE
