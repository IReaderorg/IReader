package ireader.core.http

/**
 * SSL/TLS configuration for HTTP clients.
 * Platform-specific implementations handle the actual configuration.
 */
expect class SSLConfiguration() {
    /**
     * Enable certificate pinning for specific hosts
     */
    fun enableCertificatePinning(pins: Map<String, List<String>>)
    
    /**
     * Allow self-signed certificates (for development only)
     */
    fun allowSelfSignedCertificates()
    
    /**
     * Set minimum TLS version
     */
    fun setMinimumTlsVersion(version: TlsVersion)
}

/**
 * TLS version enum
 */
enum class TlsVersion {
    TLS_1_0,
    TLS_1_1,
    TLS_1_2,
    TLS_1_3
}
