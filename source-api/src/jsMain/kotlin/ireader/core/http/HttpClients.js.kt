package ireader.core.http

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * JavaScript implementation of HttpClients.
 * 
 * Uses Ktor's JS engine for HTTP requests.
 */
actual class HttpClients : HttpClientsInterface {
    
    actual override val browser: BrowserEngine by lazy {
        BrowserEngine()
    }
    
    actual override val default: HttpClient by lazy {
        HttpClient(Js) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
            
            install(HttpTimeout) {
                requestTimeoutMillis = config.connectTimeoutSeconds * 1000
                connectTimeoutMillis = config.connectTimeoutSeconds * 1000
            }
            
            // Default headers
            defaultRequest {
                headers.append("User-Agent", DEFAULT_USER_AGENT)
            }
        }
    }
    
    actual override val cloudflareClient: HttpClient by lazy {
        // In JS context, Cloudflare bypass is handled by the browser/iOS native layer
        default
    }
    
    actual override val config: NetworkConfig by lazy {
        NetworkConfig()
    }
    
    actual override val sslConfig: SSLConfiguration by lazy {
        SSLConfiguration()
    }
    
    actual override val cookieSynchronizer: CookieSynchronizer by lazy {
        CookieSynchronizer()
    }
}
