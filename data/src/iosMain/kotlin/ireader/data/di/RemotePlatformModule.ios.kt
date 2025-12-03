package ireader.data.di

import ireader.data.remote.NetworkConnectivityMonitor
import org.koin.dsl.module

/**
 * iOS-specific remote dependencies
 */
actual val remotePlatformModule = module {
    single { NetworkConnectivityMonitor() }
}
