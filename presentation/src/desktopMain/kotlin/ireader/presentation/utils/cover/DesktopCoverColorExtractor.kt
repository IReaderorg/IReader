package ireader.presentation.utils.cover

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
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class DesktopCoverColorExtractor(
    private val catalogStore: CatalogStore,
    private val httpClients: HttpClients
) : CoverColorExtractor {
    override suspend fun extractDominantColor(coverUrl: String, sourceId: Long?): DomainColor? = withContext(Dispatchers.IO) {
        try {
            val catalog = sourceId?.let { catalogStore.get(it) }
            val baseUrl = (catalog?.source as? HttpSource)?.baseUrl
            val absoluteUrl = CoverColorExtractor.resolveCoverUrl(coverUrl, baseUrl)

            val sourceHeaders: Map<String, List<String>>? = sourceId?.let {
                val httpSource = catalog?.source as? HttpSource
                val builder = runCatching { httpSource?.getCoverRequest(absoluteUrl)?.second }
                    .getOrNull() ?: return@let null
                builder.build().headers.entries().associate { it.key to it.value }
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
            Log.error { "CoverColorExtractor: failed to extract color from $coverUrl: ${e.message}" }
            null
        }
    }

    override suspend fun extractDominantColorFromBitmap(byteArray: ByteArray): DomainColor? = withContext(Dispatchers.IO) {
        try {
            val image = ImageIO.read(ByteArrayInputStream(byteArray)) ?: return@withContext null

            // Downscale to a tiny sample; dominant hue is stable at this size
            val scaled = BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB)
            val g2d = scaled.createGraphics()
            try {
                g2d.drawImage(image, 0, 0, 50, 50, null)
            } finally {
                g2d.dispose()
            }

            // Quantize to 16-level channels; count with an IntArray instead of a Map
            val counts = IntArray(16 * 16 * 16)
            for (pixel in scaled.getRGB(0, 0, 50, 50, null, 0, 50)) {
                val r = ((pixel shr 16) and 0xF0) shr 4
                val g = ((pixel shr 8) and 0xF0) shr 4
                val b = (pixel and 0xF0) shr 4
                counts[(r shl 8) or (g shl 4) or b]++
            }

            var bestIdx = -1
            var bestCount = 0
            for (i in counts.indices) {
                if (counts[i] > bestCount) {
                    bestCount = counts[i]
                    bestIdx = i
                }
            }
            if (bestIdx < 0) return@withContext null

            val r = ((bestIdx shr 8) and 0xF) * 17
            val g = ((bestIdx shr 4) and 0xF) * 17
            val b = (bestIdx and 0xF) * 17
            DomainColor(r / 255f, g / 255f, b / 255f)
        } catch (e: Exception) {
            null
        }
    }
}
