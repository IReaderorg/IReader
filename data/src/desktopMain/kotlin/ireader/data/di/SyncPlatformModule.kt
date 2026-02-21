package ireader.data.di

import ireader.data.sync.datasource.DesktopDiscoveryDataSource
import ireader.data.sync.datasource.DiscoveryDataSource
import org.koin.dsl.module

/**
 * Desktop-specific Koin module for sync data sources.
 * 
 * Registers DesktopDiscoveryDataSource which uses JmDNS
 * for mDNS service discovery on Desktop platforms.
 */
val syncPlatformModule = module {
    
    single<DiscoveryDataSource> { 
        DesktopDiscoveryDataSource()
    }
}
