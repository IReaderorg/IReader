package ireader.ui.settings.repository

import ireader.domain.data.repository.CatalogSourceRepository
import ireader.domain.models.entities.ExtensionSource
import ireader.domain.preferences.prefs.UiPreferences
import ireader.ui.core.viewmodel.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SourceRepositoryViewModel(
    val catalogSourceRepository: CatalogSourceRepository,
    val uiPreferences: UiPreferences
) : BaseViewModel() {

    val sources = catalogSourceRepository.subscribe().asState(emptyList())

    val default= uiPreferences.defaultRepository().asState()


    /**     "www.google.com#name=ireader#owner=kazemcodes#source=githib##"
     *
     */
    companion object {
        const val NAME = "#name="
        const val OWNER = "#owner="
        const val SOURCE = "#owner="
    }
    fun parseUrl(url:String) : ExtensionSource {
        val key = url.substringBefore(NAME,"").takeIf { it.isNotBlank() } ?: throw Exception()
        val name = url.substringAfter(NAME,"")
            .substringBefore("#","").takeIf { it.isNotBlank() } ?: throw Exception()
        val owner = url.substringAfter(OWNER,"")
            .substringBefore("#","") .takeIf { it.isNotBlank() } ?: throw Exception()
        val source = url.substringAfter(SOURCE,"")
            .substringBefore("##","").takeIf { it.isNotBlank() } ?: throw Exception()
        return ExtensionSource(0,name,key,owner,source,0,true)
    }
}