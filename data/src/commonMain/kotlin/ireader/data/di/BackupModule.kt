package ireader.data.di

import ireader.data.backup.GoogleDriveBackupServiceImpl
import ireader.domain.services.backup.GoogleDriveBackupService
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Dependency injection module for backup services
 * 
 * Note: Platform-specific authenticators must be provided by platform modules
 */
val backupModule = module {
    // Include platform-specific backup dependencies
    includes(backupPlatformModule)
    
    // Google Drive Backup Service
    single<GoogleDriveBackupService> {
        GoogleDriveBackupServiceImpl(
            authenticator = get()
        )
    }
}

/**
 * Platform-specific backup module
 * Provides platform-specific implementations of GoogleDriveAuthenticator
 */
expect val backupPlatformModule: Module
