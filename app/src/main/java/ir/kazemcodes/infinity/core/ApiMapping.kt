package ir.kazemcodes.infinity.library_feature.util

import ir.kazemcodes.infinity.api_feature.HttpSource
import ir.kazemcodes.infinity.api_feature.network.apis

fun mappingApiNameToAPi(apiName : String) : HttpSource {
    var api = apis[0]
      apis.forEach {apiItem->
        if (apiItem.name == apiName) {
            api = apiItem
        }
    }
    return api
}