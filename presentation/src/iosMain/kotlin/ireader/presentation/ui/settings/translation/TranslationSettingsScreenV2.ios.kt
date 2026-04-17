package ireader.presentation.ui.settings.translation

/**
 * iOS implementation: Always return true since Gemini Nano is Android-specific
 * This prevents the warning banner from showing on iOS
 */
actual fun isAndroid14Plus(): Boolean {
    return true // Don't show Android version warning on iOS
}
