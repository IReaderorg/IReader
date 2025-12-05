package ireader.domain.di

import ireader.domain.services.common.*
import ireader.domain.services.ChapterCacheService
import ireader.domain.services.ChapterCacheServiceImpl
import ireader.domain.services.SourceHealthChecker
import ireader.domain.services.SourceHealthCheckerImpl
import ireader.domain.services.platform.*
import ireader.domain.services.translationService.TranslationServiceImpl
import ireader.domain.services.translationService.TranslationStateHolder
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
    
    // Translation Service - shared state holder and implementation
    // Note: TranslationStateHolder is shared between TranslationServiceImpl and platform services
    single<TranslationStateHolder> { TranslationStateHolder() }
    
    // TranslationServiceImpl is the core implementation (used internally by platform services)
    // IMPORTANT: This MUST be a singleton to ensure AndroidTranslationService and all consumers
    // share the same instance. Otherwise, progress updates won't reach the notification observer!
    single {
        TranslationServiceImpl(
            chapterRepository = get(),
            bookRepository = get(),
            translationEnginesManager = get(),
            saveTranslatedChapter = get(),
            getTranslatedChapter = get(),
            translationPreferences = get(),
            readerPreferences = get(),
            remoteUseCases = get(),
            getLocalCatalog = get(),
            stateHolder = get(),
            submitTranslationUseCase = getOrNull(),
            communityPreferences = getOrNull()
        )
    }
    
    // TranslationService is the public interface - on Android this wraps TranslationServiceImpl
    // with notification support. This binding is used when injecting TranslationService interface.
    single<TranslationService> { 
        ServiceFactory.createTranslationService()
    }
    
    // NEW: Chapter Cache Service
    single<ChapterCacheService> {
        ChapterCacheServiceImpl(
            maxCapacity = 5,
            maxMemoryMB = 50
        )
    }
    
    // NEW: Source Health Checker
    single<SourceHealthChecker> {
        SourceHealthCheckerImpl(
            catalogStore = get()
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
