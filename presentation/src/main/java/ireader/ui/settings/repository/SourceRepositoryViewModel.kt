package ireader.ui.settings.repository

import androidx.compose.runtime.mutableStateOf
import ireader.common.models.entities.ExtensionSource
import ireader.ui.core.viewmodel.BaseViewModel
import ireader.domain.data.repository.CatalogSourceRepository
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SourceRepositoryViewModel(
    val catalogSourceRepository: CatalogSourceRepository
) : BaseViewModel() {

    val sources = catalogSourceRepository.subscribe().asState(emptyList())

    val default= mutableStateOf(false)


    /**     "www.google.com#name=ireader#owner=kazemcodes#source=githib##"
     *
     */
    companion object {
        const val NAME = "#name="
        const val OWNER = "#owner="
        const val SOURCE = "#owner="
    }
    fun parseUrl(url:String) : ExtensionSource {
        val key = url.substringBefore(NAME).takeIf { it.isNotBlank() } ?: throw Exception()
        val name = url.substringAfter(NAME).substringBefore("#").takeIf { it.isNotBlank() } ?: throw Exception()
        val owner = url.substringAfter(OWNER).substringBefore("#") .takeIf { it.isNotBlank() } ?: throw Exception()
        val source = url.substringAfter(SOURCE).substringBefore("##").takeIf { it.isNotBlank() } ?: throw Exception()
        return ExtensionSource(0,name,key,owner,source,0,true)
    }
}