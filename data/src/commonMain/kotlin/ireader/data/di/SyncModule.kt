package ireader.data.di

import ireader.data.sync.SyncLocalDataSourceImpl
import ireader.data.sync.datasource.SyncLocalDataSource
import ireader.data.sync.datasource.TransferDataSource
import ireader.data.sync.repository.SyncRepositoryImpl
import ireader.domain.repositories.SyncRepository
import org.koin.dsl.module

/**
 * Koin module for sync-related data layer components.
 * 
 * Registers repository implementations, common data sources.
 * Platform-specific services (DiscoveryDataSource, CertificateService, KeyStorageService)
 * are registered in platform-specific modules.
 */
val syncDataModule = module {
    
    // Include platform-specific sync dependencies
    includes(syncPlatformModule)
    
    // ========== Repositories ==========
    
    single<SyncRepository> { 
        SyncRepositoryImpl(
            discoveryDataSource = get(),
            transferDataSource = get(),
            localDataSource = get(),
            platformConfig = ireader.domain.config.PlatformConfig,
            syncPreferences = get()
        )
    }
    
    // ========== Data Sources ==========
    
    /**
     * TCP-based transfer data source for better firewall compatibility.
     * 
     * Handles data transfer between devices using plain TCP sockets.
     * No HTTP upgrade needed - direct TCP connection for better firewall compatibility.
     * 
     * Dependencies:
     * - CertificateService: For TLS certificate operations (optional, future use)
     * - SocketConfigurator: For VPN bypass and local network routing (platform-specific)
     * 
     * Uses raw TCP sockets instead of WebSockets to avoid firewall issues.
     * Bypasses VPN on Android to ensure direct local network communication.
     */
    single<TransferDataSource> { 
        ireader.data.sync.datasource.TcpTransferDataSource(
            certificateService = get(),
            socketConfigurator = getOrNull() // Platform-specific, may not be available on all platforms
        )
    }
    
    single<SyncLocalDataSource> { 
        SyncLocalDataSourceImpl(
            handler = get()
        )
    }
}

/**
 * Platform-specific sync module.
 * 
 * Provides platform-specific implementations:
 * - DiscoveryDataSource: mDNS service discovery (NsdManager on Android, JmDNS on Desktop)
 * - CertificateService: Certificate generation and management
 * - KeyStorageService: Secure key storage (Android Keystore, Java Keystore)
 */
expect val syncPlatformModule: org.koin.core.module.Module
