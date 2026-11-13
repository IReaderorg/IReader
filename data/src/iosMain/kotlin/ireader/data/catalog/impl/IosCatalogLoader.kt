package ireader.data.catalog.impl

import ireader.core.http.HttpClients
import ireader.core.prefs.PreferenceStoreFactory
import ireader.core.source.LocalSource
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.models.entities.CatalogBundled
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import okio.FileSystem
import ireader.domain.preferences.prefs.UiPreferences
import platform.JavaScriptCore.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import ireader.data.catalog.js.JsRuntimeHost
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
@OptIn(ExperimentalForeignApi::class, DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class IosCatalogLoader(
    private val httpClients: HttpClients,
    private val uiPreferences: UiPreferences,
    private val preferences: PreferenceStoreFactory,
    private val communitySource: ireader.domain.community.CommunitySource? = null
) : CatalogLoader {
    
    private val catalogPreferences = preferences.create("catalogs_data")
    private val installedSourcesPrefs = preferences.create("installed_sources")
    private val json = Json { ignoreUnknownKeys = true }
    
    // JavaScriptCore context for running JS sources. Touched only from jsDispatcher.
    private var jsContext: JSContext? = null
    private var runtimeLoaded = false
    private val jsDispatcher = newSingleThreadContext("ireader-js")
    private val jsMutex = Mutex()
    private val pendingBundles = mutableListOf<PendingJsBundle>()
    private val loadedBundles = mutableSetOf<String>()
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private suspend fun <T> onJs(block: suspend () -> T): T {
        return jsMutex.withLock {
            withContext(jsDispatcher) {
                ensureContext()
                block()
            }
        }
    }

    /**
     * Set up JavaScriptCore context with native bridges
     */
    private fun ensureContext() {
        if (jsContext != null) return
        jsContext = JSContext().apply {
            exceptionHandler = { _, exception ->
                println("[JSCatalogLoader] JS Error: ${exception?.toString() ?: "Unknown"}")
            }
            evaluateScript("""
                var console = {
                    log: function() {},
                    error: function() {},
                    warn: function() {}
                };
            """)
            evaluateScript(JsRuntimeHost.prelude())
        }
    }
    
    /**
     * Load the shared JS runtime (Kotlin stdlib + dependencies)
     */
    suspend fun loadRuntime() {
        if (runtimeLoaded) return
        onJs {
            if (runtimeLoaded) return@onJs
            try {
                val runtimeJs = loadBundledFile("runtime.js")
                if (runtimeJs.isNotBlank()) jsContext?.evaluateScript(runtimeJs)
                runtimeLoaded = true
            } catch (e: Exception) {
                println("[JSCatalogLoader] Failed to load runtime: ${e.message}")
            }
        }
    }
    
    /**
     * Load a JS source by ID
     */
    suspend fun loadSource(sourceId: String): Boolean {
        if (!runtimeLoaded) loadRuntime()

        return try {
            val sourceJs = downloadSourceFile(sourceId) ?: return false
            onJs {
                jsContext?.evaluateScript(sourceJs)
                jsContext?.exception == null
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
        
        // Add Community Source for community-translated content
        communitySource?.let { source ->
            val communityCatalog = ireader.domain.models.entities.CommunityCatalog(
                source = source,
                description = "Browse and read community-translated novels"
            )
            bundled.add(communityCatalog)
        }
        
        // Load JS runtime
        loadRuntime()

        val installed = loadInstalledJsCatalogs()
        return bundled + installed
    }

    override fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally? {
        val fileSystem = FileSystem.SYSTEM
        val dir = iosJsPluginsDir()
        val metaPath = dir / "$pkgName.meta.json"
        if (!fileSystem.exists(metaPath)) return null
        return try {
            val meta = json.decodeFromString<InstalledJsSourceMeta>(
                fileSystem.read(metaPath) { readUtf8() }
            )
            val source = freeWebNovelSourceOrNull(pkgName, meta.name, meta.id, httpClients)
                ?: novgoSourceOrNull(pkgName, meta.id, httpClients)
                ?: run {
                    rememberBundle(pkgName, meta)
                    JsSourceWrapper(
                        sourceId = pkgName,
                        info = JsSourceInfo(id = meta.id, name = meta.name, lang = meta.lang),
                        loader = this,
                    )
                }
            CatalogInstalled.Locally(
                name = meta.name,
                description = meta.description.ifBlank { "JavaScript source" },
                source = source,
                pkgName = pkgName,
                versionName = meta.version,
                versionCode = meta.code,
                nsfw = meta.nsfw,
                installDir = dir,
                iconUrl = meta.icon,
            )
        } catch (e: Exception) {
            println("[JSCatalogLoader] Failed to read $pkgName: ${e.message}")
            null
        }
    }

    private fun loadInstalledJsCatalogs(): List<CatalogLocal> {
        val fileSystem = FileSystem.SYSTEM
        val dir = iosJsPluginsDir()
        if (!fileSystem.exists(dir)) return emptyList()
        return fileSystem.list(dir)
            .map { it.name }
            .filter { it.endsWith(".meta.json") }
            .mapNotNull { loadLocalCatalog(it.removeSuffix(".meta.json")) }
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
    
    internal fun rememberBundle(pkgName: String, meta: InstalledJsSourceMeta) {
        pendingBundles.add(PendingJsBundle(pkgName, meta))
    }

    // Bridge methods for JsSourceWrapper to call. They wait until the Promise settles.
    internal suspend fun jsSearch(sourceId: String, query: String, page: Int): String = onJs {
        installPendingBundles()
        awaitJs(
            JsRuntimeHost.searchCall(sourceId, JsRuntimeHost.registryId(sourceId, sourceId), query, page),
            "[]",
        )
    }

    internal suspend fun jsGetDetails(sourceId: String, bookJson: String): String = onJs {
        installPendingBundles()
        awaitJs(
            JsRuntimeHost.detailsCall(sourceId, JsRuntimeHost.registryId(sourceId, sourceId), bookJson),
            "{}",
        )
    }

    internal suspend fun jsGetChapters(sourceId: String, bookJson: String): String = onJs {
        installPendingBundles()
        awaitJs(
            JsRuntimeHost.chaptersCall(sourceId, JsRuntimeHost.registryId(sourceId, sourceId), bookJson),
            "[]",
        )
    }

    internal suspend fun jsGetContent(sourceId: String, chapterJson: String): String = onJs {
        installPendingBundles()
        awaitJs(
            JsRuntimeHost.contentCall(sourceId, JsRuntimeHost.registryId(sourceId, sourceId), chapterJson),
            "[]",
        )
    }

    private fun installPendingBundles() {
        val ctx = jsContext ?: return
        val pending = pendingBundles.toList()
        pendingBundles.clear()
        for (bundle in pending) {
            val meta = bundle.meta
            if (meta.fileName.isBlank() || meta.fileName in loadedBundles) continue
            val path = iosJsPluginsDir() / meta.fileName
            val fileSystem = FileSystem.SYSTEM
            if (!fileSystem.exists(path)) continue
            val script = fileSystem.read(path) { readUtf8() }
            if (script.isBlank()) continue
            ctx.evaluateScript(script)
            val init = meta.initFunction.ifBlank { defaultInit(bundle.pkgName, meta) }
            if (init.isNotBlank()) ctx.evaluateScript(JsRuntimeHost.registryShim(init))
            loadedBundles.add(meta.fileName)
        }
    }

    private fun defaultInit(pkgName: String, meta: InstalledJsSourceMeta): String {
        val haystack = "$pkgName ${meta.name} ${meta.fileName}".lowercase()
        return if ("freewebnovel" in haystack) "initFreeWebNovel" else ""
    }

    private suspend fun awaitJs(call: String, fallback: String): String {
        val ctx = jsContext ?: return fallback
        ctx.evaluateScript(JsRuntimeHost.resetResult())
        ctx.evaluateScript(call)
        repeat(600) {
            pump(ctx)
            val raw = ctx.evaluateScript(JsRuntimeHost.readResult())?.toString()
            if (!raw.isNullOrBlank() && raw != "undefined" && raw != "null") return raw
            delay(50)
        }
        return fallback
    }

    private suspend fun pump(ctx: JSContext) {
        repeat(8) {
            ctx.evaluateScript(JsRuntimeHost.drainTimers())
            val queueJson = ctx.evaluateScript(JsRuntimeHost.readFetchQueue())?.toString() ?: "[]"
            if (queueJson.isBlank() || queueJson == "[]" || queueJson == "undefined" || queueJson == "null") return
            ctx.evaluateScript(JsRuntimeHost.clearFetchQueue())
            val requests = JsRuntimeHost.parseFetchQueue(queueJson)
            if (requests.isEmpty()) return
            for (request in requests) {
                val response = performFetch(request)
                ctx.evaluateScript(
                    JsRuntimeHost.deliverFetch(
                        id = request.id,
                        status = response.status,
                        url = response.url,
                        body = response.body,
                        headers = response.headers,
                    )
                )
            }
        }
    }

    private suspend fun performFetch(request: ireader.data.catalog.js.JsFetchRequest): JsHttpResponse {
        return try {
            val response = httpClients.default.request(request.url) {
                method = HttpMethod.parse(request.method.ifBlank { "GET" }.uppercase())
                request.headers.forEach { (name, value) -> header(name, value) }
                val hasUserAgent = request.headers.keys.any { it.equals(HttpHeaders.UserAgent, ignoreCase = true) }
                if (!hasUserAgent) header(HttpHeaders.UserAgent, USER_AGENT)
                if (request.body.isNotEmpty()) setBody(request.body)
            }
            val headers = buildMap {
                response.headers.forEach { name, values -> put(name, values.joinToString(",")) }
            }
            JsHttpResponse(response.status.value, response.bodyAsText(), headers, request.url)
        } catch (e: Exception) {
            println("[JSCatalogLoader] fetch ${request.url}: ${e.message}")
            JsHttpResponse(0, "", emptyMap(), request.url)
        }
    }
    
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
    
    private companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.91 Mobile Safari/537.36"
    }
}

private data class PendingJsBundle(
    val pkgName: String,
    val meta: InstalledJsSourceMeta,
)

private data class JsHttpResponse(
    val status: Int,
    val body: String,
    val headers: Map<String, String>,
    val url: String,
)
