package ireader.domain.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import ireader.domain.plugins.*
import ireader.domain.services.downloaderService.DownloadServiceStateImpl
import ireader.domain.usecases.backup.CloudBackupManager
import ireader.domain.usecases.backup.CloudProvider
import ireader.domain.usecases.backup.CloudStorageProvider
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
import kotlinx.serialization.json.Json
import org.koin.dsl.module


val DomainServices = module {
    
    // HTTP Client for general use (image downloads, etc.)
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    single<DownloadServiceStateImpl> { ireader.domain.services.downloaderService.DownloadServiceStateImpl() }


    single { ireader.domain.preferences.prefs.PlayerPreferences(get()) }
    single { ireader.domain.preferences.prefs.DownloadPreferences(get()) }




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
    
    // Cloud Backup Providers
    single { DropboxProvider() }
    single { GoogleDriveProvider() }
    
    single {
        CloudBackupManager(
            providers = mapOf(
                CloudProvider.DROPBOX to get<DropboxProvider>(),
                CloudProvider.GOOGLE_DRIVE to get<GoogleDriveProvider>()
            )
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
    factory  { ireader.domain.usecases.local.book_usecases.GetLibraryCategory(get(), get()) }
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
    
    // Sync Manager
    single { 
        ireader.domain.services.SyncManager(
            remoteRepository = get(),
            supabasePreferences = get(),
            syncBooksUseCase = get(),
            getSyncedDataUseCase = get()
        )
    }

    // Filter State Manager
    single { ireader.domain.filters.FilterStateManager(get()) }
    
    // Plugin System
    // Plugin Preferences
    single { PluginPreferences(get()) }
    
    // Plugin Registry
    single { PluginRegistry(get()) }
    
    // Plugin Manager
    single {
        PluginManager(
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
    
    single {
        ireader.domain.voice.VoiceDownloader(
            httpClient = get(),
            storage = get()
        )
    }
    
    // Analytics Module
    includes(analyticsModule)
    
    // New Use Case Module following Mihon's pattern
    includes(useCaseModule)

}