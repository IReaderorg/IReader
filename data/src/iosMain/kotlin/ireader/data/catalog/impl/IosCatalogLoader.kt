package ireader.data.catalog.impl

import ireader.core.http.HttpClients
import ireader.core.prefs.PreferenceStoreFactory
import ireader.core.source.LocalSource
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.models.entities.CatalogBundled
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.preferences.prefs.UiPreferences
import platform.JavaScriptCore.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import io.ktor.client.request.*
import io.ktor.client.statement.*

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
 */
@OptIn(ExperimentalForeignApi::class)
class IosCatalogLoader(
    private val httpClients: HttpClients,
    private val uiPreferences: UiPreferences,
    private val preferences: PreferenceStoreFactory
) : CatalogLoader {
    
    private val catalogPreferences = preferences.create("catalogs_data")
    private val installedSourcesPrefs = preferences.create("installed_sources")
    private val json = Json { ignoreUnknownKeys = true }
    
    // JavaScriptCore context for running JS sources
    private var jsContext: JSContext? = null
    private var runtimeLoaded = false
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    init {
        setupJSContext()
    }
    
    /**
     * Set up JavaScriptCore context with native bridges
     */
    private fun setupJSContext() {
        jsContext = JSContext().apply {
            // Set up exception handler
            exceptionHandler = { _, exception ->
                println("[JSCatalogLoader] JS Error: ${exception?.toString() ?: "Unknown"}")
            }
            
            // Set up console object for logging
            evaluateScript("""
                var console = {
                    log: function() {
                        // Logging handled by native
                    },
                    error: function() {
                        // Error logging handled by native
                    },
                    warn: function() {
                        // Warning logging handled by native
                    }
                };
            """)
        }
    }
    
    /**
     * Load the shared JS runtime (Kotlin stdlib + dependencies)
     */
    suspend fun loadRuntime() {
        if (runtimeLoaded) return
        
        try {
            val runtimeJs = loadBundledFile("runtime.js")
            jsContext?.evaluateScript(runtimeJs)
            runtimeLoaded = true
            println("[JSCatalogLoader] Runtime loaded successfully")
        } catch (e: Exception) {
            println("[JSCatalogLoader] Failed to load runtime: ${e.message}")
        }
    }
    
    /**
     * Load a JS source by ID
     */
    suspend fun loadSource(sourceId: String): Boolean {
        if (!runtimeLoaded) loadRuntime()
        
        return try {
            val sourceJs = downloadSourceFile(sourceId)
            if (sourceJs != null) {
                jsContext?.evaluateScript(sourceJs)
                jsContext?.exception == null
            } else {
                false
            }
        } catch (e: Exception) {
            println("[JSCatalogLoader] Failed to load source $sourceId: ${e.message}")
            false
        }
    }
    
    override suspend fun loadAll(): List<CatalogLocal> {
        val bundled = mutableListOf<CatalogLocal>(
            CatalogBundled(
                source = LocalSource(),
                description = "Read novels from local storage",
                name = "Local Source"
            )
        )
        
        // Load JS runtime
        loadRuntime()
        
        // Load installed JS sources
        val installedIds = getInstalledSourceIds()
        val jsSources = installedIds.mapNotNull { id ->
            if (loadSource(id)) {
                createJsSourceCatalog(id)
            } else null
        }
        
        return bundled + jsSources
    }
    
    private fun createJsSourceCatalog(sourceId: String): CatalogLocal? {
        // Get source info from JS context
        val infoJson = jsContext?.evaluateScript(
            "JSON.stringify(SourceBridge.getSourceInfo('$sourceId'))"
        )?.toString() ?: return null
        
        return try {
            // Parse source info and create catalog
            val sourceInfo = json.decodeFromString(JsSourceInfo.serializer(), infoJson)
            val source = JsSourceWrapper(sourceId, sourceInfo, this)
            
            CatalogBundled(
                source = source,
                description = "JS Source: ${sourceInfo.name}",
                name = sourceInfo.name
            )
        } catch (e: Exception) {
            println("[JSCatalogLoader] Failed to create catalog for $sourceId: ${e.message}")
            null
        }
    }
    
    // Bridge methods for JsSourceWrapper to call
    internal fun jsSearch(sourceId: String, query: String, page: Int): String {
        return jsContext?.evaluateScript(
            "JSON.stringify(SourceBridge.search('$sourceId', '${query.escapeJs()}', $page))"
        )?.toString() ?: "[]"
    }
    
    internal fun jsGetDetails(sourceId: String, bookJson: String): String {
        val escaped = bookJson.escapeJs()
        return jsContext?.evaluateScript(
            "JSON.stringify(SourceBridge.getBookDetails('$sourceId', '$escaped'))"
        )?.toString() ?: "{}"
    }
    
    internal fun jsGetChapters(sourceId: String, bookJson: String): String {
        val escaped = bookJson.escapeJs()
        return jsContext?.evaluateScript(
            "JSON.stringify(SourceBridge.getChapters('$sourceId', '$escaped'))"
        )?.toString() ?: "[]"
    }
    
    internal fun jsGetContent(sourceId: String, chapterJson: String): String {
        val escaped = chapterJson.escapeJs()
        return jsContext?.evaluateScript(
            "JSON.stringify(SourceBridge.getContent('$sourceId', '$escaped'))"
        )?.toString() ?: "[]"
    }
    
    // APK loading not supported on iOS
    override fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally? = null
    override fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide? = null
    
    private fun loadBundledFile(filename: String): String {
        val bundle = NSBundle.mainBundle
        val name = filename.substringBeforeLast(".")
        val ext = filename.substringAfterLast(".")
        val path = bundle.pathForResource(name, ext)
        
        return if (path != null) {
            NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as? String ?: ""
        } else {
            println("[JSCatalogLoader] Bundled file not found: $filename")
            ""
        }
    }
    
    private suspend fun downloadSourceFile(sourceId: String): String? {
        return try {
            val url = "https://sources.ireader.app/js/$sourceId.js"
            httpClients.default.get(url).bodyAsText()
        } catch (e: Exception) {
            println("[JSCatalogLoader] Failed to download source $sourceId: ${e.message}")
            null
        }
    }
    
    private fun getInstalledSourceIds(): List<String> {
        val idsString = installedSourcesPrefs.getString("ids", "").get()
        return if (idsString.isNotBlank()) {
            idsString.split(",").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    }
    
    fun addInstalledSource(sourceId: String) {
        val current = getInstalledSourceIds().toMutableList()
        if (sourceId !in current) {
            current.add(sourceId)
            installedSourcesPrefs.getString("ids", "").set(current.joinToString(","))
        }
    }
    
    fun removeInstalledSource(sourceId: String) {
        val current = getInstalledSourceIds().toMutableList()
        current.remove(sourceId)
        installedSourcesPrefs.getString("ids", "").set(current.joinToString(","))
    }
    
    private fun String.escapeJs(): String {
        return this
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
