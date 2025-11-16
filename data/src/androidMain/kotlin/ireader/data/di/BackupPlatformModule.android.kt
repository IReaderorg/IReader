package ireader.data.di

import ireader.data.backup.GoogleDriveAuthenticator
import ireader.data.backup.GoogleDriveAuthenticatorAndroid
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific backup dependencies
 */
actual val backupPlatformModule: Module = module {
    single<GoogleDriveAuthenticator> {
        GoogleDriveAuthenticatorAndroid()
    }
}
