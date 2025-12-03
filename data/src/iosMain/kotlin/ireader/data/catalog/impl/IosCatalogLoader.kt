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
            
            // Expose native HTTP GET to JavaScript
            val httpGet: @convention(block) (String, JSValue) -> Unit = { url, callback ->
                scope.launch {
                    try {
                        val response = httpClients.default.get(url).bodyAsText()
                        withContext(Dispatchers.Main) {
                            callback.callWithArguments(listOf(null, response))
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            callback.callWithArguments(listOf(e.message, null))
                        }
                    }
                }
            }
            setObject(httpGet, forKeyedSubscript = "nativeHttpGet" as NSString)
            
            // Expose native HTTP POST to JavaScript
            val httpPost: @convention(block) (String, String, JSValue) -> Unit = { url, body, callback ->
                scope.launch {
                    try {
                        val response = httpClients.default.post(url) {
                            setBody(body)
                        }.bodyAsText()
                        withContext(Dispatchers.Main) {
                            callback.callWithArguments(listOf(null, response))
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            callback.callWithArguments(listOf(e.message, null))
                        }
                    }
                }
            }
            setObject(httpPost, forKeyedSubscript = "nativeHttpPost" as NSString)
            
            // Expose logging
            val log: @convention(block) (String) -> Unit = { message ->
                println("[JS Source] $message")
            }
            setObject(log, forKeyedSubscript = "nativeLog" as NSString)
            
            // Set up console object
            evaluateScript("""
                var console = {
                    log: function() {
                        var args = Array.prototype.slice.call(arguments);
                        nativeLog(args.map(function(a) { return String(a); }).join(' '));
                    },
                    error: function() {
                        var args = Array.prototype.slice.call(arguments);
                        nativeLog('[ERROR] ' + args.map(function(a) { return String(a); }).join(' '));
                    },
                    warn: function() {
                        var args = Array.prototype.slice.call(arguments);
                        nativeLog('[WARN] ' + args.map(function(a) { return String(a); }).join(' '));
                    }
                };
            """)
        }
    }
    
    override suspend fun loadAll(): List<CatalogLocal> {
        val bundled = mutableListOf<CatalogBundled>()
        
        // Add Local Source for reading local novels
        val localSourceCatalog = CatalogBundled(
            source = LocalSource(),
            description = "Read novels from local storage",
            name = "Local Source"
        )
        bundled.add(localSourceCatalog)
        
        // Load JS runtime
        loadRuntime()
        
        // Load installed JS sources
        val installedIds = getInstalledSourceIds()
        val jsSources = installedIds.mapNotNull { sourceId ->
            if (loadSource(sourceId)) {
                createJsSourceCatalog(sourceId)
            } else null
        }
        
        return bundled + jsSources
    }
    
    /**
     * Load the shared Kotlin/JS runtime
     * This contains stdlib, Ktor, Ksoup dependencies (~800KB-1.2MB)
     */
    private suspend fun loadRuntime() {
        if (runtimeLoaded) return
        
        val runtimeJs = loadBundledFile("runtime.js")
        if (runtimeJs != null) {
            jsContext?.evaluateScript(runtimeJs)
            runtimeLoaded = true
            println("[JSCatalogLoader] Runtime loaded successfully")
        } else {
            println("[JSCatalogLoader] Failed to load runtime.js")
        }
    }
    
    /**
     * Load a specific JS source
     * Individual sources are ~10-30KB each
     */
    private suspend fun loadSource(sourceId: String): Boolean {
        if (!runtimeLoaded) loadRuntime()
        
        val sourceJs = downloadSourceFile(sourceId) ?: return false
        jsContext?.evaluateScript(sourceJs)
        jsContext?.evaluateScript("initSource(nativeHttpGet)")
        
        return jsContext?.exception == null
    }
    
    /**
     * Create a catalog entry for a JS source
     */
    private fun createJsSourceCatalog(sourceId: String): CatalogLocal? {
        val ctx = jsContext ?: return null
        
        val infoJson = ctx.evaluateScript(
            "SourceBridge.getSourceInfo('$sourceId')"
        )?.toString() ?: return null
        
        // Parse source info and create catalog
        // This would create a JsSourceWrapper that delegates to JS
        return null // Placeholder - full implementation would create JsSourceCatalog
    }
    
    /**
     * Load a bundled JS file from app resources
     */
    private fun loadBundledFile(filename: String): String? {
        val bundle = NSBundle.mainBundle
        val name = filename.substringBeforeLast(".")
        val ext = filename.substringAfterLast(".")
        val path = bundle.pathForResource(name, ext) ?: return null
        
        return try {
            NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as? String
        } catch (e: Exception) {
            println("[JSCatalogLoader] Error loading $filename: ${e.message}")
            null
        }
    }
    
    /**
     * Download a source JS file from CDN
     */
    private suspend fun downloadSourceFile(sourceId: String): String? {
        return try {
            val url = "https://sources.ireader.app/js/$sourceId.js"
            httpClients.default.get(url).bodyAsText()
        } catch (e: Exception) {
            println("[JSCatalogLoader] Error downloading $sourceId: ${e.message}")
            null
        }
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
    fun saveInstalledSourceId(sourceId: String) {
        val current = getInstalledSourceIds().toMutableSet()
        current.add(sourceId)
        installedSourcesPrefs.putString("ids", current.joinToString(","))
    }
    
    /**
     * Remove installed source ID from preferences
     */
    fun removeInstalledSourceId(sourceId: String) {
        val current = getInstalledSourceIds().toMutableSet()
        current.remove(sourceId)
        installedSourcesPrefs.putString("ids", current.joinToString(","))
    }
    
    /**
     * Install a new JS source
     */
    suspend fun installSource(sourceId: String): Boolean {
        val success = loadSource(sourceId)
        if (success) {
            saveInstalledSourceId(sourceId)
        }
        return success
    }
    
    /**
     * Uninstall a JS source
     */
    fun uninstallSource(sourceId: String) {
        removeInstalledSourceId(sourceId)
        // Note: The source will still be in memory until app restart
    }
    
    /**
     * Get available sources from CDN
     */
    suspend fun getAvailableSources(): List<SourceInfo> {
        return try {
            val response = httpClients.default.get("https://sources.ireader.app/index.json").bodyAsText()
            // Parse JSON response
            emptyList() // Placeholder
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        scope.cancel()
        jsContext = null
        runtimeLoaded = false
    }
}

/**
 * Source info from CDN
 */
data class SourceInfo(
    val id: String,
    val name: String,
    val lang: String,
    val version: String,
    val baseUrl: String
)

// Extension for HTTP client
private suspend fun io.ktor.client.HttpClient.get(url: String): io.ktor.client.statement.HttpResponse {
    return this.request(url) {
        method = io.ktor.http.HttpMethod.Get
    }
}

private suspend fun io.ktor.client.HttpClient.post(url: String, block: io.ktor.client.request.HttpRequestBuilder.() -> Unit): io.ktor.client.statement.HttpResponse {
    return this.request(url) {
        method = io.ktor.http.HttpMethod.Post
        block()
    }
}

private suspend fun io.ktor.client.statement.HttpResponse.bodyAsText(): String {
    return io.ktor.client.statement.bodyAsText()
}
