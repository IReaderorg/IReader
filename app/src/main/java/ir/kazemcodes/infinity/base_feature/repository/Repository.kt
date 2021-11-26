package ir.kazemcodes.infinity.base_feature.repository

import ir.kazemcodes.infinity.explore_feature.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.library_feature.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.library_feature.domain.repository.LocalChapterRepository

interface Repository {
    val remote : RemoteRepository

    val localBookRepository : LocalBookRepository

    val localChapterRepository : LocalChapterRepository

}