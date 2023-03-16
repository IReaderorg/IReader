package ireader.presentation.imageloader.coil.imageloader

import com.seiko.imageloader.component.keyer.Keyer
import com.seiko.imageloader.component.mapper.Mapper
import com.seiko.imageloader.option.Options
import io.ktor.http.*
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote

class CatalogRemoteMapper : Mapper<Url> {
    override fun map(data: Any, options: Options): Url? {
        return if (data is CatalogRemote) {
            Url(data.iconUrl)
        } else if (data is CatalogInstalled){
            Url(data.iconUrl)
        } else {
            null
        }
    }
}
class CatalogKeyer : Keyer {
    override fun key(data: Any, options: Options): String? {
        return when(data) {
            is CatalogRemote -> data.iconUrl
            is CatalogLocal -> data.sourceId.toString()
            else -> null
        }
    }
}
