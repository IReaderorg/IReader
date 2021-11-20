package ir.kazemcodes.infinity.base_feature.repository

import ir.kazemcodes.infinity.explore_feature.data.ParsedHttpSource
import ir.kazemcodes.infinity.explore_feature.data.repository.RemoteRepositoryImpl
import ir.kazemcodes.infinity.explore_feature.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.library_feature.data.BookDao
import ir.kazemcodes.infinity.library_feature.data.repository.LocalRepositoryImpl
import ir.kazemcodes.infinity.library_feature.domain.repository.LocalRepository

class RepositoryImpl(private val api: ParsedHttpSource , private val dao : BookDao) : Repository {
    override val remote: RemoteRepository
        get() = RemoteRepositoryImpl(api)
    override val local: LocalRepository
        get() = LocalRepositoryImpl(dao)
}