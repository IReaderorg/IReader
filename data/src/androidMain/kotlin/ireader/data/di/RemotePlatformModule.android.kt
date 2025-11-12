package ireader.data.di

import ireader.data.remote.NetworkConnectivityMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific remote dependencies
 */
actual val remotePlatformModule = module {
    single { 
        NetworkConnectivityMonitor().apply {
            initialize(androidContext())
        }
    }
}
