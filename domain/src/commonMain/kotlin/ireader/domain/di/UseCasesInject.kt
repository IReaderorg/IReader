package ireader.domain.di



import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.download.delete.DeleteAllSavedDownload
import ireader.domain.usecases.download.delete.DeleteSavedDownload
import ireader.domain.usecases.download.delete.DeleteSavedDownloads
import ireader.domain.usecases.download.get.SubscribeDownloadsUseCase
import ireader.domain.usecases.download.insert.InsertDownload
import ireader.domain.usecases.download.insert.InsertDownloads
import ireader.domain.usecases.download.update.UpdateDownloadPriority
import ireader.domain.usecases.explore.ExploreBookUseCases
import ireader.domain.usecases.explore.SaveExploreBook
import ireader.domain.usecases.explore.SaveExploreBooks
import ireader.domain.usecases.explore.GetExploreBook
import ireader.domain.usecases.explore.PromoteExploreBookToLibrary
import ireader.domain.usecases.explore.ClearExploreBooks
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.local.book_usecases.FindAllInLibraryBooks
import ireader.domain.usecases.local.book_usecases.FindBookById
import ireader.domain.usecases.local.book_usecases.SubscribeBookById
import ireader.domain.usecases.local.book_usecases.SubscribeInLibraryBooks
import ireader.domain.usecases.local.chapter_usecases.FindAllInLibraryChapters
import ireader.domain.usecases.local.chapter_usecases.FindChapterById
import ireader.domain.usecases.local.chapter_usecases.FindChaptersByBookId
import ireader.domain.usecases.local.chapter_usecases.SubscribeChapterById
import ireader.domain.usecases.local.chapter_usecases.SubscribeChaptersByBookId
import ireader.domain.usecases.local.chapter_usecases.UpdateLastReadTime
import ireader.domain.usecases.local.delete_usecases.book.DeleteAllBooks
import ireader.domain.usecases.local.delete_usecases.book.DeleteBookById
import ireader.domain.usecases.local.delete_usecases.book.DeleteNotInLibraryBooks
import ireader.domain.usecases.local.delete_usecases.book.UnFavoriteBook
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteAllChapters
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteChapterByChapter
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteChapters
import ireader.domain.usecases.local.delete_usecases.chapter.DeleteChaptersByBookId
import ireader.domain.usecases.local.insert_usecases.InsertBook
import ireader.domain.usecases.local.insert_usecases.InsertBookAndChapters
import ireader.domain.usecases.local.insert_usecases.InsertBooks
import ireader.domain.usecases.local.insert_usecases.InsertChapter
import ireader.domain.usecases.local.insert_usecases.InsertChapters
import ireader.domain.usecases.local.insert_usecases.UpdateBook
import ireader.domain.usecases.migration.MigrateNovelUseCase
import ireader.domain.usecases.preferences.reader_preferences.AutoScrollMode
import ireader.domain.usecases.preferences.reader_preferences.BackgroundColorUseCase
import ireader.domain.usecases.preferences.reader_preferences.BrightnessStateUseCase
import ireader.domain.usecases.preferences.reader_preferences.BrowseLayoutTypeUseCase
import ireader.domain.usecases.preferences.reader_preferences.BrowseScreenPrefUseCase
import ireader.domain.usecases.preferences.reader_preferences.FontHeightUseCase
import ireader.domain.usecases.preferences.reader_preferences.FontSizeStateUseCase
import ireader.domain.usecases.preferences.reader_preferences.ImmersiveModeUseCase
import ireader.domain.usecases.preferences.reader_preferences.LibraryLayoutTypeUseCase
import ireader.domain.usecases.preferences.reader_preferences.ParagraphDistanceUseCase
import ireader.domain.usecases.preferences.reader_preferences.ParagraphIndentUseCase
import ireader.domain.usecases.preferences.reader_preferences.ReaderPrefUseCases
import ireader.domain.usecases.preferences.reader_preferences.ScrollIndicatorUseCase
import ireader.domain.usecases.preferences.reader_preferences.ScrollModeUseCase
import ireader.domain.usecases.preferences.reader_preferences.SortersDescUseCase
import ireader.domain.usecases.preferences.reader_preferences.SortersUseCase
import ireader.domain.usecases.preferences.reader_preferences.TextAlignmentUseCase
import ireader.domain.usecases.preferences.reader_preferences.TextColorUseCase
import ireader.domain.usecases.preferences.reader_preferences.screens.LibraryScreenPrefUseCases
import ireader.domain.usecases.remote.FetchAndSaveChapterContentUseCase
import ireader.domain.usecases.remote.GetBookDetail
import ireader.domain.usecases.remote.GetRemoteBooksUseCase
import ireader.domain.usecases.remote.GetRemoteChapters
import ireader.domain.usecases.remote.GetRemoteReadingContent
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.statistics.GetReadingStatisticsUseCase
import ireader.domain.usecases.statistics.StatisticsUseCases
import ireader.domain.usecases.statistics.TrackReadingProgressUseCase
import ireader.domain.usecases.statistics.GetLibraryInsightsUseCase
import ireader.domain.usecases.statistics.GetReadingAnalyticsUseCase
import ireader.domain.usecases.statistics.GetUpcomingReleasesUseCase
import ireader.domain.usecases.statistics.GetRecommendationsUseCase
import ireader.domain.usecases.statistics.ExportStatisticsUseCase
import ireader.domain.usecases.statistics.ApplyAdvancedFiltersUseCase
import ireader.domain.usecases.remote.GlobalSearchUseCase
import ireader.domain.usecases.updates.DeleteAllUpdates
import ireader.domain.usecases.updates.FindUpdatesPaginated
import ireader.domain.usecases.updates.SubscribeUpdates
import ireader.domain.usecases.updates.UpdateUseCases
import org.koin.dsl.module


val UseCasesInject = module {

    // Remote use cases - factory for on-demand creation
    factory<RemoteUseCases> {
        val contentFilterUseCase: ireader.domain.usecases.reader.ContentFilterUseCase? = getOrNull()
        val findChapterById = FindChapterById(get(), contentFilterUseCase)
        RemoteUseCases(
            getBookDetail = GetBookDetail(),
            getRemoteBooks = GetRemoteBooksUseCase(),
            getRemoteChapters = GetRemoteChapters(),
            getRemoteReadingContent = GetRemoteReadingContent(),
            globalSearch = get(),
            fetchAndSaveChapterContent = FetchAndSaveChapterContentUseCase(get(), findChapterById),
        )
    }
    factory<ireader.domain.usecases.reader.PreloadChapterUseCase> { ireader.domain.usecases.reader.PreloadChapterUseCase() }
    factory<ireader.domain.usecases.reader.ApplyDefaultReadingModeUseCase> { ireader.domain.usecases.reader.ApplyDefaultReadingModeUseCase(get(), get()) }
    
    // Local use cases - factory for on-demand creation
    factory<ireader.domain.usecases.local.LocalInsertUseCases> {
        ireader.domain.usecases.local.LocalInsertUseCases(
            insertBook = InsertBook(get()),
            insertBookAndChapters = InsertBookAndChapters(get()),
            insertBooks = InsertBooks(get()),
            insertChapter = InsertChapter(get()),
            insertChapters = InsertChapters(get()),
            updateBook = UpdateBook(get())
        )
    }
    factory<ireader.domain.usecases.local.LocalGetBookUseCases> {
        ireader.domain.usecases.local.LocalGetBookUseCases(
            findAllInLibraryBooks = FindAllInLibraryBooks(get()),
            findBookById = FindBookById(get()),
            findBookByKey = ireader.domain.usecases.local.FindBookByKey(get()),
            findBooksByKey = ireader.domain.usecases.local.FindBooksByKey(get()),
            subscribeBookById = SubscribeBookById(get()),
            subscribeBooksByKey = ireader.domain.usecases.local.SubscribeBooksByKey(get()),
            SubscribeInLibraryBooks = SubscribeInLibraryBooks(get()),
        )
    }
    factory<ireader.domain.usecases.local.LocalGetChapterUseCase> {
        val contentFilterUseCase: ireader.domain.usecases.reader.ContentFilterUseCase? = getOrNull()
        ireader.domain.usecases.local.LocalGetChapterUseCase(
            findAllInLibraryChapters = FindAllInLibraryChapters(get()),
            findChapterById = FindChapterById(get(), contentFilterUseCase),
            findChaptersByBookId = FindChaptersByBookId(get()),
            subscribeChaptersByBookId = SubscribeChaptersByBookId(get()),
            updateLastReadTime = UpdateLastReadTime(
                insertUseCases = get(),
                historyUseCase = get(),
                uiPreferences = get(),
                changeNotifier = getOrNull() // LibraryChangeNotifier - optional
            ),
            subscribeChapterById = SubscribeChapterById(get(), contentFilterUseCase)
        )
    }
    factory<ireader.domain.usecases.local.DeleteUseCase> {
        ireader.domain.usecases.local.DeleteUseCase(
            deleteAllBook = DeleteAllBooks(get()),
            deleteAllChapters = DeleteAllChapters(get()),
            deleteBookById = DeleteBookById(get()),
            deleteChapterByChapter = DeleteChapterByChapter(get()),
            deleteChapters = DeleteChapters(get()),
            deleteChaptersByBookId = DeleteChaptersByBookId(get()),
            unFavoriteBook = UnFavoriteBook(
                get(),
                bookCategoryRepository = get(),
                get(),get(),get()
            ),
            deleteNotInLibraryBooks = DeleteNotInLibraryBooks(get())
        )
    }

    // Preference use cases - factory (lightweight but not needed at startup)
    factory<LibraryScreenPrefUseCases> { LibraryScreenPrefUseCases(
        libraryLayoutTypeUseCase = LibraryLayoutTypeUseCase(get(), get()),
        sortersDescUseCase = SortersDescUseCase(get()),
        sortersUseCase = SortersUseCase(get())
    ) }
    factory<ReaderPrefUseCases> { ReaderPrefUseCases(
        autoScrollMode = AutoScrollMode(get()),
        brightnessStateUseCase = BrightnessStateUseCase(get()),
        fontHeightUseCase = FontHeightUseCase(get()),
        fontSizeStateUseCase = FontSizeStateUseCase(get()),
        immersiveModeUseCase = ImmersiveModeUseCase(get()),
        paragraphDistanceUseCase = ParagraphDistanceUseCase(get()),
        paragraphIndentUseCase = ParagraphIndentUseCase(get()),
        scrollIndicatorUseCase = ScrollIndicatorUseCase(get()),
        scrollModeUseCase = ScrollModeUseCase(get()),
        textColorUseCase = TextColorUseCase(get()),
        textAlignmentUseCase = TextAlignmentUseCase(get()),
        backgroundColorUseCase = BackgroundColorUseCase(get())
    ) }
    factory<BrowseScreenPrefUseCase> { BrowseScreenPrefUseCase(
        browseLayoutTypeUseCase = BrowseLayoutTypeUseCase(get())
    ) }
    factory<HistoryUseCase> { HistoryUseCase(
        get()
    ) }
    factory<ireader.domain.usecases.history.GetLastReadNovelUseCase> { 
        ireader.domain.usecases.history.GetLastReadNovelUseCase(
            historyRepository = get(),
            bookRepository = get(),
            chapterRepository = get()
        ) 
    }
    factory<UpdateUseCases> { UpdateUseCases(
        subscribeUpdates = SubscribeUpdates(get()),
        deleteAllUpdates = DeleteAllUpdates(get()),
        findUpdatesPaginated = FindUpdatesPaginated(get()),
    ) }
    
    // Download use cases - factory
    factory<DeleteAllSavedDownload> { DeleteAllSavedDownload(get()) }
    factory<DeleteSavedDownloads> { DeleteSavedDownloads(get()) }
    factory<UpdateDownloadPriority> { UpdateDownloadPriority(get()) }
    factory<SubscribeDownloadsUseCase> { SubscribeDownloadsUseCase(get()) }

    factory<InsertDownload> { InsertDownload(get()) }
    factory<InsertDownloads> { InsertDownloads(get()) }
    factory<DeleteSavedDownload> { DeleteSavedDownload(get()) }
    
    factory<DownloadUseCases> {
        DownloadUseCases(
            downloadChapter = ireader.domain.usecases.download.DownloadChapterUseCase(get()),
            downloadChapters = ireader.domain.usecases.download.DownloadChaptersUseCase(get()),
            downloadUnreadChapters = ireader.domain.usecases.local.book_usecases.DownloadUnreadChaptersUseCase(get(), get(), get()),
            cancelDownload = ireader.domain.usecases.download.CancelDownloadUseCase(get()),
            pauseDownload = ireader.domain.usecases.download.PauseDownloadUseCase(get()),
            resumeDownload = ireader.domain.usecases.download.ResumeDownloadUseCase(get()),
            getDownloadStatus = ireader.domain.usecases.download.GetDownloadStatusUseCase(get()),
            subscribeDownloadsUseCase = get(),
            insertDownload = get(),
            insertDownloads = get(),
            deleteSavedDownload = get(),
            deleteAllSavedDownload = get(),
            deleteSavedDownloads = get(),
            updateDownloadPriority = get()
        )
    }
    
    // Translation use cases - factory (not needed at startup)
    factory { ireader.domain.usecases.translation.SaveTranslatedChapterUseCase(get()) }
    factory { ireader.domain.usecases.translation.GetTranslatedChapterUseCase(get()) }
    factory { ireader.domain.usecases.translation.DeleteTranslatedChapterUseCase(get()) }
    factory { ireader.domain.usecases.translation.GetAllTranslationsForChapterUseCase(get()) }
    factory { ireader.domain.usecases.translation.GetTranslatedChaptersByBookIdUseCase(get()) }
    factory { ireader.domain.usecases.translation.ApplyGlossaryToTextUseCase() }
    factory { 
        ireader.domain.usecases.translate.TranslateChapterWithStorageUseCase(
            translationEnginesManager = get(),
            saveTranslatedChapterUseCase = get(),
            getTranslatedChapterUseCase = get(),
            getGlossaryAsMapUseCase = get(),
            applyGlossaryToTextUseCase = get(),
            autoShareTranslationUseCase = get(),
            bookRepository = getOrNull()
        )
    }
    factory { 
        ireader.domain.usecases.translate.TranslateParagraphUseCase(
            translationEnginesManager = get()
        ) 
    }
    factory { 
        ireader.domain.usecases.translate.TranslateBookMetadataUseCase(
            translationEnginesManager = get(),
            translationPreferences = get(),
            readerPreferences = get(),
            translationQueueManager = get()
        ) 
    }
    
    // Glossary use cases - factory
    factory { ireader.domain.usecases.glossary.GetGlossaryByBookIdUseCase(get()) }
    factory { ireader.domain.usecases.glossary.GetGlossaryByTypeUseCase(get()) }
    factory { ireader.domain.usecases.glossary.SearchGlossaryUseCase(get()) }
    factory { ireader.domain.usecases.glossary.SaveGlossaryEntryUseCase(get()) }
    factory { ireader.domain.usecases.glossary.UpdateGlossaryEntryUseCase(get()) }
    factory { ireader.domain.usecases.glossary.DeleteGlossaryEntryUseCase(get()) }
    factory { ireader.domain.usecases.glossary.ExportGlossaryUseCase(get()) }
    factory { ireader.domain.usecases.glossary.ImportGlossaryUseCase(get()) }
    factory { ireader.domain.usecases.glossary.GetGlossaryAsMapUseCase(get()) }
    
    // Batch operations use cases - factory
    factory { ireader.domain.usecases.local.book_usecases.DownloadUnreadChaptersUseCase(get(), get(), get()) }
    factory { ireader.domain.usecases.local.book_usecases.ArchiveBookUseCase(get()) }
    
    // Smart categories use case - factory
    factory { ireader.domain.usecases.local.book_usecases.GetSmartCategoryBooksUseCase(get()) }
    
    // Statistics use cases - factory (not needed at startup)
    factory { GetReadingStatisticsUseCase(get()) }
    factory { TrackReadingProgressUseCase(get()) }
    factory { GetLibraryInsightsUseCase(get()) }
    factory { GetReadingAnalyticsUseCase(get()) }
    factory { GetUpcomingReleasesUseCase(get()) }
    factory { GetRecommendationsUseCase(get()) }
    factory { ExportStatisticsUseCase(get()) }
    factory { ApplyAdvancedFiltersUseCase(get()) }
    factory { GlobalSearchUseCase(get()) }
    factory { ireader.domain.usecases.statistics.SyncStatisticsUseCase(get()) }
    factory { StatisticsUseCases(
        getReadingStatistics = get(),
        trackReadingProgress = get(),
        syncStatistics = get()
    ) }
    
    // Chapter report use cases - factory
    factory { ireader.domain.usecases.chapter.ReportBrokenChapterUseCase(get()) }
    
    // Chapter health and repair use cases - factory
    factory { ireader.domain.services.ChapterHealthChecker() }
    factory { ireader.domain.usecases.chapter.AutoRepairChapterUseCase(
        chapterRepository = get(),
        chapterHealthRepository = get(),
        catalogStore = get(),
        chapterHealthChecker = get()
    ) }
    
    // Source report use cases - factory
    factory { ireader.domain.usecases.source.ReportBrokenSourceUseCase(get()) }
    
    // Source switching use cases - factory
    factory { ireader.domain.usecases.source.CheckSourceAvailabilityUseCase(
        bookRepository = get(),
        chapterRepository = get(),
        sourceComparisonRepository = get(),
        catalogStore = get()
    ) }
    factory { ireader.domain.usecases.source.MigrateToSourceUseCase(
        bookRepository = get(),
        chapterRepository = get(),
        historyRepository = get(),
        sourceComparisonRepository = get(),
        catalogStore = get(),
        migrateChaptersWithPreservation = get()
    ) }
    factory { ireader.domain.usecases.source.MigrateChaptersWithPreservationUseCase(
        chapterRepository = get(),
        historyRepository = get()
    ) }
    
    // Migration use cases - factory
    factory<ireader.domain.usecases.migration.BookMatcher> { ireader.domain.usecases.migration.BookMatcher() }
    factory<ireader.domain.usecases.migration.ChapterMapper> { ireader.domain.usecases.migration.ChapterMapper() }
    factory<ireader.domain.usecases.migration.SearchMigrationTargetsUseCase> { 
        ireader.domain.usecases.migration.SearchMigrationTargetsUseCase(get()) 
    }
    factory<ireader.domain.usecases.migration.MigrateBookUseCase> { 
        ireader.domain.usecases.migration.MigrateBookUseCase(
            bookRepository = get(),
            chapterRepository = get(),
            categoryRepository = get(),
            migrationRepository = get(),
            notificationRepository = get(),
            bookMatcher = get()
        )
    }
    factory<MigrateNovelUseCase> { 
      MigrateNovelUseCase(
            bookRepository = get(),
            chapterRepository = get(),
            catalogStore = get(),
            getRemoteBooksUseCase = get<RemoteUseCases>().getRemoteBooks,
            getRemoteChapters = get<RemoteUseCases>().getRemoteChapters,
            bookMatcher = get(),
            chapterMapper = get()
        ) 
    }
    
    // Font management use cases - factory
    factory { ireader.domain.usecases.fonts.SystemFontsInitializer(get()) }
    
    // Donation use cases - factory
    factory { ireader.domain.usecases.donation.DonationTriggerManager(get(), get()) }
    factory<ireader.domain.usecases.donation.GetFundingGoalsUseCase> { ireader.domain.usecases.donation.GetFundingGoalsUseCase(get()) }
    factory<ireader.domain.usecases.donation.UpdateFundingGoalUseCase> { ireader.domain.usecases.donation.UpdateFundingGoalUseCase(get()) }
    factory<ireader.domain.usecases.donation.DonationUseCases> { ireader.domain.usecases.donation.DonationUseCases(
        donationTriggerManager = get()
    ) }
    
    // ePub export use cases - factory
    factory { ireader.domain.usecases.epub.ExportNovelAsEpubUseCase(get()) }
    factory { ireader.domain.epub.EpubBuilder(httpClient = get(), fileSystem = get()) }
    factory { ireader.domain.usecases.epub.ExportBookAsEpubUseCase(
        findBookById = get<ireader.domain.usecases.local.LocalGetBookUseCases>().findBookById,
        chapterRepository = get(),
        translatedChapterRepository = get(),
        epubBuilder = get()
    ) }
    
    // Badge use cases
    factory { ireader.domain.usecases.badge.GetAvailableBadgesUseCase(get()) }
    factory { ireader.domain.usecases.badge.SubmitPaymentProofUseCase(get()) }
    factory { ireader.domain.usecases.badge.SetPrimaryBadgeUseCase(get()) }
    factory { ireader.domain.usecases.badge.SetFeaturedBadgesUseCase(get()) }
    
    // NFT use cases
    factory { ireader.domain.usecases.nft.SaveWalletAddressUseCase(get()) }
    factory { ireader.domain.usecases.nft.VerifyNFTOwnershipUseCase(get()) }
    factory { ireader.domain.usecases.nft.GetNFTVerificationStatusUseCase(get()) }
    factory { ireader.domain.usecases.nft.GetNFTMarketplaceUrlUseCase() }
    
    // Reading Buddy use cases - now uses unified database statistics (no preferences)
    factory { ireader.domain.usecases.quote.ReadingBuddyUseCases(get()) }
    factory { ireader.domain.usecases.quote.QuoteUseCases(get()) }
    
    // Explore Book use cases - for managing temporary explore/browse books
    factory { SaveExploreBook(get()) }
    factory { SaveExploreBooks(get()) }
    factory { GetExploreBook(get()) }
    factory { PromoteExploreBookToLibrary(get(), get(), get()) }
    factory { ClearExploreBooks(get()) }
    factory { ExploreBookUseCases(
        saveExploreBook = get(),
        saveExploreBooks = get(),
        getExploreBook = get(),
        promoteToLibrary = get(),
        clearExploreBooks = get()
    ) }
}