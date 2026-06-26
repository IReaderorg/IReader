package eu.kanade.tachiyomi.source

/**
 * Minimal ConfigurableSource interface shim.
 * Some tsundoku extensions implement this to provide settings.
 */
interface ConfigurableSource {
    fun setupPreferenceScreen(screen: Any) {}
}
