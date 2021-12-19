package ir.kazemcodes.infinity.base_feature.repository

import ir.kazemcodes.infinity.local_feature.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.local_feature.domain.repository.LocalChapterRepository

interface Repository {

    val localBookRepository : LocalBookRepository

    val localChapterRepository : LocalChapterRepository

}