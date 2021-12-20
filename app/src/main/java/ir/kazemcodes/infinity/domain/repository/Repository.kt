package ir.kazemcodes.infinity.domain.repository

import ir.kazemcodes.infinity.domain.local_feature.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.domain.local_feature.domain.repository.LocalChapterRepository

interface Repository {

    val localBookRepository : LocalBookRepository

    val localChapterRepository : LocalChapterRepository

}