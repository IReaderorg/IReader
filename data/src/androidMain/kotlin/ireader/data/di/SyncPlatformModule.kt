package ireader.data.di

import android.content.Context
import ireader.data.sync.datasource.AndroidDiscoveryDataSource
import ireader.data.sync.datasource.DiscoveryDataSource
import ireader.data.sync.encryption.AndroidCertificateService
import ireader.data.sync.encryption.AndroidKeyStorageService
import ireader.domain.services.sync.CertificateService
import ireader.domain.services.sync.KeyStorageService
import org.koin.dsl.module

/**
 * Android-specific Koin module for sync data sources and encryption services.
 * 
 * Provides Android-specific implementations:
 * - AndroidDiscoveryDataSource: Uses NsdManager for mDNS service discovery
 * - AndroidCertificateService: Uses Android Keystore for certificate management
 * - AndroidKeyStorageService: Uses Android Keystore System for secure key storage
 * - SocketConfigurator: Configures sockets to bypass VPN for local network sync
 */
actual val syncPlatformModule = module {
    
    /**
     * Android mDNS service discovery using NsdManager.
     * 
     * Discovers other IReader devices on the local network using
     * Android's Network Service Discovery API.
     */
    single<DiscoveryDataSource> { 
        AndroidDiscoveryDataSource(
            context = get<Context>()
        )
    }
    
    /**
     * Android certificate service using Android Keystore.
     * 
     * Generates and manages self-signed certificates for TLS connections.
     * Uses Android Keystore for secure certificate storage.
     * 
     * Task 9.2.2: Self-signed certificate generation (Android)
     */
    single<CertificateService> {
        AndroidCertificateService(
            context = get<Context>()
        )
    }
    
    /**
     * Android secure key storage using Android Keystore System.
     * 
     * Stores encryption keys securely using Android's hardware-backed
     * Keystore System. Keys are encrypted and protected by the platform.
     * 
     * Task 9.2.5: Secure key storage (Android)
     */
    single<KeyStorageService> {
        AndroidKeyStorageService(
            context = get<Context>()
        )
    }
    
    /**
     * Socket configurator for VPN bypass.
     * 
     * Configures network sockets to bypass VPN and use the underlying
     * WiFi network directly for local sync operations.
     * 
     * This ensures WiFi sync works even when a VPN is active by binding
     * the process to the WiFi network using ConnectivityManager.
     */
    single<ireader.data.sync.datasource.SocketConfigurator> {
        ireader.data.sync.datasource.SocketConfigurator(
            context = get<Context>()
        )
    }
}
