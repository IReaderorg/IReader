package ir.kazemcodes.infinity.domain.utils

import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.data.network.sources
import timber.log.Timber

fun mappingApiNameToAPi(apiName : String) : Source {
    var api = sources[0]
      sources.forEach { apiItem->
        if (apiItem.name == apiName) {
            api = apiItem
        }
          Timber.d("Timber: TEST"+ api.baseUrl , api.name, apiName)
    }
    return api
}