package ireader.core.http

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

/**
 * iOS implementation of HttpClients using Darwin engine
 */
actual class HttpClients : HttpClientsInterface {
    
    actual override val browser: BrowserEngine = BrowserEngine()
    
    actual override val default: HttpClient = HttpClient(Darwin) {
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }
    
    actual override val cloudflareClient: HttpClient = default
}
