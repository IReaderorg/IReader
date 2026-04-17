package ireader.presentation.ui.settings.translation

/**
 * Desktop implementation: Always return true since Gemini Nano is Android-specific
 * This prevents the warning banner from showing on Desktop
 */
actual fun isAndroid14Plus(): Boolean {
    return true // Don't show Android version warning on Desktop
}
