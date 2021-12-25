package ir.kazemcodes.infinity.domain.use_cases.datastore

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveLatestChapterUseCase(
    private val repository: Repository,
) {
    suspend operator fun invoke(latestReadChapter: String) {
        return repository.dataStoreRepository.saveLatestChapterUseCase(latestReadChapter)
    }
}