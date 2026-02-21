package ireader.data.di

import ireader.data.sync.SyncLocalDataSourceImpl
import ireader.data.sync.datasource.KtorTransferDataSource
import ireader.data.sync.datasource.SyncLocalDataSource
import ireader.data.sync.datasource.TransferDataSource
import ireader.data.sync.repository.SyncRepositoryImpl
import ireader.domain.repositories.SyncRepository
import org.koin.dsl.module

/**
 * Koin module for sync-related data layer components.
 * 
 * Registers repository implementation and common data sources.
 * Platform-specific data sources (DiscoveryDataSource) are registered
 * in platform-specific modules.
 */
val syncDataModule = module {
    
    // Repository
    single<SyncRepository> { 
        SyncRepositoryImpl(
            discoveryDataSource = get(),
            transferDataSource = get(),
            localDataSource = get()
        )
    }
    
    // Common data sources
    single<TransferDataSource> { KtorTransferDataSource() }
    
    single<SyncLocalDataSource> { 
        SyncLocalDataSourceImpl(
            handler = get()
        )
    }
}
