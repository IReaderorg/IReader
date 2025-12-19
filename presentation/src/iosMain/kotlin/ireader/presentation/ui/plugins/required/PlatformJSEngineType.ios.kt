package ireader.presentation.ui.plugins.required

/**
 * iOS does not support plugins.
 * JS sources are not available on iOS.
 */
actual fun getPlatformJSEngineType(): RequiredPluginType = RequiredPluginType.NONE
