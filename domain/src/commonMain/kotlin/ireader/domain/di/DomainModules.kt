package ireader.domain.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginPreferences
import ireader.domain.plugins.PluginRegistry
import ireader.domain.services.book.bookModule
import ireader.domain.services.chapter.chapterModule
import ireader.domain.services.downloaderService.DownloadStateHolder
import ireader.domain.services.library.libraryModule
import ireader.domain.services.preferences.preferencesModule
import ireader.domain.usecases.backup.CloudBackupManager
import ireader.domain.usecases.backup.CloudProvider
import ireader.domain.usecases.backup.CreateBackup
import ireader.domain.usecases.backup.DropboxProvider
import ireader.domain.usecases.backup.GoogleDriveProvider
import ireader.domain.usecases.backup.RestoreBackup
import ireader.domain.usecases.category.CategoriesUseCases
import ireader.domain.usecases.category.CreateCategoryWithName
import ireader.domain.usecases.category.ReorderCategory
import ireader.domain.usecases.download.get.SubscribeDownloadsUseCase
import ireader.domain.usecases.fonts.FontUseCase
import ireader.domain.usecases.preferences.apperance.NightModePreferencesUseCase
import ireader.domain.usecases.preferences.reader_preferences.DohPrefUseCase
import ireader.domain.usecases.preferences.services.LastUpdateTime
import ireader.domain.usecases.remote.GetBookDetail
import ireader.domain.usecases.remote.GetRemoteBooksUseCase
import ireader.domain.usecases.remote.GetRemoteChapters
import ireader.domain.usecases.remote.GetRemoteReadingContent
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.domain.usecases.translation.GetAllTranslationsForChapterUseCase
import kotlinx.serialization.json.Json
import org.koin.dsl.module


val DomainServices = module {
    
    // HTTP Client - LAZY: not needed at startup
    single(createdAtStart = false) {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    // Download state - lightweight, can be singleton
    single<DownloadStateHolder> { DownloadStateHolder() }

    // Preferences - lightweight
    single { ireader.domain.preferences.prefs.PlayerPreferences(get()) }
    single { ireader.domain.preferences.prefs.DownloadPreferences(get()) }
    single { ireader.domain.preferences.prefs.TranslationPreferences(get()) }
    
    // Translation use cases - FACTORY: not needed at startup
    factory { GetAllTranslationsForChapterUseCase(get()) }
    
    // NOTE: TranslationServiceImpl and TranslationStateHolder are registered in ServiceModule.kt
    // as singletons to ensure the same instance is shared across AndroidTranslationService
    // and all other consumers. DO NOT register them here as factory - it will break notifications!




    factory  {
        CreateBackup(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory  {
        RestoreBackup(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    
    // Cloud Backup Providers - lazy loaded when user accesses backup
    factory { DropboxProvider() }
    factory { GoogleDriveProvider() }
    
    // CloudBackupManager - lazy loaded
    factory {
        CloudBackupManager(
            providers = mapOf(
                CloudProvider.DROPBOX to get<DropboxProvider>(),
                CloudProvider.GOOGLE_DRIVE to get<GoogleDriveProvider>()
            )
        )
    }
    
    // LNReader Backup Import - lazy loaded when user imports LNReader backup
    factory { ireader.domain.usecases.backup.lnreader.LNReaderBackupParser() }
    factory { ireader.domain.usecases.backup.lnreader.LNReaderSourceMapper(get()) }
    factory {
        ireader.domain.usecases.backup.lnreader.ImportLNReaderBackup(
            parser = get(),
            sourceMapper = get(),
            bookRepository = get(),
            chapterRepository = get(),
            categoryRepository = get(),
            bookCategoryRepository = get(),
            transactions = get(),
            fileSaver = get()
        )
    }
    factory  { CategoriesUseCases(get(), get(), get(), get()) }
    factory  { CreateCategoryWithName(get()) }
    factory  { ReorderCategory(get()) }
    factory  { SubscribeDownloadsUseCase(get()) }


    factory  { FontUseCase(get(), get()) }
    factory  { ireader.domain.usecases.fonts.FontManagementUseCase(get()) }


    factory  { ireader.domain.usecases.local.FindBooksByKey(get()) }
    factory  { ireader.domain.usecases.local.SubscribeBooksByKey(get()) }
    factory  { ireader.domain.usecases.local.FindBookByKey(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.BookMarkChapterUseCase(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.FindAllInLibraryBooks(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.GetLibraryCategory(get(), get(), get()) }
    factory  {
        ireader.domain.usecases.local.book_usecases.MarkBookAsReadOrNotUseCase(
            get(),
            get()
        )
    }
    factory  { ireader.domain.usecases.local.book_usecases.SubscribeBookById(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.FindBookById(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.FindDuplicateBook(get()) }
    factory  { ireader.domain.usecases.local.book_usecases.SubscribeInLibraryBooks(get()) }
    factory  {
        ireader.domain.usecases.local.chapter_usecases.UpdateLastReadTime(
            get(),
            get(),
            get()
        )
    }
    factory  { ireader.domain.usecases.local.delete_usecases.book.DeleteBookById(get()) }
    factory  {
        ireader.domain.usecases.local.delete_usecases.book.UnFavoriteBook(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory  {
        ireader.domain.usecases.local.delete_usecases.book.DeleteNotInLibraryBooks(
            get()
        )
    }
    factory  {
        ireader.domain.usecases.local.delete_usecases.chapter.DeleteChaptersByBookId(
            get()
        )
    }
    factory  { ireader.domain.usecases.local.insert_usecases.InsertBook(get()) }
    factory  { ireader.domain.usecases.local.insert_usecases.UpdateBook(get()) }
    factory  { ireader.domain.usecases.local.insert_usecases.InsertBooks(get()) }
    factory  { ireader.domain.usecases.local.insert_usecases.InsertBookAndChapters(get()) }
    factory  { ireader.domain.usecases.local.insert_usecases.InsertChapter(get()) }
    factory  { ireader.domain.usecases.local.insert_usecases.InsertChapters(get()) }
    factory  {
        NightModePreferencesUseCase(
            get()
        )
    }
    factory  { DohPrefUseCase(get()) }

    factory  { LastUpdateTime(get()) }

    factory  { GetBookDetail() }
    factory  { GetRemoteBooksUseCase() }
    factory  { GetRemoteChapters() }
    factory  { GetRemoteReadingContent() }
    factory  { ireader.domain.usecases.remote.GlobalSearchUseCase(get()) }

    factory  {
        TranslationEnginesManager(
            readerPreferences = get(),
            httpClients = get(),
            pluginManager = getOrNull() // Optional to avoid circular dependency
        )
    }
    factory  { ireader.domain.catalogs.interactor.GetInstalledCatalog(get()) }

    factory  { ireader.domain.usecases.updates.SubscribeUpdates(get()) }
    factory  { ireader.domain.usecases.updates.DeleteAllUpdates(get()) }

    // Sync Use Cases
    factory  { ireader.domain.usecases.sync.SyncBooksUseCase(get()) }
    factory  { ireader.domain.usecases.sync.GetSyncedDataUseCase(get()) }
    factory  { ireader.domain.usecases.sync.FetchAndMergeSyncedBooksUseCase(get(), get(), get()) }
    factory  { ireader.domain.usecases.sync.RefreshLibraryFromRemoteUseCase(get(), get()) }
    factory  { ireader.domain.usecases.sync.SyncBookToRemoteUseCase(get(), get()) }
    factory  { ireader.domain.usecases.sync.SyncBooksToRemoteUseCase(get(), get()) }
    factory  { ireader.domain.usecases.sync.PerformFullSyncUseCase(get(), get(), get()) }
    factory  { ireader.domain.usecases.sync.ToggleBookInLibraryUseCase(get(), get(), get()) }
    factory  { ireader.domain.usecases.sync.IsUserAuthenticatedUseCase(get()) }
    
    // Sync use cases container
    factory {
        ireader.domain.usecases.sync.SyncUseCases(
            syncBookToRemote = get(),
            syncBooksToRemote = get(),
            performFullSync = get(),
            refreshLibraryFromRemote = get(),
            toggleBookInLibrary = get(),
            fetchAndMergeSyncedBooks = get(),
            isUserAuthenticated = get()
        )
    }
    
    // Sync Manager - lazy loaded when sync is used
    factory { 
        ireader.domain.services.SyncManager(
            remoteRepository = get(),
            supabasePreferences = get(),
            syncBooksUseCase = get(),
            getSyncedDataUseCase = get()
        )
    }

    // Filter State Manager - lazy loaded
    factory { ireader.domain.filters.FilterStateManager(get()) }
    
    // Plugin System - all lazy loaded since plugins are not needed at startup
    // Plugin Preferences
    factory { PluginPreferences(get()) }
    
    // Plugin Registry
    factory { PluginRegistry(get()) }
    
    // Plugin Manager
    factory {
        PluginManager(
            fileSystem = get(),
            loader = get(),
            registry = get(),
            preferences = get(),
            monetization = get(),
            database = get(),
            securityManager = get(),
            performanceMetricsManager = get()
        )
    }
    
    // Voice Management - Platform-specific implementation will be provided in platform modules
    
    // Voice downloader - lazy loaded when TTS is used
    factory {
        ireader.domain.voice.VoiceDownloader(
            httpClient = get(),
            storage = get()
        )
    }
    

    
    // Use Case Module - New clean architecture use cases
    includes(useCaseModule)
    
    // Use Case Aggregate Module - Aggregates for ViewModel simplification
    // Requirements: 4.1, 4.2, 4.3, 4.5
    includes(useCaseAggregateModule)
    
    // Chapter Module - Unified Chapter Controller and use cases
    // Requirements: 5.5 - ChapterController injectable via DI
    includes(chapterModule)
    
    // Preferences Module - ReaderPreferencesController as singleton
    // Requirements: 6.1, 6.2, 6.3, 6.4
    includes(preferencesModule)
    
    // Book Module - BookController as singleton
    // Requirements: 6.1, 6.2, 6.3, 6.4
    includes(bookModule)
    
    // Library Module - LibraryController as singleton
    // Requirements: 6.1, 6.2, 6.3, 6.4
    includes(libraryModule)
    
    // Note: Preferences, UseCases, and Repository UseCases are loaded separately
    // to avoid circular dependencies with UseCasesInject module

}