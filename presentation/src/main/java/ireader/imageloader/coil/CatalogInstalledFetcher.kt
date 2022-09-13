package ireader.imageloader.coil

import android.content.Context
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.key.Keyer
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source
import ireader.common.models.entities.CatalogInstalled
import java.io.File

class CatalogInstalledFetcher(
    private val context: Context,
    private val data: CatalogInstalled
) : Fetcher, Keyer<CatalogInstalled> {

    private val pkgManager = context.packageManager
    override suspend fun fetch(): FetchResult? {
        return when (data) {
            is CatalogInstalled.SystemWide -> {
                val icon = pkgManager.getApplicationIcon(data.pkgName)
                DrawableResult(icon, false, DataSource.DISK)
            }
            is CatalogInstalled.Locally -> {
                val file = File(data.installDir, "${data.pkgName}.png")
                val source = withContext(Dispatchers.IO) { file.source().buffer() }
                SourceResult(ImageSource(source, context), "image/png", DataSource.DISK)
            }
        }
    }

    class Factory : Fetcher.Factory<CatalogInstalled> {
        override fun create(data: CatalogInstalled, options: Options, imageLoader: ImageLoader): Fetcher {
            return CatalogInstalledFetcher(options.context, data)
        }
    }

    override fun key(data: CatalogInstalled, options: Options): String? {
        return data.pkgName
    }
}
