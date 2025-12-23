package ireader.data.di

import ireader.data.repository.TrackingRepositoryImpl
import ireader.data.tracking.anilist.AniListRepositoryImpl
import ireader.domain.data.repository.TrackingRepository
import org.koin.dsl.module

/**
 * Koin module for tracking-related dependencies.
 * Provides AniList and other tracking service implementations.
 */
val trackingModule = module {
    // AniList repository
    single {
        AniListRepositoryImpl(
            httpClient = get<ireader.core.http.HttpClients>().default,
            preferenceStore = get()
        )
    }
    
    // Main tracking repository
    single<TrackingRepository> {
        TrackingRepositoryImpl(
            handler = get(),
            aniListRepository = get()
        )
    }
}
