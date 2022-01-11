package ir.kazemcodes.infinity.data.repository

import android.content.Context
import ir.kazemcodes.infinity.data.local.dao.BookDao
import ir.kazemcodes.infinity.data.local.dao.ChapterDao
import ir.kazemcodes.infinity.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val context: Context,
    private val remoteUseCase: RemoteUseCase,
    private val preferences: PreferencesHelper,
) : Repository {
    override val localBookRepository: LocalBookRepository
        get() = LocalBookRepositoryImpl(bookDao)
    override val localChapterRepository: LocalChapterRepository
        get() = LocalChapterRepositoryImpl(chapterDao)
    override val preferencesHelper: PreferencesHelper = preferences
    override val remoteRepository: RemoteRepository
        get() = RemoteRepositoryImpl(chapterDao, remoteUseCase)


}