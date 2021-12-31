package ir.kazemcodes.infinity.util

import android.content.Context
import ir.kazemcodes.infinity.data.network.Extensions
import ir.kazemcodes.infinity.data.network.models.Source
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.instance

fun mappingApiNameToAPi(apiName: String,context: Context): Source {
    val di : DI by closestDI(context)
    val extensions : Extensions by di.instance<Extensions>()
    val sources = extensions.getSources()
    var source = extensions.getSources()[0]
    sources.forEach { apiItem ->
        if (apiItem.name == apiName) {
            source = apiItem
        }
    }
    return source
}