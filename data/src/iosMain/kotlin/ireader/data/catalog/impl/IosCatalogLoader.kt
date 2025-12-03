package ireader.data.catalog.impl

import ireader.core.http.HttpClients
import ireader.core.prefs.PreferenceStoreFactory
import ireader.core.source.LocalSource
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.models.entities.CatalogBundled
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.preferences.prefs.UiPreferences

/**
 * iOS implementation of CatalogLoader
 * 
 * Based on the iOS Source Architecture document, this uses Kotlin/JS + JavaScriptCore
 * for loading sources dynamically while remaining App Store compliant.
 * 
 * Key points:
 * - iOS prohibits dynamic native code loading (no dlopen for App Store apps)
 * - JavaScript execution via JavaScriptCore is allowed
 * - Sources are compiled to JS and loaded at runtime
 * 
 * TODO: Implement full JS bridge using:
 * - platform.JavaScriptCore.JSContext
 * - platform.JavaScriptCore.JSValue
 * - Native HTTP bridge for Ktor requests
 */
class IosCatalogLoader(
    private val httpClients: HttpClients,
    private val uiPreferences: UiPreferences,
    private val preferences: PreferenceStoreFactory
) : CatalogLoader {
    
    private val catalogPreferences = preferences.create("catalogs_data")
    private val installedSourcesPrefs = preferences.create("installed_sources")
    
    // TODO: Initialize JavaScriptCore context
    // private val jsContext: JSContext = JSContext()
    // private var runtimeLoaded = false
    
    override suspend fun loadAll(): List<CatalogLocal> {
        val bundled = mutableListOf<CatalogBundled>()
        
        // Add Local Source for reading local novels
        val localSourceCatalog = CatalogBundled(
            source = LocalSource(),
            description = "Read novels from local storage",
            name = "Local Source"
        )
        bundled.add(localSourceCatalog)
        
        // TODO: Load JS runtime and installed JS sources
        // val jsSources = loadJsSources()
        
        return bundled
    }
    
    /**
     * Load the shared Kotlin/JS runtime
     * This contains stdlib, Ktor, Ksoup dependencies (~800KB-1.2MB)
     */
    private suspend fun loadRuntime() {
        // TODO: Implement
        // if (runtimeLoaded) return
        // val runtimeJs = loadBundledFile("runtime.js")
        // jsContext.evaluateScript(runtimeJs)
        // runtimeLoaded = true
    }
    
    /**
     * Load a specific JS source
     * Individual sources are ~10-30KB each
     */
    private suspend fun loadSource(sourceId: String): Boolean {
        // TODO: Implement
        // if (!runtimeLoaded) loadRuntime()
        // val sourceJs = downloadSourceFile(sourceId) ?: return false
        // jsContext.evaluateScript(sourceJs)
        // jsContext.evaluateScript("initSource(nativeHttpGet)")
        // return jsContext.exception == null
        return false
    }
    
    /**
     * APK/JAR loading not supported on iOS - use JS sources instead
     */
    override fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally? = null
    
    /**
     * System-wide catalogs not applicable on iOS
     */
    override fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide? = null
    
    /**
     * Get list of installed source IDs from preferences
     */
    private fun getInstalledSourceIds(): List<String> {
        return installedSourcesPrefs
            .getString("ids", "")
            .split(",")
            .filter { it.isNotBlank() }
    }
    
    /**
     * Save installed source ID to preferences
     */
    private fun saveInstalledSourceId(sourceId: String) {
        val current = getInstalledSourceIds().toMutableSet()
        current.add(sourceId)
        installedSourcesPrefs.putString("ids", current.joinToString(","))
    }
    
    /**
     * Remove installed source ID from preferences
     */
    private fun removeInstalledSourceId(sourceId: String) {
        val current = getInstalledSourceIds().toMutableSet()
        current.remove(sourceId)
        installedSourcesPrefs.putString("ids", current.joinToString(","))
    }
}
