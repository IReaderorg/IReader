package ireader.presentation.imageloader

import android.graphics.drawable.Drawable
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.fetch.SourceFetchResult
import coil3.key.Keyer
import coil3.request.Options
import coil3.toUri
import ireader.domain.models.entities.CatalogInstalled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.buffer
import okio.source
import java.io.File

class CatalogInstalledFetcher(
    private val data: CatalogInstalled,
    private val packageManger: PackageManager
) : Fetcher, Keyer<CatalogInstalled> {


    @OptIn(ExperimentalCoilApi::class)
    override suspend fun fetch(): FetchResult? {
        return when (data) {
            is CatalogInstalled.SystemWide -> {
                // For system-wide installations, try to use app icon if no iconUrl
                if (data.iconUrl.isBlank()) {
                    try {
                        val icon = packageManger.getApplicationIcon(data.pkgName) as Drawable
                        return ImageFetchResult(image = icon.asImage(), isSampled = true, DataSource.DISK)
                    } catch (e: Exception) {
                        // If app icon fails, return null to let mapper handle iconUrl
                        return null
                    }
                }
                // If iconUrl exists, return null to let Coil's HTTP fetcher handle it via mapper
                null
            }

            is CatalogInstalled.Locally -> {
                // Return null to let Coil's HTTP fetcher handle the iconUrl via mapper
                // This uses Coil's built-in caching instead of manual file management
                null
            }
            
            is ireader.domain.models.entities.JSPluginCatalog -> {
                // Return null to let Coil's HTTP fetcher handle the iconUrl via mapper
                // This uses Coil's built-in caching instead of manual file management
                null
            }
        }
    }

    class Factory(
        private val packageManager: PackageManager
    ) : Fetcher.Factory<CatalogInstalled> {

        override fun create(
            data: CatalogInstalled,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher? {
            return CatalogInstalledFetcher(data, packageManager)
        }
    }

    override fun key(data: CatalogInstalled, options: Options): String? {
        return data.pkgName
    }
}
