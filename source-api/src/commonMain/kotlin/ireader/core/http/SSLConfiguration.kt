package ireader.core.http

import okhttp3.OkHttpClient

/**
 * Platform-specific SSL/TLS configuration
 * Handles certificate pinning, custom trust managers, and TLS version control
 */
expect class SSLConfiguration() {
    /**
     * Apply SSL/TLS configuration to OkHttpClient builder
     */
    fun applyTo(builder: OkHttpClient.Builder)
    
    /**
     * Enable certificate pinning for specific domains
     * @param pins Map of domain to list of certificate pins (SHA-256 hashes)
     */
    fun enableCertificatePinning(pins: Map<String, List<String>>)
    
    /**
     * Allow self-signed certificates (for development/testing only)
     */
    fun allowSelfSignedCertificates()
    
    /**
     * Set minimum TLS version
     */
    fun setMinimumTlsVersion(version: TlsVersion)
}

enum class TlsVersion {
    TLS_1_2,
    TLS_1_3
}
