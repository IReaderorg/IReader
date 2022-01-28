package ir.kazemcodes.infinity.core.domain.use_cases.preferences.services

import ir.kazemcodes.infinity.core.domain.repository.Repository

class SetLastUpdateTime(
    private val repository: Repository,
) {
    operator fun invoke(time: Long) {
        repository.preferencesHelper.lastUpdateCheck.set(time)
    }
}

class ReadLastUpdateTime(
    private val repository: Repository,
) {
    operator fun invoke() : Long {
        return repository.preferencesHelper.lastUpdateCheck.get()
    }
}