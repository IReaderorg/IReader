package ireader.data.di

import ireader.data.backup.GoogleDriveAuthenticator
import ireader.data.backup.GoogleDriveAuthenticatorAndroid
import ireader.data.backup.GoogleDriveConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific backup dependencies
 */
actual val backupPlatformModule: Module = module {
    single<GoogleDriveAuthenticator> {
        GoogleDriveAuthenticatorAndroid().apply {
            val clientId = GoogleDriveConfig.clientId ?: ""
            initialize(androidContext(), clientId)
        }
    }
    
    // Also provide the concrete type for Activity-based auth flow
    single<GoogleDriveAuthenticatorAndroid> {
        get<GoogleDriveAuthenticator>() as GoogleDriveAuthenticatorAndroid
    }
}
