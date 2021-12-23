package ir.kazemcodes.infinity.library_feature.util

import ir.kazemcodes.infinity.api_feature.network.sources
import ir.kazemcodes.infinity.data.network.models.Source

fun mappingApiNameToAPi(apiName : String) : Source {
    var api = sources[0]
      sources.forEach { apiItem->
        if (apiItem.name == apiName) {
            api = apiItem
        }
    }
    return api
}