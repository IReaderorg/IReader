package ireader.data.di

import ireader.data.backup.GoogleDriveAuthenticator
import ireader.data.backup.GoogleDriveAuthenticatorDesktop
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Desktop-specific backup dependencies
 */
actual val backupPlatformModule: Module = module {
    single<GoogleDriveAuthenticator> {
        GoogleDriveAuthenticatorDesktop()
    }
}
