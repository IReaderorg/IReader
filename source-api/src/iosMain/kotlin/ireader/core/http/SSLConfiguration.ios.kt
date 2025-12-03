package ireader.core.http

/**
 * iOS implementation of SSLConfiguration
 * 
 * iOS handles SSL/TLS at the system level through App Transport Security (ATS)
 */
actual class SSLConfiguration {
    
    actual fun enableCertificatePinning(pins: Map<String, List<String>>) {
        // iOS handles certificate pinning through ATS or URLSession delegate
        // TODO: Implement if needed
    }
    
    actual fun allowSelfSignedCertificates() {
        // Not recommended for production - requires ATS exception in Info.plist
    }
    
    actual fun setMinimumTlsVersion(version: TlsVersion) {
        // iOS handles TLS version through ATS configuration
    }
}
