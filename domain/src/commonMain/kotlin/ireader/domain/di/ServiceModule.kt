package ireader.domain.di

import ireader.domain.services.common.*
import ireader.domain.services.ChapterCacheService
import ireader.domain.services.ChapterCacheServiceImpl
import ireader.domain.services.platform.*
import org.koin.dsl.module

/**
 * Koin module for platform-agnostic services
 * 
 * Note: Services are created without immediate initialization to avoid blocking.
 * Each service implements lazy initialization on first use.
 */
val ServiceModule = module {
    
    // Core Services - lazy initialization on first use
    single<BackgroundTaskService> { 
        ServiceFactory.createBackgroundTaskService()
    }
    
    single<DownloadService> { 
        ServiceFactory.createDownloadService()
    }
    
    single<FileService> { 
        ServiceFactory.createFileService()
    }
    
    single<NotificationService> { 
        ServiceFactory.createNotificationService()
    }
    
    single<LibraryUpdateService> { 
        ServiceFactory.createLibraryUpdateService()
    }
    
    single<ExtensionService> { 
        ServiceFactory.createExtensionService()
    }
    
    single<BackupService> { 
        ServiceFactory.createBackupService()
    }
    
    single<CacheService> { 
        ServiceFactory.createCacheService()
    }
    
    single<TTSService> { 
        ServiceFactory.createTTSService()
    }
    
    single<SyncService> { 
        ServiceFactory.createSyncService()
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
