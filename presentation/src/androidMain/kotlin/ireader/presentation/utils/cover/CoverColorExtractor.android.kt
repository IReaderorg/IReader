package ireader.presentation.utils.cover

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpStatusCode
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.core.source.HttpSource
import ireader.domain.catalogs.CatalogStore
import ireader.domain.models.common.DomainColor
import ireader.domain.utils.cover.CoverColorExtractor
import ireader.presentation.imageloader.convertToOkHttpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class AndroidCoverColorExtractor(
    private val catalogStore: CatalogStore,
    private val httpClients: HttpClients
) : CoverColorExtractor {
    override suspend fun extractDominantColor(coverUrl: String, sourceId: Long?): DomainColor? = withContext(Dispatchers.IO) {
        try {
            val absoluteUrl = if (coverUrl.startsWith("http")) {
                coverUrl
            } else {
                val catalog = sourceId?.let { catalogStore.get(it) }
                val baseUrl = (catalog?.source as? HttpSource)?.baseUrl?.trimEnd('/') ?: ""
                if (baseUrl.isNotBlank()) "$baseUrl/$coverUrl" else coverUrl
            }
            
            val sourceHeaders = sourceId?.let { 
                val catalog = catalogStore.get(it)
                val httpSource = catalog?.source as? HttpSource
                httpSource?.getCoverRequest(absoluteUrl)?.second?.build()?.convertToOkHttpRequest()?.headers
            }
            
            val response = httpClients.default.get(absoluteUrl) {
                sourceHeaders?.let { headers ->
                    this.headers {
                        headers.forEach { (name, value) ->
                            append(name, value)
                        }
                    }
                }
            }
            
            if (response.status != HttpStatusCode.OK) {
                Log.warn { "CoverColorExtractor: HTTP ${response.status.value} for $absoluteUrl" }
                return@withContext null
            }
            
            val bytes = response.body<ByteArray>()
            extractDominantColorFromBitmap(bytes)
        } catch (e: Exception) {
            Log.error { "CoverColorExtractor: failed to extract color from $coverUrl: ${e.message}" }
            null
        }
    }

    override suspend fun extractDominantColorFromBitmap(byteArray: ByteArray): DomainColor? = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size) ?: return@withContext null
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
            val width = scaledBitmap.width
            val height = scaledBitmap.height
            val pixels = IntArray(width * height)
            scaledBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val colorMap = mutableMapOf<Int, Int>()
            for (pixel in pixels) {
                val alpha = Color.alpha(pixel)
                if (alpha < 128) continue
                val r = (Color.red(pixel) / 16) * 16
                val g = (Color.green(pixel) / 16) * 16
                val b = (Color.blue(pixel) / 16) * 16
                val quantized = Color.rgb(
                    r.coerceIn(0, 255),
                    g.coerceIn(0, 255),
                    b.coerceIn(0, 255)
                )
                colorMap[quantized] = (colorMap[quantized] ?: 0) + 1
            }

            val dominant = colorMap.maxByOrNull { it.value }?.key ?: return@withContext null
            DomainColor.fromArgb(dominant)
        } catch (e: Exception) {
            null
        }
    }
}
