package ireader.data.di

import ireader.data.chapter.ChapterRepositoryImpl
import ireader.data.chapterhealth.ChapterHealthRepositoryImpl
import ireader.data.chapterreport.ChapterReportRepositoryImpl
import ireader.domain.data.repository.ChapterHealthRepository
import ireader.domain.data.repository.ChapterReportRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.services.chapter.ChapterNotifier
import org.koin.dsl.module

/**
 * DI module for chapter-related repositories.
 * Contains ChapterRepository, ChapterHealthRepository, ChapterReportRepository, and ChapterNotifier.
 * 
 * ChapterNotifier is registered here (data layer) as a singleton so all screens
 * share the same notifier instance. However, notifications are ONLY emitted by
 * ChapterController (single source of truth) - NOT by ChapterRepository.
 */
val chapterRepositoryModule = module {
    // ChapterNotifier - Lightweight change notification system for chapter data.
    // Registered as SINGLETON so all screens share the same notifier instance.
    // Note: Only ChapterController emits notifications (single source of truth).
    single { ChapterNotifier() }
    
    // ChapterRepository - pure data access, no notification emission
    single<ChapterRepository> { ChapterRepositoryImpl(get(), getOrNull()) }
    single<ChapterHealthRepository> { ChapterHealthRepositoryImpl(get()) }
    single<ChapterReportRepository> { ChapterReportRepositoryImpl(get()) }
}
