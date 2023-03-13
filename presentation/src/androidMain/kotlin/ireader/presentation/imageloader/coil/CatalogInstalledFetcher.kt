package ireader.presentation.imageloader.coil

import android.graphics.drawable.Drawable
import com.seiko.imageloader.component.fetcher.FetchResult
import com.seiko.imageloader.component.fetcher.Fetcher
import com.seiko.imageloader.component.keyer.Keyer
import com.seiko.imageloader.option.Options
import ireader.domain.models.entities.CatalogInstalled
import ireader.presentation.imageloader.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.source
import java.io.File

class CatalogInstalledFetcher(
        private val data: CatalogInstalled,
        private val packageManger: PackageManager
) : Fetcher, Keyer {


    override suspend fun fetch(): FetchResult? {
        return when (data) {
            is CatalogInstalled.SystemWide -> {
                if (data.iconUrl.isNotBlank()) {
                    val file = File(data.installDir, "${data.pkgName}.png")
                    val source = withContext(Dispatchers.IO) { file.source().buffer() }
                    FetchResult.Source(source)
                } else {
                    val icon = packageManger.getApplicationIcon(data.pkgName) as Drawable
                    FetchResult.Image(com.seiko.imageloader.Image(icon))
                }
            }
            is CatalogInstalled.Locally -> {
                val file = File(data.installDir, "${data.pkgName}.png")
                val source = withContext(Dispatchers.IO) { file.source().buffer() }
                FetchResult.Source(source)
            }

        }
    }

    class Factory(
            private val packageManager: PackageManager
    ) : Fetcher.Factory {
        override fun create(data: Any, options: Options): Fetcher? {
            return if (data is CatalogInstalled) {
                CatalogInstalledFetcher(data, packageManager)
            } else {
                null
            }
        }
    }

    override fun key(data: Any, options: Options): String? {
        return if (data is CatalogInstalled) {
            data.pkgName
        } else {
            null
        }
    }
}
