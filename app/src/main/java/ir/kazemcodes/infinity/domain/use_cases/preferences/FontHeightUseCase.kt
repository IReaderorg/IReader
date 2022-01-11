package ir.kazemcodes.infinity.domain.use_cases.preferences

import ir.kazemcodes.infinity.domain.repository.Repository

class SaveFontHeightUseCase(
    private val repository: Repository,
) {
    operator fun invoke(fontHeight : Int) {
        repository.preferencesHelper.fontHeight.set(fontHeight)
    }
}
class  ReadFontHeightUseCase(
    private val repository: Repository,
) {
    operator fun invoke(): Int {
        return repository.preferencesHelper.fontHeight.get()
    }
}
