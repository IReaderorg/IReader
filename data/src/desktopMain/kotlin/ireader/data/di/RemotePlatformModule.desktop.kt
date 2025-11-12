package ireader.data.di

import ireader.data.remote.NetworkConnectivityMonitor
import org.koin.dsl.module

/**
 * Desktop-specific remote dependencies
 */
actual val remotePlatformModule = module {
    single { NetworkConnectivityMonitor() }
}
