package ireader.domain.services.common

/**
 * Factory for creating platform-specific service implementations
 */
expect object ServiceFactory {
    fun createBackgroundTaskService(): BackgroundTaskService
    fun createDownloadService(): DownloadService
    fun createFileService(): FileService
    fun createNotificationService(): NotificationService
    fun createLibraryUpdateService(): LibraryUpdateService
    fun createExtensionService(): ExtensionService
    fun createBackupService(): BackupService
    fun createTTSService(): TTSService
    fun createSyncService(): SyncService
    fun createCacheService(): CacheService
    fun createTranslationService(): TranslationService
}
