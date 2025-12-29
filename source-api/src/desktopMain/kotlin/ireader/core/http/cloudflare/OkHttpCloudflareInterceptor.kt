package ireader.core.http.cloudflare

import ireader.core.http.CloudflareBypassHandler
import ireader.core.log.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that automatically handles Cloudflare challenges.
 * Uses CloudflareBypassHandler to solve challenges with FlareSolverr auto-start support.
 */
class OkHttpCloudflareInterceptor(
    private val bypassHandler: CloudflareBypassHandler
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()
        val domain = extractDomain(url)
        
        // Add cached cookies if available
        val cached = bypassHandler.getCachedCookies(domain)
        val request = if (cached != null && !cached.isExpired()) {
            originalRequest.newBuilder()
                .header("User-Agent", cached.userAgent)
                .header("Cookie", buildCookieHeader(cached))
                .build()
        } else {
            originalRequest
        }
        
        // Make the request
        val response = chain.proceed(request)
        
        // Check if Cloudflare challenge
        if (isCloudflareChallenge(response)) {
            Log.debug { "[OkHttpCloudflareInterceptor] Cloudflare challenge detected for $url" }
            
            // Try to bypass
            val bypassResult = runBlocking { bypassHandler.bypass(url) }
            if (bypassResult != null) {
                Log.debug { "[OkHttpCloudflareInterceptor] Bypass successful, retrying request" }
                
                // Close the original response
                response.close()
                
                // Retry with new cookies
                val retryRequest = originalRequest.newBuilder()
                    .header("User-Agent", bypassResult.userAgent)
                    .header("Cookie", buildCookieHeader(bypassResult))
                    .build()
                
                return chain.proceed(retryRequest)
            } else {
                Log.debug { "[OkHttpCloudflareInterceptor] Bypass failed" }
            }
        }
        
        return response
    }
    
    private fun isCloudflareChallenge(response: Response): Boolean {
        val code = response.code
        if (code != 403 && code != 503) return false
        
        // Check headers for Cloudflare indicators
        val server = response.header("Server")
        val cfRay = response.header("CF-RAY")
        
        if (server?.contains("cloudflare", ignoreCase = true) == true || cfRay != null) {
            // Peek at body to confirm (without consuming it)
            val body = response.peekBody(10000).string()
            return body.contains("Just a moment", ignoreCase = true) ||
                   body.contains("cf-browser-verification", ignoreCase = true) ||
                   body.contains("Checking your browser", ignoreCase = true)
        }
        
        return false
    }
    
    private fun buildCookieHeader(cookies: CloudflareBypassHandler.CookieData): String {
        val parts = mutableListOf("cf_clearance=${cookies.cfClearance}")
        cookies.cfBm?.let { parts.add("__cf_bm=$it") }
        return parts.joinToString("; ")
    }
    
    private fun buildCookieHeader(result: CloudflareBypassHandler.BypassResult): String {
        val parts = mutableListOf("cf_clearance=${result.cfClearance}")
        result.cfBm?.let { parts.add("__cf_bm=$it") }
        return parts.joinToString("; ")
    }
    
    private fun extractDomain(url: String): String {
        return url.lowercase()
            .removePrefix("http://")
            .removePrefix("https://")
            .removePrefix("www.")
            .substringBefore("/")
            .substringBefore(":")
    }
}
