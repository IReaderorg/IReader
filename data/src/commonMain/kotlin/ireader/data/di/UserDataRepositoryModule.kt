package ireader.data.di

import ireader.data.font.FontRepositoryImpl
import ireader.data.repository.ReaderThemeRepositoryImpl
import ireader.data.repository.ThemeRepositoryImpl
import ireader.data.security.SecurityRepositoryImpl
import ireader.data.statistics.ReadingStatisticsRepositoryImpl
import ireader.domain.data.repository.FontRepository
import ireader.domain.data.repository.ReaderThemeRepository
import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.data.repository.SecurityRepository
import ireader.domain.data.repository.ThemeRepository
import org.koin.dsl.module

/**
 * DI module for user data repositories.
 * Contains ThemeRepository, ReaderThemeRepository, SecurityRepository,
 * ReadingStatisticsRepository, and FontRepository.
 */
val userDataRepositoryModule = module {
    single<ThemeRepository> { ThemeRepositoryImpl(get()) }
    single<ReaderThemeRepository> { ReaderThemeRepositoryImpl(get()) }
    single<SecurityRepository> { SecurityRepositoryImpl(get(), get()) }
    single<ReadingStatisticsRepository> { ReadingStatisticsRepositoryImpl(get()) }
    single<FontRepository> { FontRepositoryImpl(get(), get()) }
}
