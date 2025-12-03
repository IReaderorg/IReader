package ireader.core.http

import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * iOS implementation of HttpClients using Darwin engine
 */
actual class HttpClients : HttpClientsInterface {
    
    actual override val browser: BrowserEngine = BrowserEngine()
    actual override val config: NetworkConfig = NetworkConfig()
    actual override val sslConfig: SSLConfiguration = SSLConfiguration()
    actual override val cookieSynchronizer: CookieSynchronizer = CookieSynchronizer()
    
    actual override val default: HttpClient = HttpClient(Darwin) {
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = config.connectTimeoutSeconds * 1000
            connectTimeoutMillis = config.connectTimeoutSeconds * 1000
            socketTimeoutMillis = config.readTimeoutMinutes * 60 * 1000
        }
        
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        
        install(UserAgent) {
            agent = config.userAgent
        }
    }
    
    actual override val cloudflareClient: HttpClient = default
}
