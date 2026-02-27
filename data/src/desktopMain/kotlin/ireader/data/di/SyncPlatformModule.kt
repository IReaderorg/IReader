package ireader.data.di

import ireader.data.sync.datasource.DesktopDiscoveryDataSource
import ireader.data.sync.datasource.DiscoveryDataSource
import ireader.data.sync.encryption.DesktopCertificateService
import ireader.data.sync.encryption.DesktopKeyStorageService
import ireader.domain.services.sync.CertificateService
import ireader.domain.services.sync.KeyStorageService
import org.koin.dsl.module

/**
 * Desktop-specific Koin module for sync data sources and encryption services.
 * 
 * Provides Desktop-specific implementations:
 * - DesktopDiscoveryDataSource: Uses JmDNS for mDNS service discovery
 * - DesktopCertificateService: Uses Java Keystore for certificate management
 * - DesktopKeyStorageService: Uses Java Keystore (JKS) for secure key storage
 * - SocketConfigurator: No-op implementation (VPN bypass not needed on desktop)
 */
actual val syncPlatformModule = module {
    
    /**
     * Desktop mDNS service discovery using JmDNS.
     * 
     * Discovers other IReader devices on the local network using
     * the JmDNS library for multicast DNS service discovery.
     */
    single<DiscoveryDataSource> { 
        DesktopDiscoveryDataSource()
    }
    
    /**
     * Desktop certificate service using Java Keystore.
     * 
     * Generates and manages self-signed certificates for TLS connections.
     * Uses Java Keystore (JKS) for secure certificate storage.
     * 
     * Task 9.2.2: Self-signed certificate generation (Desktop)
     */
    single<CertificateService> {
        DesktopCertificateService()
    }
    
    /**
     * Desktop secure key storage using Java Keystore (JKS).
     * 
     * Stores encryption keys securely using Java's Keystore API.
     * Keys are encrypted and stored in a password-protected keystore file.
     * 
     * Task 9.2.5: Secure key storage (Desktop)
     */
    single<KeyStorageService> {
        DesktopKeyStorageService()
    }
    
    /**
     * Socket configurator (no-op on desktop).
     * 
     * Desktop OS typically handles local network routing correctly,
     * so VPN bypass is not needed. This is a no-op implementation.
     */
    single<ireader.data.sync.datasource.SocketConfigurator> {
        ireader.data.sync.datasource.SocketConfigurator()
    }
}
