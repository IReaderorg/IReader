package ireader.domain.di

import ireader.domain.usecases.sync.*
import org.koin.dsl.module

/**
 * Koin module for sync-related use cases.
 * 
 * Registers all domain layer use cases for the Local WiFi Book Sync feature.
 */
val syncDomainModule = module {
    
    // Basic sync use cases
    factory { StartSyncUseCase(get()) }
    factory { StopSyncUseCase(get()) }
    factory { GetDiscoveredDevicesUseCase(get()) }
    factory { GetSyncStatusUseCase(get()) }
    factory { CancelSyncUseCase(get()) }
    
    // Conflict handling use cases
    factory { DetectConflictsUseCase() }
    factory { ResolveConflictsUseCase() }
    
    // Main sync orchestration use case
    factory { 
        SyncWithDeviceUseCase(
            syncRepository = get(),
            detectConflictsUseCase = get(),
            resolveConflictsUseCase = get()
        )
    }
}
