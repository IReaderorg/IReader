package org.ireader.core.coil

import android.app.Application
import coil.ImageLoader
import coil.util.CoilUtils
import org.ireader.core.okhttp.HttpClients
import javax.inject.Inject

class CoilLoaderFactory @Inject constructor(
    private val context: Application,
    private val httpClients: HttpClients,
) {

    fun create(): ImageLoader {
        val coilCache = CoilUtils.createDefaultCache(context)

        val okhttpClient = httpClients.okhttpClient


        return ImageLoader.Builder(context)
            .okHttpClient(okhttpClient.newBuilder().cache(coilCache).build())
            .build()
    }

}