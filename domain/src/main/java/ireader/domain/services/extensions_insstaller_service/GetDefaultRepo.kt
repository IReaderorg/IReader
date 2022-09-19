package ireader.domain.services.extensions_insstaller_service

import ireader.domain.data.repository.CatalogSourceRepository
import ireader.domain.models.entities.ExtensionSource
import ireader.domain.preferences.prefs.UiPreferences
import org.koin.core.annotation.Factory

@Factory
class GetDefaultRepo(
    private val uiPreferences: UiPreferences,
    private val repository: CatalogSourceRepository
) {
    suspend operator fun invoke(): ExtensionSource {
        val defaultRepo = uiPreferences.defaultRepository().get()
        return repository.find(defaultRepo)?.takeIf { it.id >= 0 } ?: ExtensionSource.default()
    }
}
