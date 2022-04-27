package org.ireader.domain.use_cases.local.chapter_usecases

import org.ireader.common_models.entities.Chapter
import org.ireader.common_data.repository.LocalChapterRepository
import javax.inject.Inject

class FindChaptersByKey @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(key: String): List<Chapter> {
        return localChapterRepository.findChaptersByKey(key)
    }

}

class FindChapterByKey @Inject constructor(private val localChapterRepository: org.ireader.common_data.repository.LocalChapterRepository) {
    suspend operator fun invoke(key: String): Chapter? {
        return localChapterRepository.findChapterByKey(key)
    }

}
