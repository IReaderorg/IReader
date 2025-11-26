package ireader.domain.di

import ireader.domain.services.common.*
import ireader.domain.services.ChapterCacheService
import ireader.domain.services.ChapterCacheServiceImpl
import ireader.domain.services.platform.*
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module

/**
 * Koin module for platform-agnostic services
 */
val ServiceModule = module {
    
    // Core Services
    single<BackgroundTaskService> { 
        ServiceFactory.createBackgroundTaskService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<DownloadService> { 
        ServiceFactory.createDownloadService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<FileService> { 
        ServiceFactory.createFileService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<NotificationService> { 
        ServiceFactory.createNotificationService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<LibraryUpdateService> { 
        ServiceFactory.createLibraryUpdateService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<ExtensionService> { 
        ServiceFactory.createExtensionService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<BackupService> { 
        ServiceFactory.createBackupService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<CacheService> { 
        ServiceFactory.createCacheService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<TTSService> { 
        ServiceFactory.createTTSService().apply {
            runBlocking { initialize() }
        }
    }
    
    single<SyncService> { 
        ServiceFactory.createSyncService().apply {
            runBlocking { initialize() }
        }
    }
    
    // NEW: Chapter Cache Service
    single<ChapterCacheService> {
        ChapterCacheServiceImpl(
            maxCapacity = 5,
            maxMemoryMB = 50
        )
    }
    
    // NEW: Platform Services (implementations provided by platform-specific modules)
    // These will be provided by DomainModulePlatform
    // single<DeviceInfoService> { ... }
    // single<NetworkService> { ... }
    // single<BiometricService> { ... }
    // single<HapticService> { ... }
    // single<SecureStorageService> { ... }
}
