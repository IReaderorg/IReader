package ireader.data.di

import android.content.Context
import ireader.data.sync.datasource.AndroidDiscoveryDataSource
import ireader.data.sync.datasource.DiscoveryDataSource
import org.koin.dsl.module

/**
 * Android-specific Koin module for sync data sources.
 * 
 * Registers AndroidDiscoveryDataSource which uses NsdManager
 * for mDNS service discovery on Android.
 */
val syncPlatformModule = module {
    
    single<DiscoveryDataSource> { 
        AndroidDiscoveryDataSource(
            context = get<Context>()
        )
    }
}
