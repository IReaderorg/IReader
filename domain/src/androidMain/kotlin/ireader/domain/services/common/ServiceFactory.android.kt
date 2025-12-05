package ireader.domain.services.common

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of ServiceFactory
 */
actual object ServiceFactory : KoinComponent {
    private val context: Context by inject()
    
    actual fun createBackgroundTaskService(): BackgroundTaskService {
        return AndroidBackgroundTaskService(context)
    }
    
    actual fun createDownloadService(): DownloadService {
        return AndroidDownloadService(context)
    }
    
    actual fun createFileService(): FileService {
        return AndroidFileService(context)
    }
    
    actual fun createNotificationService(): NotificationService {
        return AndroidNotificationService(context)
    }
    
    actual fun createLibraryUpdateService(): LibraryUpdateService {
        return AndroidLibraryUpdateService(context)
    }
    
    actual fun createExtensionService(): ExtensionService {
        return AndroidExtensionService(context)
    }
    
    actual fun createBackupService(): BackupService {
        return AndroidBackupService(context)
    }
    
    actual fun createTTSService(): TTSService {
        return AndroidTTSService(context)
    }
    
    actual fun createSyncService(): SyncService {
        return AndroidSyncService(context)
    }
    
    actual fun createCacheService(): CacheService {
        return AndroidCacheService(context)
    }
    
    actual fun createTranslationService(): TranslationService {
        return AndroidTranslationService(context)
    }
}
