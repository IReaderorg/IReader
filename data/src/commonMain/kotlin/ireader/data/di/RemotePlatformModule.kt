package ireader.data.di

import ireader.data.remote.NetworkConnectivityMonitor
import org.koin.dsl.module

/**
 * Platform-specific module for remote dependencies
 * Implementations should be provided in androidMain and desktopMain
 */
expect val remotePlatformModule: org.koin.core.module.Module
