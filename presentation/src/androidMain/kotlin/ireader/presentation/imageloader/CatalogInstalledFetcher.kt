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
                if (data.iconUrl.isNotBlank()) {
                    val file = File(data.installDir, "${data.pkgName}.png")
                    val source = withContext(Dispatchers.IO) { file.source().buffer() }
                    SourceFetchResult(
                        source = ImageSource(source = source, fileSystem = FileSystem.SYSTEM),
                        mimeType = "image/*",
                        dataSource = DataSource.DISK,
                    )
                } else {
                    val icon = packageManger.getApplicationIcon(data.pkgName) as Drawable
                    ImageFetchResult(image = icon.asImage(),isSampled = true,DataSource.DISK)
                }
            }

            is CatalogInstalled.Locally -> {
                val file = File(data.installDir, "${data.pkgName}.png")
                val source = withContext(Dispatchers.IO) { file.source().buffer() }
                return SourceFetchResult(
                    source = ImageSource(source = source, fileSystem = FileSystem.SYSTEM),
                    mimeType = "image/*",
                    dataSource = DataSource.DISK,
                )
            }
            
            is ireader.domain.models.entities.JSPluginCatalog -> {
                // For JS plugins, try to load icon from local file first
                val file = File(data.installDir, "${data.pkgName}.png")
                if (file.exists() && file.length() > 0) {
                    val source = withContext(Dispatchers.IO) { file.source().buffer() }
                    return SourceFetchResult(
                        source = ImageSource(source = source, fileSystem = FileSystem.SYSTEM),
                        mimeType = "image/*",
                        dataSource = DataSource.DISK,
                    )
                }
                
                // If local file doesn't exist but we have an iconUrl, 
                // return null to let Coil's default HTTP fetcher handle it via the mapper
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
