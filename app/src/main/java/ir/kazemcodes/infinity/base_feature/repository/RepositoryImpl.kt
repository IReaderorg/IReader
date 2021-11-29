package ir.kazemcodes.infinity.base_feature.repository

import ir.kazemcodes.infinity.api_feature.ParsedHttpSource
import ir.kazemcodes.infinity.explore_feature.data.repository.RemoteRepositoryImpl
import ir.kazemcodes.infinity.explore_feature.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.library_feature.data.BookDao
import ir.kazemcodes.infinity.library_feature.data.ChapterDao
import ir.kazemcodes.infinity.library_feature.data.repository.LocalBookRepositoryImpl
import ir.kazemcodes.infinity.library_feature.data.repository.LocalChapterRepositoryImpl
import ir.kazemcodes.infinity.library_feature.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.library_feature.domain.repository.LocalChapterRepository
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val api: ParsedHttpSource,
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao
) : Repository {
    override val remote: RemoteRepository
        get() = RemoteRepositoryImpl(api)
    override val localBookRepository: LocalBookRepository
        get() = LocalBookRepositoryImpl(bookDao)
    override val localChapterRepository: LocalChapterRepository
        get() = LocalChapterRepositoryImpl(chapterDao)
}