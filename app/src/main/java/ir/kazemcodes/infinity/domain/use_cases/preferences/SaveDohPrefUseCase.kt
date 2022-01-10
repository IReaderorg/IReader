package ir.kazemcodes.infinity.domain.use_cases.preferences

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveDohPrefUseCase(
    private val repository: Repository,
) {
     operator fun invoke(dohPref: Int) {
         repository.preferencesHelper.dohStateKey.set(dohPref)
    }
}