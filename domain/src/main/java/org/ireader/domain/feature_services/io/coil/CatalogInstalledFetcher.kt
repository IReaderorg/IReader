package org.ireader.domain.feature_services.io.coil

import android.app.Application
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source
import org.ireader.domain.models.entities.CatalogInstalled
import java.io.File

class CatalogInstalledFetcher(context: Application) : Fetcher<CatalogInstalled> {

    private val pkgManager = context.packageManager

    override suspend fun fetch(
        pool: BitmapPool,
        data: CatalogInstalled,
        size: Size,
        options: Options,
    ): FetchResult {
        return when (data) {
            is CatalogInstalled.SystemWide -> {
                val icon = pkgManager.getApplicationIcon(data.pkgName)
                DrawableResult(icon, false, DataSource.DISK)
            }
            is CatalogInstalled.Locally -> {
                val file = File(data.installDir, "${data.pkgName}.png")
                val source = withContext(Dispatchers.IO) { file.source().buffer() }
                SourceResult(source, "image/png", DataSource.DISK)
            }
        }
    }

    override fun key(data: CatalogInstalled): String? {
        return data.pkgName
    }

}