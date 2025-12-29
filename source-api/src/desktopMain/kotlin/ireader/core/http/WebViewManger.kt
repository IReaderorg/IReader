package ireader.core.http

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ireader.core.log.Log
import ireader.core.util.createICoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Desktop implementation of WebViewManager
 * 
 * Uses FlareSolverr when available for full browser capabilities,
 * falls back to basic HTTP fetch for simple pages.
 * 
 * To enable full browser capabilities on desktop, install FlareSolverr:
 * - Docker: docker run -d -p 8191:8191 ghcr.io/flaresolverr/flaresolverr:latest
 * - Or download from: https://github.com/FlareSolverr/FlareSolverr/releases
 */
actual class WebViewManger {
    actual var isInit: Boolean = false
    actual var userAgent: String = DEFAULT_USER_AGENT
    actual var selector: String? = null
    actual var html: Document? = null
    actual var webUrl: String? = null
    actual var inProgress: Boolean = false
    
    private var onContentReady: ((String) -> Unit)? = null
    private var flareSolverrUrl: String = "http://localhost:8191/v1"
    private var flareSolverrAvailable: Boolean? = null
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private val httpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                config {
                    followRedirects(true)
                    followSslRedirects(true)
                }
            }
        }
    }
    
    private val scope = createICoroutineScope(Dispatchers.IO)

    actual fun init(): Any {
        isInit = true
        Log.debug { "[DesktopWebViewManager] Initialized" }
        return 0
    }
    
    /**
     * Configure FlareSolverr endpoint
     */
    fun configureFlareSolverr(url: String) {
        flareSolverrUrl = url
        flareSolverrAvailable = null
    }

    actual fun update() {
        // No-op on desktop - state is managed internally
    }

    actual fun destroy() {
        isInit = false
        inProgress = false
        html = null
        webUrl = null
        selector = null
        onContentReady = null
        Log.debug { "[DesktopWebViewManager] Destroyed" }
    }
    
    actual fun loadInBackground(url: String, selector: String?, onReady: (String) -> Unit) {
        this.webUrl = url
        this.selector = selector
        this.onContentReady = onReady
        this.inProgress = true
        
        if (!isInit) {
            init()
        }
        
        scope.launch {
            try {
                val content = fetchContent(url, selector)
                html = if (content.isNotEmpty()) Ksoup.parse(content) else null
                inProgress = false
                onReady(content)
            } catch (e: Exception) {
                Log.error(e, "[DesktopWebViewManager] Failed to load: $url")
                inProgress = false
                onReady("")
            }
        }
    }
    
    private suspend fun fetchContent(url: String, selector: String?): String {
        // Try FlareSolverr first
        if (checkFlareSolverrAvailable()) {
            val result = fetchWithFlareSolverr(url)
            if (result.isNotEmpty()) {
                return result
            }
        }
        
        // Fall back to basic HTTP
        return fetchWithHttpClient(url)
    }
    
    private suspend fun checkFlareSolverrAvailable(): Boolean {
        if (flareSolverrAvailable != null) return flareSolverrAvailable!!
        
        return try {
            val response = httpClient.post(flareSolverrUrl) {
                contentType(ContentType.Application.Json)
                setBody("""{"cmd":"sessions.list"}""")
            }
            flareSolverrAvailable = response.status.value in 200..299
            if (flareSolverrAvailable == true) {
                Log.info { "[DesktopWebViewManager] FlareSolverr available" }
            }
            flareSolverrAvailable!!
        } catch (e: Exception) {
            flareSolverrAvailable = false
            false
        }
    }
    
    private suspend fun fetchWithFlareSolverr(url: String): String {
        try {
            val request = WebViewFlareSolverrRequest(
                cmd = "request.get",
                url = url,
                maxTimeout = 60000
            )
            
            val response = httpClient.post(flareSolverrUrl) {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(WebViewFlareSolverrRequest.serializer(), request))
            }
            
            val responseText = response.bodyAsText()
            val solverrResponse = json.decodeFromString(WebViewFlareSolverrResponse.serializer(), responseText)
            
            if (solverrResponse.status == "ok" && solverrResponse.solution != null) {
                Log.debug { "[DesktopWebViewManager] FlareSolverr success for: $url" }
                return solverrResponse.solution.response ?: ""
            }
        } catch (e: Exception) {
            Log.warn { "[DesktopWebViewManager] FlareSolverr failed: ${e.message}" }
            flareSolverrAvailable = false
        }
        return ""
    }
    
    private suspend fun fetchWithHttpClient(url: String): String {
        return try {
            val response = httpClient.get(url) {
                headers {
                    append("User-Agent", userAgent)
                }
            }
            response.bodyAsText()
        } catch (e: Exception) {
            Log.error(e, "[DesktopWebViewManager] HTTP fetch failed")
            ""
        }
    }
    
    actual fun isProcessingInBackground(): Boolean = inProgress
    
    actual fun isAvailable(): Boolean {
        // Desktop WebView is available with limited capabilities
        // Full capabilities require FlareSolverr
        return true
    }
}

// Internal DTOs for FlareSolverr
@Serializable
private data class WebViewFlareSolverrRequest(
    val cmd: String,
    val url: String,
    val maxTimeout: Int = 60000
)

@Serializable
private data class WebViewFlareSolverrResponse(
    val status: String = "error",
    val message: String = "",
    val solution: WebViewFlareSolverrSolution? = null
)

@Serializable
private data class WebViewFlareSolverrSolution(
    val url: String,
    val status: Int,
    val response: String? = null
)
