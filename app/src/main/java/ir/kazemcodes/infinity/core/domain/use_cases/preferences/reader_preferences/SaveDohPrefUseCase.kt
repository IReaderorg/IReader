package ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences

import ir.kazemcodes.infinity.core.domain.repository.Repository

class SaveDohPrefUseCase(
    private val repository: Repository,
) {
    operator fun invoke(dohPref: Int) {
        repository.preferencesHelper.dohStateKey.set(dohPref)
    }
}