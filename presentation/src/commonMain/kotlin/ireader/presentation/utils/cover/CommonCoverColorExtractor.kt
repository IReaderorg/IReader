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
            var y = 0
            var write = 0
            val argb = IntArray((w / stride + 1) * (h / stride + 1))
            while (y < h) {
                var x = 0
                while (x < w) {
                    val c = pixels[x, y]
                    if (c.alpha >= 0.5f) {
                        argb[write++] =
                            (((c.red * 255f).toInt()) shl 16) or
                            (((c.green * 255f).toInt()) shl 8) or
                            ((c.blue * 255f).toInt())
                    }
                    x += stride
                }
                y += stride
            }

            selectSeedColor(argb.copyOf(write))
        } catch (e: Exception) {
            Log.warn { "CoverColorExtractor: decode failed: ${e.message}" }
            null
        }
    }

    /**
     * Picks a vivid seed from sampled RGB pixels. Plain "most frequent bucket"
     * usually lands on white/black text or borders, washing the theme out to
     * gray — so score buckets by population × saturation², skipping near-gray
     * and extreme-brightness buckets. Falls back to the plain winner when the
     * cover is genuinely grayscale.
     */
    companion object {
        fun selectSeedColor(argb: IntArray): DomainColor? {
        if (argb.isEmpty()) return null
        val counts = IntArray(16 * 16 * 16)
        for (p in argb) {
            // Pack each channel's high nibble: [r@8][g@4][b@0], matching the *17 decode below
            val r = (p shr 20) and 0xF
            val g = (p shr 12) and 0xF
            val b = (p shr 4) and 0xF
            counts[(r shl 8) or (g shl 4) or b]++
        }

        var fallbackIdx = -1
        var fallbackCount = 0
        var bestIdx = -1
        var bestScore = 0f
        for (i in counts.indices) {
            val count = counts[i]
            if (count > fallbackCount) {
                fallbackCount = count
                fallbackIdx = i
            }
            if (count == 0) continue
            val r = (((i shr 8) and 0xF) * 17) / 255f
            val g = (((i shr 4) and 0xF) * 17) / 255f
            val b = ((i and 0xF) * 17) / 255f
            val max = maxOf(r, g, b)
            val min = minOf(r, g, b)
            val sat = if (max == 0f) 0f else (max - min) / max
            val lum = 0.2126f * r + 0.7152f * g + 0.0722f * b
            if (sat < 0.18f || lum < 0.12f || lum > 0.92f) continue
            val score = count * sat * sat
            if (score > bestScore) {
                bestScore = score
                bestIdx = i
            }
        }
        val idx = if (bestIdx >= 0) bestIdx else fallbackIdx
        if (idx < 0) return null

            return DomainColor(
                ((((idx shr 8) and 0xF) * 17) / 255f),
                ((((idx shr 4) and 0xF) * 17) / 255f),
                (((idx and 0xF) * 17) / 255f)
            )
        }
    }
}
