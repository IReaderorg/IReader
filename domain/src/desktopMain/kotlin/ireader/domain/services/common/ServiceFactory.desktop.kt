package ireader.domain.services.common

/**
 * Desktop implementation of ServiceFactory
 */
actual object ServiceFactory {
    
    actual fun createBackgroundTaskService(): BackgroundTaskService {
        return DesktopBackgroundTaskService()
    }
    
    actual fun createDownloadService(): DownloadService {
        return DesktopDownloadService()
    }
    
    actual fun createFileService(): FileService {
        return DesktopFileService()
    }
    
    actual fun createNotificationService(): NotificationService {
        return DesktopNotificationService()
    }
    
    actual fun createLibraryUpdateService(): LibraryUpdateService {
        return DesktopLibraryUpdateService()
    }
    
    actual fun createExtensionService(): ExtensionService {
        return DesktopExtensionService()
    }
    
    actual fun createBackupService(): BackupService {
        return DesktopBackupService()
    }
    
    actual fun createTTSService(): TTSService {
        return DesktopTTSService()
    }
    
    actual fun createSyncService(): SyncService {
        return DesktopSyncService()
    }
    
    actual fun createCacheService(): CacheService {
        return DesktopCacheService()
    }
    
    actual fun createTranslationService(): TranslationService {
        return DesktopTranslationService()
    }
}
