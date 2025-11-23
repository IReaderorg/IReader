package ireader.presentation.di

import ireader.presentation.ui.settings.statistics.StatsScreenModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Dependency injection module for new StateScreenModel implementations.
 * Provides screen models following Mihon's StateScreenModel pattern.
 */
val screenModelModule = module {
    
    // Statistics screen model
    factoryOf(::StatsScreenModel)
    
    // Main settings screen - singleton to prevent recreation
    single { 
        ireader.presentation.ui.settings.MainSettingScreenViewModel(
            uiPreferences = get(),
            getCurrentUser = {
                val getCurrentUserUseCase: ireader.domain.usecases.remote.GetCurrentUserUseCase = get()
                getCurrentUserUseCase().getOrNull()
            }
        )
    }
    
    // Leaderboard screen model
    factory {
        ireader.presentation.ui.leaderboard.LeaderboardViewModel(
            leaderboardUseCases = get()
        )
    }
}