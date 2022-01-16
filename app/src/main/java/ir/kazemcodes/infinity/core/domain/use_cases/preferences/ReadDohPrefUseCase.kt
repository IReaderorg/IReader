package ir.kazemcodes.infinity.core.domain.use_cases.preferences

import ir.kazemcodes.infinity.core.domain.repository.Repository

class ReadDohPrefUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Int {
        return repository.preferencesHelper.dohStateKey.get()
    }
}