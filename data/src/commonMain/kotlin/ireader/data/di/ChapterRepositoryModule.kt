package ireader.data.di

import ireader.data.chapter.ChapterRepositoryImpl
import ireader.data.chapterhealth.ChapterHealthRepositoryImpl
import ireader.data.chapterreport.ChapterReportRepositoryImpl
import ireader.domain.data.repository.ChapterHealthRepository
import ireader.domain.data.repository.ChapterReportRepository
import ireader.domain.data.repository.ChapterRepository
import org.koin.dsl.module

/**
 * DI module for chapter-related repositories.
 * Contains ChapterRepository, ChapterHealthRepository, and ChapterReportRepository.
 */
val chapterRepositoryModule = module {
    single<ChapterRepository> { ChapterRepositoryImpl(get(), getOrNull()) }
    single<ChapterHealthRepository> { ChapterHealthRepositoryImpl(get()) }
    single<ChapterReportRepository> { ChapterReportRepositoryImpl(get()) }
}
