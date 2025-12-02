package ireader.domain.di



import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.download.delete.DeleteAllSavedDownload
import ireader.domain.usecases.download.delete.DeleteSavedDownload
import ireader.domain.usecases.download.delete.DeleteSavedDownloads
import ireader.domain.usecases.download.get.SubscribeDownloadsUseCase
import ireader.domain.usecases.download.insert.InsertDownload
import ireader.domain.usecases.download.insert.InsertDownloads
import ireader.domain.usecases.download.update.UpdateDownloadPriority
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
import ireader.domain.usecases.updates.SubscribeUpdates
import ireader.domain.usecases.updates.UpdateUseCases
import org.koin.dsl.module


val UseCasesInject = module {

    single<RemoteUseCases> { RemoteUseCases(
        getBookDetail = GetBookDetail(),
        getRemoteBooks = GetRemoteBooksUseCase(),
        getRemoteChapters = GetRemoteChapters(),
        getRemoteReadingContent = GetRemoteReadingContent(),
        globalSearch = get(),
    ) }
    single<ireader.domain.usecases.reader.PreloadChapterUseCase> { ireader.domain.usecases.reader.PreloadChapterUseCase() }
    single<ireader.domain.usecases.reader.ApplyDefaultReadingModeUseCase> { ireader.domain.usecases.reader.ApplyDefaultReadingModeUseCase(get(), get()) }
    single<ireader.domain.usecases.local.LocalInsertUseCases> {
        ireader.domain.usecases.local.LocalInsertUseCases(
            insertBook = InsertBook(get()),
            insertBookAndChapters = InsertBookAndChapters(get()),
            insertBooks = InsertBooks(get()),
            insertChapter = InsertChapter(get()),
            insertChapters = InsertChapters(get()),
            updateBook = UpdateBook(get())
        )
    }
    single<ireader.domain.usecases.local.LocalGetBookUseCases> {
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
    single<ireader.domain.usecases.local.LocalGetChapterUseCase> {
        ireader.domain.usecases.local.LocalGetChapterUseCase(
            findAllInLibraryChapters = FindAllInLibraryChapters(get()),
            findChapterById = FindChapterById(get()),
            findChaptersByBookId = FindChaptersByBookId(get()),
            subscribeChaptersByBookId = SubscribeChaptersByBookId(get()),
            updateLastReadTime = UpdateLastReadTime(
                insertUseCases = get(),
                historyUseCase = get(),
                uiPreferences = get()
            ),
            subscribeChapterById = SubscribeChapterById(get())
        )
    }
    single<ireader.domain.usecases.local.DeleteUseCase> {
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

    single<LibraryScreenPrefUseCases> { LibraryScreenPrefUseCases(
        libraryLayoutTypeUseCase = LibraryLayoutTypeUseCase(get(), get()),
        sortersDescUseCase = SortersDescUseCase(get()),
        sortersUseCase = SortersUseCase(get())
    ) }
    single<ReaderPrefUseCases> { ReaderPrefUseCases(
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
    single<BrowseScreenPrefUseCase> { BrowseScreenPrefUseCase(
        browseLayoutTypeUseCase = BrowseLayoutTypeUseCase(get())
    ) }
    single<HistoryUseCase> { HistoryUseCase(
        get()
    ) }
    single<ireader.domain.usecases.history.GetLastReadNovelUseCase> { 
        ireader.domain.usecases.history.GetLastReadNovelUseCase(
            historyRepository = get(),
            bookRepository = get(),
            chapterRepository = get()
        ) 
    }
    single<UpdateUseCases> { UpdateUseCases(
        subscribeUpdates = SubscribeUpdates(get()),
        deleteAllUpdates = DeleteAllUpdates(get()),
    ) }
    
    // Download use cases
    single<DeleteAllSavedDownload> { DeleteAllSavedDownload(get()) }
    single<DeleteSavedDownloads> { DeleteSavedDownloads(get()) }
    single<UpdateDownloadPriority> { UpdateDownloadPriority(get()) }
    single<SubscribeDownloadsUseCase> { SubscribeDownloadsUseCase(get()) }

    single<InsertDownload> { InsertDownload(get()) }
    single<InsertDownloads> { InsertDownloads(get()) }
    single<DeleteSavedDownload> { DeleteSavedDownload(get()) }
    
    single<DownloadUseCases> {
        DownloadUseCases(
            downloadChapter = ireader.domain.usecases.download.DownloadChapterUseCase(get()),
            downloadChapters = ireader.domain.usecases.download.DownloadChaptersUseCase(get()),
            downloadUnreadChapters = ireader.domain.usecases.local.book_usecases.DownloadUnreadChaptersUseCase(get(), get()),
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
    
    // Translation use cases
    single { ireader.domain.usecases.translation.SaveTranslatedChapterUseCase(get()) }
    single { ireader.domain.usecases.translation.GetTranslatedChapterUseCase(get()) }
    single { ireader.domain.usecases.translation.DeleteTranslatedChapterUseCase(get()) }
    single { ireader.domain.usecases.translation.GetAllTranslationsForChapterUseCase(get()) }
    single { ireader.domain.usecases.translation.ApplyGlossaryToTextUseCase() }
    single { 
        ireader.domain.usecases.translate.TranslateChapterWithStorageUseCase(
            translationEnginesManager = get(),
            saveTranslatedChapterUseCase = get(),
            getTranslatedChapterUseCase = get(),
            getGlossaryAsMapUseCase = get(),
            applyGlossaryToTextUseCase = get()
        ) 
    }
    single { 
        ireader.domain.usecases.translate.TranslateParagraphUseCase(
            translationEnginesManager = get()
        ) 
    }
    
    // Glossary use cases
    single { ireader.domain.usecases.glossary.GetGlossaryByBookIdUseCase(get()) }
    single { ireader.domain.usecases.glossary.GetGlossaryByTypeUseCase(get()) }
    single { ireader.domain.usecases.glossary.SearchGlossaryUseCase(get()) }
    single { ireader.domain.usecases.glossary.SaveGlossaryEntryUseCase(get()) }
    single { ireader.domain.usecases.glossary.UpdateGlossaryEntryUseCase(get()) }
    single { ireader.domain.usecases.glossary.DeleteGlossaryEntryUseCase(get()) }
    single { ireader.domain.usecases.glossary.ExportGlossaryUseCase(get()) }
    single { ireader.domain.usecases.glossary.ImportGlossaryUseCase(get()) }
    single { ireader.domain.usecases.glossary.GetGlossaryAsMapUseCase(get()) }
    
    // Batch operations use cases
    single { ireader.domain.usecases.local.book_usecases.DownloadUnreadChaptersUseCase(get(), get()) }
    single { ireader.domain.usecases.local.book_usecases.ArchiveBookUseCase(get()) }
    
    // Smart categories use case
    single { ireader.domain.usecases.local.book_usecases.GetSmartCategoryBooksUseCase(get()) }
    
    // Statistics use cases
    single { GetReadingStatisticsUseCase(get()) }
    single { TrackReadingProgressUseCase(get()) }
    single { GetLibraryInsightsUseCase(get()) }
    single { GetReadingAnalyticsUseCase(get()) }
    single { GetUpcomingReleasesUseCase(get()) }
    single { GetRecommendationsUseCase(get()) }
    single { ExportStatisticsUseCase(get()) }
    single { ApplyAdvancedFiltersUseCase(get()) }
    single { GlobalSearchUseCase(get()) }
    single { ireader.domain.usecases.statistics.SyncStatisticsUseCase(get()) }
    single { StatisticsUseCases(
        getReadingStatistics = get(),
        trackReadingProgress = get(),
        syncStatistics = get()
    ) }
    
    // Chapter report use cases
    single { ireader.domain.usecases.chapter.ReportBrokenChapterUseCase(get()) }
    
    // Chapter health and repair use cases
    single { ireader.domain.services.ChapterHealthChecker() }
    single { ireader.domain.usecases.chapter.AutoRepairChapterUseCase(
        chapterRepository = get(),
        chapterHealthRepository = get(),
        catalogStore = get(),
        chapterHealthChecker = get()
    ) }
    
    // Source report use cases
    single { ireader.domain.usecases.source.ReportBrokenSourceUseCase(get()) }
    
    // Source switching use cases
    single { ireader.domain.usecases.source.CheckSourceAvailabilityUseCase(
        bookRepository = get(),
        chapterRepository = get(),
        sourceComparisonRepository = get(),
        catalogStore = get()
    ) }
    single { ireader.domain.usecases.source.MigrateToSourceUseCase(
        bookRepository = get(),
        chapterRepository = get(),
        sourceComparisonRepository = get(),
        catalogStore = get()
    ) }
    
    // Migration use cases
    single<ireader.domain.usecases.migration.BookMatcher> { ireader.domain.usecases.migration.BookMatcher() }
    single<ireader.domain.usecases.migration.ChapterMapper> { ireader.domain.usecases.migration.ChapterMapper() }
    single<ireader.domain.usecases.migration.SearchMigrationTargetsUseCase> { 
        ireader.domain.usecases.migration.SearchMigrationTargetsUseCase(get()) 
    }
    single<ireader.domain.usecases.migration.MigrateBookUseCase> { 
        ireader.domain.usecases.migration.MigrateBookUseCase(
            bookRepository = get(),
            chapterRepository = get(),
            categoryRepository = get(),
            migrationRepository = get(),
            notificationRepository = get(),
            bookMatcher = get()
        )
    }
    single<MigrateNovelUseCase> { 
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
    
    // Font management use cases
    single { ireader.domain.usecases.fonts.SystemFontsInitializer(get()) }
    
    // Donation use cases
    single { ireader.domain.usecases.donation.DonationTriggerManager(get(), get()) }
    single<ireader.domain.usecases.donation.OpenWalletUseCase> { ireader.domain.usecases.donation.OpenWalletUseCase(get()) }
    single<ireader.domain.usecases.donation.CheckWalletInstalledUseCase> { ireader.domain.usecases.donation.CheckWalletInstalledUseCase(get()) }
    single<ireader.domain.usecases.donation.CopyAddressUseCase> { ireader.domain.usecases.donation.CopyAddressUseCase(get()) }
    single<ireader.domain.usecases.donation.GeneratePaymentUriUseCase> { ireader.domain.usecases.donation.GeneratePaymentUriUseCase(get()) }
    single<ireader.domain.usecases.donation.GetFundingGoalsUseCase> { ireader.domain.usecases.donation.GetFundingGoalsUseCase(get()) }
    single<ireader.domain.usecases.donation.UpdateFundingGoalUseCase> { ireader.domain.usecases.donation.UpdateFundingGoalUseCase(get()) }
    single<ireader.domain.usecases.donation.DonationUseCases> { ireader.domain.usecases.donation.DonationUseCases(
        donationTriggerManager = get(),
        openWallet = get(),
        checkWalletInstalled = get(),
        copyAddress = get(),
        generatePaymentUri = get()
    ) }
    
    // ePub export use cases
    single { ireader.domain.usecases.epub.ExportNovelAsEpubUseCase(get()) }
    single { ireader.domain.epub.EpubBuilder(httpClient = get()) }
    single { ireader.domain.usecases.epub.ExportBookAsEpubUseCase(
        findBookById = get<ireader.domain.usecases.local.LocalGetBookUseCases>().findBookById,
        chapterRepository = get(),
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
    
    // Reading Buddy use cases
    factory { ireader.domain.usecases.quote.ReadingBuddyUseCases(get()) }
    factory { ireader.domain.usecases.quote.QuoteUseCases(get()) }
}