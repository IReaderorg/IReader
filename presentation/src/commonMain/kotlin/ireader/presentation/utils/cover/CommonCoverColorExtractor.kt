package ireader.presentation.utils.cover

import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.toPixelMap
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

/**
 * Single multiplatform extractor: fetches the cover with the source's own headers,
 * decodes via the Skia-backed [decodeToImageBitmap] (handles WebP/AVIF on every
 * platform, unlike platform codecs), then finds the dominant color by counting a
 * sparse sample grid against a 16-level RGB histogram.
 */
class CommonCoverColorExtractor(
    private val catalogStore: CatalogStore,
    private val httpClients: HttpClients
) : CoverColorExtractor {

    override suspend fun extractDominantColor(coverUrl: String, sourceId: Long?): DomainColor? = withContext(Dispatchers.IO) {
        try {
            val catalog = sourceId?.let { catalogStore.get(it) }
            val baseUrl = (catalog?.source as? HttpSource)?.baseUrl
            val absoluteUrl = CoverColorExtractor.resolveCoverUrl(coverUrl, baseUrl)

            // Reuse the source's cover request headers (Referer/User-Agent gates)
            val sourceHeaders: Map<String, List<String>>? = sourceId?.let {
                val httpSource = catalog?.source as? HttpSource
                runCatching {
                    val builder = httpSource?.getCoverRequest(absoluteUrl)?.second
                        ?: return@let null
                    builder.build().headers.entries().associate { entry -> entry.key to entry.value }
                }.getOrNull()
            }

            val response = httpClients.default.get(absoluteUrl) {
                sourceHeaders?.forEach { (name, values) ->
                    values.forEach { value -> headers.append(name, value) }
                }
            }
            if (response.status != HttpStatusCode.OK) {
                Log.warn { "CoverColorExtractor: HTTP ${response.status.value} for $absoluteUrl" }
                return@withContext null
            }
            extractDominantColorFromBitmap(response.body<ByteArray>())
        } catch (e: Exception) {
            Log.error { "CoverColorExtractor: failed for $coverUrl: ${e.message}" }
            null
        }
    }

    override suspend fun extractDominantColorFromBitmap(byteArray: ByteArray): DomainColor? = withContext(Dispatchers.IO) {
        try {
            val bitmap = byteArray.decodeToImageBitmap()
            val pixels = bitmap.toPixelMap()
            val w = bitmap.width
            val h = bitmap.height

            // Sample ~2500 grid points regardless of resolution
            val stride = maxOf(1, sqrt(w.toFloat() * h / 2500f).toInt())
            val counts = IntArray(16 * 16 * 16)
            var y = 0
            while (y < h) {
                var x = 0
                while (x < w) {
                    val c = pixels[x, y]
                    if (c.alpha >= 0.5f) {
                        val r = (c.red * 255f).toInt() shr 4
                        val g = (c.green * 255f).toInt() shr 4
                        val b = (c.blue * 255f).toInt() shr 4
                        counts[(r shl 8) or (g shl 4) or b]++
                    }
                    x += stride
                }
                y += stride
            }

            var bestIdx = -1
            var bestCount = 0
            for (i in counts.indices) {
                if (counts[i] > bestCount) {
                    bestCount = counts[i]
                    bestIdx = i
                }
            }
            if (bestIdx < 0 || bestCount == 0) return@withContext null

            val r = ((bestIdx shr 8) and 0xF) * 17
            val g = ((bestIdx shr 4) and 0xF) * 17
            val b = (bestIdx and 0xF) * 17
            DomainColor(r / 255f, g / 255f, b / 255f)
        } catch (e: Exception) {
            Log.warn { "CoverColorExtractor: decode failed: ${e.message}" }
            null
        }
    }
}
