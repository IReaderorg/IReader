package ireader.core.http

/**
 * iOS implementation of SSLConfiguration
 * 
 * iOS handles SSL/TLS at the system level through App Transport Security (ATS)
 */
actual class SSLConfiguration {
    
    actual fun applyToClient(builder: Any) {
        // iOS handles SSL through ATS - no manual configuration needed
    }
    
    actual fun trustAllCertificates(): SSLConfiguration {
        // Not recommended for production
        return this
    }
    
    actual fun pinCertificates(pins: List<String>): SSLConfiguration {
        // TODO: Implement certificate pinning if needed
        return this
    }
}
