package org.ireader.domain.use_cases.local.chapter_usecases

import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalChapterRepository
import javax.inject.Inject

class FindChaptersByKey @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(key: String): List<Chapter> {
        return localChapterRepository.findChaptersByKey(key)
    }

}

class FindChapterByKey @Inject constructor(private val localChapterRepository: LocalChapterRepository) {
    suspend operator fun invoke(key: String): Chapter? {
        return localChapterRepository.findChapterByKey(key)
    }

}
