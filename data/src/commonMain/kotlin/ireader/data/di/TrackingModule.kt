package ireader.data.di

import ireader.data.repository.TrackingRepositoryImpl
import ireader.data.tracking.anilist.AniListRepositoryImpl
import ireader.data.tracking.kitsu.KitsuRepositoryImpl
import ireader.data.tracking.mal.MyAnimeListRepositoryImpl
import ireader.data.tracking.mangaupdates.MangaUpdatesRepositoryImpl
import ireader.domain.data.repository.TrackingRepository
import org.koin.dsl.module

/**
 * Koin module for tracking-related dependencies.
 * Provides AniList, MyAnimeList, Kitsu, and MangaUpdates tracking service implementations.
 */
val trackingModule = module {
    // AniList repository
    single {
        AniListRepositoryImpl(
            httpClient = get<ireader.core.http.HttpClients>().default,
            preferenceStore = get()
        )
    }
    
    // MyAnimeList repository
    single {
        MyAnimeListRepositoryImpl(
            httpClient = get<ireader.core.http.HttpClients>().default,
            preferenceStore = get()
        )
    }
    
    // Kitsu repository
    single {
        KitsuRepositoryImpl(
            httpClient = get<ireader.core.http.HttpClients>().default,
            preferenceStore = get()
        )
    }
    
    // MangaUpdates repository
    single {
        MangaUpdatesRepositoryImpl(
            httpClient = get<ireader.core.http.HttpClients>().default,
            preferenceStore = get()
        )
    }
    
    // Main tracking repository with all services
    single<TrackingRepository> {
        TrackingRepositoryImpl(
            handler = get(),
            aniListRepository = get(),
            malRepository = get(),
            kitsuRepository = get(),
            mangaUpdatesRepository = get()
        )
    }
}
