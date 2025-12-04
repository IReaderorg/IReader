package ireader.core.http

/**
 * JavaScript implementation of SSL/TLS configuration.
 * 
 * In JavaScript/browser context, SSL is handled by the browser/runtime,
 * so this is mostly a no-op implementation.
 */
actual class SSLConfiguration actual constructor() {
    
    /**
     * Enable certificate pinning for specific hosts.
     * Not supported in JS - browser handles SSL.
     */
    actual fun enableCertificatePinning(pins: Map<String, List<String>>) {
        // No-op in JS - browser handles SSL
        console.log("SSLConfiguration: Certificate pinning not supported in JS")
    }
    
    /**
     * Allow self-signed certificates.
     * Not supported in JS - browser handles SSL.
     */
    actual fun allowSelfSignedCertificates() {
        // No-op in JS - browser handles SSL
        console.log("SSLConfiguration: Self-signed certificates setting not supported in JS")
    }
    
    /**
     * Set minimum TLS version.
     * Not supported in JS - browser handles SSL.
     */
    actual fun setMinimumTlsVersion(version: TlsVersion) {
        // No-op in JS - browser handles SSL
        console.log("SSLConfiguration: TLS version setting not supported in JS")
    }
}
