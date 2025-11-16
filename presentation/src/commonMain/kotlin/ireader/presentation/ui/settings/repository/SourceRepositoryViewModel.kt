package ireader.presentation.ui.settings.repository

import androidx.compose.runtime.mutableStateOf
import ireader.domain.data.repository.CatalogSourceRepository
import ireader.domain.models.entities.ExtensionSource
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel



class SourceRepositoryViewModel(
    val catalogSourceRepository: CatalogSourceRepository,
    val uiPreferences: UiPreferences
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {

    val sources = catalogSourceRepository.subscribe().asState(emptyList())
    var showAutomaticSourceDialog = mutableStateOf(false)

    val default= uiPreferences.defaultRepository().asState()


    /**     https://raw.githubusercontent.com/IReaderorg/IReader-extensions/repo/index.min.json?name=IReader 2?owner=KazemCodes?source=https://github.com/IReaderorg/IReader"
     *
     */
    companion object {
        const val NAME = "?name="
        const val OWNER = "owner="
        const val SOURCE = "source="
        const val Separator = "?"
    }
    fun parseUrl(url:String) : ExtensionSource {
        val key = url.substringBefore(NAME,"").takeIf { it.isNotBlank() } ?: throw Exception()
        val name = url.substringAfter(NAME,"")
            .substringBefore(Separator,"").takeIf { it.isNotBlank() } ?: throw Exception()
        val owner = url.substringAfter(OWNER,"")
            .substringBefore(Separator,"") .takeIf { it.isNotBlank() } ?: throw Exception()
        val source = url.substringAfter(SOURCE,"")
            .takeIf { it.isNotBlank() } ?: throw Exception()
        return ExtensionSource(0,name,key,owner,source,null,null,0,true,"IREADER")
    }
}