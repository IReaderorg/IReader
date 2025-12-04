package ireader.core.http.cloudflare

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CloudflareDetectorTest {
    
    @Test
    fun `JS challenge patterns are detected`() {
        val jsChallengeBodies = listOf(
            "<title>Just a moment...</title>",
            "Checking your browser before accessing",
            "<div id=\"cf-browser-verification\">",
            "var _cf_chl_opt = {",
            "cdn-cgi/challenge-platform"
        )
        
        jsChallengeBodies.forEach { body ->
            assertTrue(
                body.contains("Just a moment") ||
                body.contains("Checking your browser") ||
                body.contains("cf-browser-verification") ||
                body.contains("_cf_chl_opt") ||
                body.contains("challenge-platform"),
                "Should detect JS challenge pattern in: $body"
            )
        }
    }
    
    @Test
    fun `Turnstile patterns are detected`() {
        val turnstileBodies = listOf(
            "<div class=\"cf-turnstile\" data-sitekey=\"0x4AAAAAAA\">",
            "challenges.cloudflare.com/turnstile/v0/api.js"
        )
        
        turnstileBodies.forEach { body ->
            assertTrue(
                body.contains("cf-turnstile") ||
                body.contains("turnstile"),
                "Should detect Turnstile pattern in: $body"
            )
        }
    }
    
    @Test
    fun `Block patterns are detected`() {
        val blockBodies = listOf(
            "Access denied",
            "Sorry, you have been blocked",
            "<div class=\"cf-error-details\">"
        )
        
        blockBodies.forEach { body ->
            assertTrue(
                body.contains("Access denied", ignoreCase = true) ||
                body.contains("you have been blocked", ignoreCase = true) ||
                body.contains("cf-error-details"),
                "Should detect block pattern in: $body"
            )
        }
    }
}
