package ir.kazemcodes.infinity.domain.repository

import ir.kazemcodes.infinity.domain.local_feature.data.BookDao
import ir.kazemcodes.infinity.domain.local_feature.data.ChapterDao
import ir.kazemcodes.infinity.domain.local_feature.data.repository.LocalBookRepositoryImpl
import ir.kazemcodes.infinity.domain.local_feature.data.repository.LocalChapterRepositoryImpl
import ir.kazemcodes.infinity.domain.local_feature.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.domain.local_feature.domain.repository.LocalChapterRepository
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao
) : Repository {
    override val localBookRepository: LocalBookRepository
        get() = LocalBookRepositoryImpl(bookDao)
    override val localChapterRepository: LocalChapterRepository
        get() = LocalChapterRepositoryImpl(chapterDao)
}