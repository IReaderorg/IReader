package ir.kazemcodes.infinity.util

import android.content.Context
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.sources.Extensions
import uy.kohesive.injekt.injectLazy


class SourceMapper(private val context: Context) {

    fun mappingSourceNameToSource(apiName: String): Source {
        val extensions: Extensions by injectLazy()
        val sources = extensions.getSources()
        var source = extensions.getSources()[0]
        sources.forEach { apiItem ->
            if (apiItem.name == apiName) {
                source = apiItem
            }
        }
        return source
    }
}