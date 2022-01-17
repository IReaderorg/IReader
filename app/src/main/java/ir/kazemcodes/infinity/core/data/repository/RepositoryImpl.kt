package ir.kazemcodes.infinity.core.data.repository

import ir.kazemcodes.infinity.core.data.local.BookDatabase
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.domain.repository.Repository
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    override val localBookRepository: LocalBookRepository,
    override val localChapterRepository: LocalChapterRepository,
    override val preferencesHelper: PreferencesHelper,
    override val remoteRepository: RemoteRepository,
    override val database: BookDatabase
) : Repository {


}