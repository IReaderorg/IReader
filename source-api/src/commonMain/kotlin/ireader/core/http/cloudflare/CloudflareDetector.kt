package ireader.core.http.cloudflare

import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

/**
 * Detects Cloudflare protection and challenge types from HTTP responses.
 */
object CloudflareDetector {
    
    // Cloudflare-specific headers
    private val CF_HEADERS = listOf(
        "cf-ray",
        "cf-cache-status",
        "cf-request-id"
    )
    
    // Patterns indicating JS challenge
    private val JS_CHALLENGE_PATTERNS = listOf(
        "Just a moment",
        "Checking your browser",
        "cf-browser-verification",
        "_cf_chl_opt",
        "cf_chl_prog",
        "challenge-platform"
    )
    
    // Patterns indicating Turnstile
    private val TURNSTILE_PATTERNS = listOf(
        "challenges.cloudflare.com/turnstile",
        "cf-turnstile",
        "turnstile.js"
    )
    
    // Patterns indicating CAPTCHA
    private val CAPTCHA_PATTERNS = listOf(
        "cf-captcha-container",
        "g-recaptcha",
        "h-captcha"
    )
    
    // Patterns indicating managed challenge
    private val MANAGED_CHALLENGE_PATTERNS = listOf(
        "managed_checking_msg",
        "cf-please-wait",
        "Verifying you are human"
    )
    
    // Patterns indicating block
    private val BLOCK_PATTERNS = listOf(
        "cf-error-details",
        "Access denied",
        "Sorry, you have been blocked",
        "You have been blocked",
        "Error 1020"
    )
    
    // Patterns indicating rate limit
    private val RATE_LIMIT_PATTERNS = listOf(
        "Error 1015",
        "You are being rate limited"
    )
    
    /**
     * Detect Cloudflare challenge from HTTP response.
     * 
     * @param response HTTP response
     * @param body Response body text
     * @return Detected challenge type
     */
    fun detect(response: HttpResponse, body: String): CloudflareChallenge {
        val statusCode = response.status.value
        val rayId = extractRayId(response)
        
        // Check if this is even a Cloudflare response
        if (!isCloudflareResponse(response, body)) {
            return CloudflareChallenge.None
        }
        
        // Check for rate limiting (429)
        if (statusCode == HttpStatusCode.TooManyRequests.value) {
            val retryAfter = extractRetryAfter(response)
            return CloudflareChallenge.RateLimited(retryAfter, rayId)
        }
        
        // Check for IP block (403 with block message)
        if (statusCode == HttpStatusCode.Forbidden.value) {
            if (BLOCK_PATTERNS.any { body.contains(it, ignoreCase = true) }) {
                return CloudflareChallenge.BlockedIP(rayId)
            }
        }
        
        // Check for Turnstile challenge
        if (TURNSTILE_PATTERNS.any { body.contains(it, ignoreCase = true) }) {
            val siteKey = extractTurnstileSiteKey(body) ?: ""
            return CloudflareChallenge.TurnstileChallenge(siteKey, rayId)
        }
        
        // Check for CAPTCHA challenge
        if (CAPTCHA_PATTERNS.any { body.contains(it, ignoreCase = true) }) {
            val siteKey = extractCaptchaSiteKey(body) ?: ""
            return CloudflareChallenge.CaptchaChallenge(siteKey, rayId)
        }
        
        // Check for managed challenge
        if (MANAGED_CHALLENGE_PATTERNS.any { body.contains(it, ignoreCase = true) }) {
            return CloudflareChallenge.ManagedChallenge(rayId)
        }
        
        // Check for JS challenge (most common)
        if (statusCode == HttpStatusCode.ServiceUnavailable.value || 
            statusCode == HttpStatusCode.Forbidden.value) {
            if (JS_CHALLENGE_PATTERNS.any { body.contains(it, ignoreCase = true) }) {
                return CloudflareChallenge.JSChallenge(rayId)
            }
        }
        
        // Check for rate limit patterns in body
        if (RATE_LIMIT_PATTERNS.any { body.contains(it, ignoreCase = true) }) {
            return CloudflareChallenge.RateLimited(null, rayId)
        }
        
        // If we have Cloudflare headers but couldn't identify the challenge
        if (rayId != null && (statusCode == 403 || statusCode == 503)) {
            return CloudflareChallenge.Unknown(
                rayId = rayId,
                hints = listOf("Cloudflare response with unknown challenge type")
            )
        }
        
        return CloudflareChallenge.None
    }
    
    /**
     * Quick check if response is likely a Cloudflare challenge.
     * Use this for fast filtering before full detection.
     */
    fun isChallengeLikely(response: HttpResponse): Boolean {
        return mightBeCloudflare(response)
    }
    
    /**
     * Quick check if response might be Cloudflare protected.
     * Use this for fast filtering before full detection.
     */
    fun mightBeCloudflare(response: HttpResponse): Boolean {
        val statusCode = response.status.value
        
        // Quick status code check
        if (statusCode !in listOf(403, 429, 503, 520, 521, 522, 523, 524)) {
            return false
        }
        
        // Check for Cloudflare headers
        return CF_HEADERS.any { header ->
            response.headers[header] != null
        } || response.headers["server"]?.contains("cloudflare", ignoreCase = true) == true
    }
    
    /**
     * Check if response is from Cloudflare.
     */
    private fun isCloudflareResponse(response: HttpResponse, body: String): Boolean {
        // Check headers
        val hasCloudflareHeaders = CF_HEADERS.any { header ->
            response.headers[header] != null
        }
        
        val serverIsCloudflare = response.headers["server"]
            ?.contains("cloudflare", ignoreCase = true) == true
        
        // Check body for Cloudflare indicators
        val hasCloudflareBody = body.contains("cloudflare", ignoreCase = true) ||
                body.contains("cf-browser-verification", ignoreCase = true) ||
                body.contains("__cf_", ignoreCase = true)
        
        return hasCloudflareHeaders || serverIsCloudflare || hasCloudflareBody
    }
    
    /**
     * Extract CF-Ray ID from response headers.
     */
    private fun extractRayId(response: HttpResponse): String? {
        return response.headers["cf-ray"]
    }
    
    /**
     * Extract Retry-After header value in seconds.
     */
    private fun extractRetryAfter(response: HttpResponse): Long? {
        return response.headers["retry-after"]?.toLongOrNull()
    }
    
    /**
     * Extract Turnstile site key from page body.
     */
    private fun extractTurnstileSiteKey(body: String): String? {
        val patterns = listOf(
            Regex("""sitekey['":\s]+['"]([^'"]+)['"]"""),
            Regex("""data-sitekey=['"]([^'"]+)['"]"""),
            Regex("""turnstileSiteKey['":\s]+['"]([^'"]+)['"]""")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                return match.groupValues.getOrNull(1)
            }
        }
        return null
    }
    
    /**
     * Extract CAPTCHA site key from page body.
     */
    private fun extractCaptchaSiteKey(body: String): String? {
        val patterns = listOf(
            Regex("""data-sitekey=['"]([^'"]+)['"]"""),
            Regex("""sitekey['":\s]+['"]([^'"]+)['"]""")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(body)
            if (match != null) {
                return match.groupValues.getOrNull(1)
            }
        }
        return null
    }
}
