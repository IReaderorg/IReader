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
        TODO("Desktop BackupService implementation")
    }
    
    actual fun createTTSService(): TTSService {
        TODO("Desktop TTSService implementation")
    }
    
    actual fun createSyncService(): SyncService {
        TODO("Desktop SyncService implementation")
    }
    
    actual fun createCacheService(): CacheService {
        TODO("Desktop CacheService implementation")
    }
}
