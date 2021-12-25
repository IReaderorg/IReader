package ir.kazemcodes.infinity.domain.utils

import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.data.network.sources

fun mappingApiNameToAPi(apiName: String): Source {
    var api = sources[0]
    sources.forEach { apiItem ->
        if (apiItem.name == apiName) {
            api = apiItem
        }
    }
    return api
}