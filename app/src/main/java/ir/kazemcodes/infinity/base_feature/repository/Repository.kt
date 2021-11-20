package ir.kazemcodes.infinity.base_feature.repository

import ir.kazemcodes.infinity.explore_feature.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.library_feature.domain.repository.LocalRepository

interface Repository {
    val remote : RemoteRepository

    val local : LocalRepository

}