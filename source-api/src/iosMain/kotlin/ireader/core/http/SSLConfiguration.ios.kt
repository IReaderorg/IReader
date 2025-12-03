package ireader.core.http

import platform.Foundation.*
import platform.Security.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of SSLConfiguration
 * 
 * iOS handles SSL/TLS at the system level through App Transport Security (ATS).
 * Most SSL configuration is done via Info.plist rather than programmatically.
 * 
 * For certificate pinning, iOS apps typically use:
 * - URLSession delegate methods
 * - Third-party libraries like TrustKit
 * 
 * Note: Ktor's Darwin engine handles most SSL configuration automatically.
 */
@OptIn(ExperimentalForeignApi::class)
actual class SSLConfiguration {
    
    // Store pinned certificates for validation
    private val pinnedCertificates = mutableMapOf<String, List<String>>()
    private var allowSelfSigned = false
    private var minimumTlsVersion: TlsVersion = TlsVersion.TLS_1_2
    
    /**
     * Enable certificate pinning for specific hosts
     * 
     * On iOS, certificate pinning is typically implemented via:
     * 1. URLSession delegate (didReceiveChallenge)
     * 2. Info.plist configuration
     * 3. Third-party libraries like TrustKit
     * 
     * This stores the pins for use by custom URLSession delegates.
     * 
     * @param pins Map of hostname to list of SHA-256 certificate hashes (base64 encoded)
     */
    actual fun enableCertificatePinning(pins: Map<String, List<String>>) {
        pinnedCertificates.clear()
        pinnedCertificates.putAll(pins)
        
        // Note: To actually enforce pinning, you need to:
        // 1. Implement URLSessionDelegate.URLSession:didReceiveChallenge:completionHandler:
        // 2. Validate the server certificate against the pinned hashes
        // 3. Configure Ktor's Darwin engine to use this delegate
        
        // Example validation would be done in the delegate:
        // - Get server certificate from challenge.protectionSpace.serverTrust
        // - Calculate SHA-256 hash of the certificate's public key
        // - Compare against pinnedCertificates[host]
    }
    
    /**
     * Allow self-signed certificates (for development only)
     * 
     * WARNING: This is insecure and should only be used for development/testing.
     * 
     * On iOS, this requires:
     * 1. Adding NSAppTransportSecurity exception in Info.plist
     * 2. Implementing URLSession delegate to accept self-signed certs
     */
    actual fun allowSelfSignedCertificates() {
        allowSelfSigned = true
        
        // Note: To actually allow self-signed certificates:
        // 1. Add to Info.plist:
        //    <key>NSAppTransportSecurity</key>
        //    <dict>
        //        <key>NSAllowsArbitraryLoads</key>
        //        <true/>
        //    </dict>
        // 2. Implement URLSessionDelegate to accept the challenge
    }
    
    /**
     * Set minimum TLS version
     * 
     * iOS handles TLS version through ATS configuration in Info.plist.
     * By default, iOS requires TLS 1.2 or higher.
     * 
     * To allow older TLS versions (not recommended), add to Info.plist:
     * <key>NSAppTransportSecurity</key>
     * <dict>
     *     <key>NSExceptionDomains</key>
     *     <dict>
     *         <key>example.com</key>
     *         <dict>
     *             <key>NSExceptionMinimumTLSVersion</key>
     *             <string>TLSv1.0</string>
     *         </dict>
     *     </dict>
     * </dict>
     */
    actual fun setMinimumTlsVersion(version: TlsVersion) {
        minimumTlsVersion = version
        
        // iOS enforces TLS 1.2 minimum by default via ATS
        // Lower versions require Info.plist exceptions
        when (version) {
            TlsVersion.TLS_1_0, TlsVersion.TLS_1_1 -> {
                println("[SSLConfiguration] Warning: TLS ${version.name} requires Info.plist exception")
            }
            TlsVersion.TLS_1_2, TlsVersion.TLS_1_3 -> {
                // These are supported by default
            }
        }
    }
    
    /**
     * Get the pinned certificates for a host
     */
    fun getPinnedCertificates(host: String): List<String>? {
        return pinnedCertificates[host]
    }
    
    /**
     * Check if self-signed certificates are allowed
     */
    fun isSelfSignedAllowed(): Boolean = allowSelfSigned
    
    /**
     * Get the minimum TLS version
     */
    fun getMinimumTlsVersion(): TlsVersion = minimumTlsVersion
}
