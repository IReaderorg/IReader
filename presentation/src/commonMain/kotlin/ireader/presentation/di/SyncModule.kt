package ireader.presentation.di

import ireader.presentation.ui.sync.viewmodel.SyncViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Dependency injection module for sync presentation layer.
 * Provides ViewModels for the Local WiFi Book Sync feature.
 */
val syncPresentationModule = module {
    
    // SyncViewModel - Main screen model for sync operations
    factory {
        SyncViewModel(
            startSyncUseCase = get(),
            stopSyncUseCase = get(),
            syncWithDeviceUseCase = get(),
            getDiscoveredDevicesUseCase = get(),
            getSyncStatusUseCase = get(),
            cancelSyncUseCase = get(),
            resolveConflictsUseCase = get()
        )
    }
}
