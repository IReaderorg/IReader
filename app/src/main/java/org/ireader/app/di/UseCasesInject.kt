package org.ireader.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.ireader.common_data.repository.LocalBookRepository
import org.ireader.common_data.repository.RemoteKeyRepository
import org.ireader.core_catalogs.CatalogPreferences
import org.ireader.core_catalogs.CatalogStore
import org.ireader.core_catalogs.interactor.GetCatalogsByType
import org.ireader.core_catalogs.interactor.GetLocalCatalog
import org.ireader.core_catalogs.interactor.GetLocalCatalogs
import org.ireader.core_catalogs.interactor.GetRemoteCatalogs
import org.ireader.core_catalogs.interactor.InstallCatalog
import org.ireader.core_catalogs.interactor.SyncRemoteCatalogs
import org.ireader.core_catalogs.interactor.TogglePinnedCatalog
import org.ireader.core_catalogs.interactor.UninstallCatalog
import org.ireader.core_catalogs.interactor.UpdateCatalog
import org.ireader.core_catalogs.service.CatalogInstaller
import org.ireader.core_catalogs.service.CatalogRemoteApi
import org.ireader.core_catalogs.service.CatalogRemoteRepository
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.FindBookByKey
import org.ireader.domain.use_cases.local.FindBooksByKey
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.local.SubscribeBooksByKey
import org.ireader.domain.use_cases.local.book_usecases.FindAllInLibraryBooks
import org.ireader.domain.use_cases.local.book_usecases.FindBookById
import org.ireader.domain.use_cases.local.book_usecases.FindBookByIds
import org.ireader.domain.use_cases.local.book_usecases.SubscribeBookById
import org.ireader.domain.use_cases.local.book_usecases.SubscribeInLibraryBooks
import org.ireader.domain.use_cases.local.chapter_usecases.FindAllInLibraryChapters
import org.ireader.domain.use_cases.local.chapter_usecases.FindChapterById
import org.ireader.domain.use_cases.local.chapter_usecases.FindChapterByIdByBatch
import org.ireader.domain.use_cases.local.chapter_usecases.FindChapterByKey
import org.ireader.domain.use_cases.local.chapter_usecases.FindChaptersByBookId
import org.ireader.domain.use_cases.local.chapter_usecases.FindChaptersByKey
import org.ireader.domain.use_cases.local.chapter_usecases.FindFirstChapter
import org.ireader.domain.use_cases.local.chapter_usecases.FindLastReadChapter
import org.ireader.domain.use_cases.local.chapter_usecases.SubscribeChapterById
import org.ireader.domain.use_cases.local.chapter_usecases.SubscribeChaptersByBookId
import org.ireader.domain.use_cases.local.chapter_usecases.SubscribeLastReadChapter
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllBooks
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteAllExploreBook
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteBookAndChapterByBookIds
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteBookById
import org.ireader.domain.use_cases.local.delete_usecases.book.DeleteBooks
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteAllChapters
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapterByChapter
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChapters
import org.ireader.domain.use_cases.local.delete_usecases.chapter.DeleteChaptersByBookId
import org.ireader.domain.use_cases.local.delete_usecases.chapter.UpdateChaptersUseCase
import org.ireader.domain.use_cases.local.insert_usecases.InsertBook
import org.ireader.domain.use_cases.local.insert_usecases.InsertBookAndChapters
import org.ireader.domain.use_cases.local.insert_usecases.InsertBooks
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapter
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapters
import org.ireader.domain.use_cases.remote.GetBookDetail
import org.ireader.domain.use_cases.remote.GetRemoteBooksUseCase
import org.ireader.domain.use_cases.remote.GetRemoteChapters
import org.ireader.domain.use_cases.remote.GetRemoteReadingContent
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.use_cases.remote.key.DeleteAllRemoteKeys
import org.ireader.image_loader.LibraryCovers
import org.ireader.sources.extension.CatalogsStateImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class UseCasesInject {


    @Provides
    @Singleton
    fun provideRemoteUseCase(
       libraryCovers: LibraryCovers,
    ): RemoteUseCases {
        return RemoteUseCases(
            getBookDetail = GetBookDetail(libraryCovers),
            getRemoteBooks = GetRemoteBooksUseCase(),
            getRemoteChapters = GetRemoteChapters(),
            getRemoteReadingContent = GetRemoteReadingContent(),
        )
    }
    @Provides
    @Singleton
    fun provideLocalInsertUseCases(
       localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository,
    localBookRepository: LocalBookRepository
    ): LocalInsertUseCases {
        return LocalInsertUseCases(
            insertBook = InsertBook(localBookRepository),
            insertBookAndChapters = InsertBookAndChapters(localBookRepository),
            insertBooks = InsertBooks(localBookRepository),
            insertChapter = InsertChapter(localChapterRepository),
            insertChapters = InsertChapters(localChapterRepository),
            updateChaptersUseCase = UpdateChaptersUseCase(localChapterRepository)
        )
    }


    @Provides
    @Singleton
    fun provideLocalGetBookUseCases(
        localBookRepository: LocalBookRepository
    ): LocalGetBookUseCases {
        return LocalGetBookUseCases(
          findAllInLibraryBooks = FindAllInLibraryBooks(localBookRepository),
            findBookById = FindBookById(localBookRepository),
            findBookByIds = FindBookByIds(localBookRepository),
            findBookByKey = FindBookByKey(localBookRepository),
            findBooksByKey = FindBooksByKey(localBookRepository),
            subscribeBookById = SubscribeBookById(localBookRepository),
            subscribeBooksByKey = SubscribeBooksByKey(localBookRepository),
            SubscribeInLibraryBooks = SubscribeInLibraryBooks(localBookRepository),
        )
    }

    @Provides
    @Singleton
    fun provideLocalChapterUseCase(
        localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository
    ): LocalGetChapterUseCase {
        return LocalGetChapterUseCase(
            findAllInLibraryChapters = FindAllInLibraryChapters(localChapterRepository),
            findChapterById = FindChapterById(localChapterRepository),
            findChapterByIdByBatch = FindChapterByIdByBatch(localChapterRepository),
            findChapterByKey = FindChapterByKey(localChapterRepository),
            findChaptersByBookId = FindChaptersByBookId(localChapterRepository),
            findChaptersByKey = FindChaptersByKey(localChapterRepository),
            findFirstChapter = FindFirstChapter(localChapterRepository),
            findLastReadChapter = FindLastReadChapter(localChapterRepository),
            subscribeChapterById = SubscribeChapterById(localChapterRepository),
            subscribeChaptersByBookId = SubscribeChaptersByBookId(localChapterRepository),
            subscribeLastReadChapter = SubscribeLastReadChapter(localChapterRepository)
        )
    }
    @Provides
    @Singleton
    fun provideDeleteUseCase(
        localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository,
        localBookRepository: LocalBookRepository,
        remoteKeyRepository: RemoteKeyRepository
    ): DeleteUseCase {
        return DeleteUseCase(
            deleteAllBook = DeleteAllBooks(localBookRepository),
            deleteAllChapters = DeleteAllChapters(localChapterRepository),
            deleteAllExploreBook = DeleteAllExploreBook(localBookRepository),
            deleteAllRemoteKeys = DeleteAllRemoteKeys(remoteKeyRepository),
            deleteBookAndChapterByBookIds = DeleteBookAndChapterByBookIds(localBookRepository),
            deleteBookById = DeleteBookById(localBookRepository),
            deleteBooks = DeleteBooks(localBookRepository),
            deleteChapterByChapter = DeleteChapterByChapter(localChapterRepository),
            deleteChapters = DeleteChapters(localChapterRepository),
            deleteChaptersByBookId = DeleteChaptersByBookId(localChapterRepository)
        )
    }

    /*** Catalog UseCase **/
    @Provides
    @Singleton
    fun providesCatalogsStateImpl(
    ): CatalogsStateImpl {
        return CatalogsStateImpl()
    }
    @Provides
    @Singleton
    fun providesGetCatalogsByType(
     localCatalogs: GetLocalCatalogs,
         remoteCatalogs: GetRemoteCatalogs,
    ): GetCatalogsByType {
        return GetCatalogsByType(localCatalogs, remoteCatalogs)
    }
    @Provides
    @Singleton
    fun providesGetRemoteCatalogs(
        catalogRemoteRepository: CatalogRemoteRepository,
    ): GetRemoteCatalogs {
        return GetRemoteCatalogs(catalogRemoteRepository)
    }
    @Provides
    @Singleton
    fun providesGetLocalCatalogs(
      catalogStore: CatalogStore,
       libraryRepository: LocalBookRepository,
    ): GetLocalCatalogs {
        return GetLocalCatalogs(catalogStore, libraryRepository)
    }
    @Provides
    @Singleton
    fun providesGetLocalCatalog(
     store: CatalogStore
    ): GetLocalCatalog {
        return GetLocalCatalog(store)
    }
    @Provides
    @Singleton
    fun providesUpdateCatalog(
       catalogRemoteRepository: CatalogRemoteRepository,
        installCatalog: InstallCatalog,
    ): UpdateCatalog {
        return UpdateCatalog(catalogRemoteRepository, installCatalog)
    }
    @Provides
    @Singleton
    fun providesInstallCatalog(
     catalogInstaller: CatalogInstaller,
    ): InstallCatalog {
        return InstallCatalog(catalogInstaller)
    }
    @Provides
    @Singleton
    fun providesUninstallCatalog(
       catalogInstaller: CatalogInstaller,
    ): UninstallCatalog {
        return UninstallCatalog(catalogInstaller)
    }

    @Provides
    @Singleton
    fun providesTogglePinnedCatalog(
     store: CatalogStore,
    ): TogglePinnedCatalog {
        return TogglePinnedCatalog(store)
    }
    @Provides
    @Singleton
    fun providesSyncRemoteCatalogs(
     catalogRemoteRepository: CatalogRemoteRepository,
      catalogRemoteApi: CatalogRemoteApi,
      catalogPreferences: CatalogPreferences,
    ): SyncRemoteCatalogs {
        return SyncRemoteCatalogs(catalogRemoteRepository, catalogRemoteApi, catalogPreferences)
    }


}