package ir.kazemcodes.infinity.data.repository

import android.content.Context
import ir.kazemcodes.infinity.data.local.dao.BookDao
import ir.kazemcodes.infinity.data.local.dao.ChapterDao
import ir.kazemcodes.infinity.domain.repository.DataStoreHelper
import ir.kazemcodes.infinity.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.domain.repository.Repository
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val context: Context,
) : Repository {
    override val localBookRepository: LocalBookRepository
        get() = LocalBookRepositoryImpl(bookDao)
    override val localChapterRepository: LocalChapterRepository
        get() = LocalChapterRepositoryImpl(chapterDao)
    override val dataStoreRepository: DataStoreHelper
        get() = DataStoreHelperImpl(context)
}