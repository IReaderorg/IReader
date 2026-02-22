package ireader.data.di

import ireader.data.sync.SyncLocalDataSourceImpl
import ireader.data.sync.datasource.KtorTransferDataSource
import ireader.data.sync.datasource.SyncLocalDataSource
import ireader.data.sync.datasource.TransferDataSource
import ireader.data.sync.encryption.AesEncryptionService
import ireader.data.sync.encryption.CertificatePinningManager
import ireader.data.sync.repository.SyncRepositoryImpl
import ireader.data.sync.repository.TrustedDeviceRepositoryImpl
import ireader.domain.repositories.SyncRepository
import ireader.domain.repositories.TrustedDeviceRepository
import ireader.domain.services.sync.EncryptionService
import org.koin.dsl.module

/**
 * Koin module for sync-related data layer components.
 * 
 * Registers repository implementations, common data sources, and encryption services.
 * Platform-specific services (DiscoveryDataSource, CertificateService, KeyStorageService)
 * are registered in platform-specific modules.
 */
val syncDataModule = module {
    
    // Include platform-specific sync dependencies
    includes(syncPlatformModule)
    
    // ========== Encryption Services ==========
    
    /**
     * AES-256-GCM encryption service for payload encryption.
     * 
     * Provides authenticated encryption for sync data payloads.
     * Uses platform-specific crypto APIs via expect/actual pattern.
     * 
     * Task 9.2.4: AES-256 payload encryption implementation
     */
    single<EncryptionService> { 
        AesEncryptionService() 
    }
    
    /**
     * Certificate pinning manager for MITM attack prevention.
     * 
     * Manages certificate fingerprints for trusted devices and validates
     * certificates during TLS handshake to prevent man-in-the-middle attacks.
     * 
     * Dependencies:
     * - CertificateService: Platform-specific certificate operations
     * - SyncLocalDataSource: Persistent storage for certificate fingerprints
     * 
     * Task 9.2.3: Certificate pinning implementation
     */
    single { 
        CertificatePinningManager(
            certificateService = get(),
            localStorage = get()
        )
    }
    
    // ========== Repositories ==========
    
    single<SyncRepository> { 
        SyncRepositoryImpl(
            discoveryDataSource = get(),
            transferDataSource = get(),
            localDataSource = get(),
            platformConfig = ireader.domain.config.PlatformConfig
        )
    }
    
    single<TrustedDeviceRepository> {
        TrustedDeviceRepositoryImpl(
            syncLocalDataSource = get()
        )
    }
    
    // ========== Data Sources ==========
    
    /**
     * Ktor-based WebSocket transfer data source with TLS support.
     * 
     * Handles secure data transfer between devices using WebSocket connections.
     * Supports both plain and TLS-encrypted connections with certificate pinning.
     * 
     * Dependencies:
     * - CertificateService: For TLS certificate operations (optional)
     * - CertificatePinningManager: For certificate validation (optional)
     * 
     * Task 9.2.1: TLS/SSL WebSocket support
     */
    single<TransferDataSource> { 
        KtorTransferDataSource(
            certificateService = get(),
            certificatePinningManager = get()
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
