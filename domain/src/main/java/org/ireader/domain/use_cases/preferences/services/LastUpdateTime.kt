package org.ireader.infinity.core.domain.use_cases.preferences.services

import org.ireader.domain.repository.Repository

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
    operator fun invoke(): Long {
        return repository.preferencesHelper.lastUpdateCheck.get()
    }
}