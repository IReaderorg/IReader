package ireader.presentation.ui.plugins.required

/**
 * iOS uses J2V8 JavaScript engine (same as Android for now).
 * TODO: May need a different engine for iOS in the future.
 */
actual fun getPlatformJSEngineType(): RequiredPluginType = RequiredPluginType.JS_ENGINE
