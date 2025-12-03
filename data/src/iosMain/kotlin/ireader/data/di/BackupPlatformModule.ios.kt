package ireader.data.di

import ireader.data.backup.GoogleDriveAuthenticator
import ireader.data.backup.GoogleDriveAuthenticatorIos
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific backup dependencies
 */
actual val backupPlatformModule: Module = module {
    single<GoogleDriveAuthenticator> {
        GoogleDriveAuthenticatorIos()
    }
}
