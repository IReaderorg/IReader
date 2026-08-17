package ireader.domain.utils.cover

import ireader.domain.models.common.DomainColor

interface CoverColorExtractor {
    suspend fun extractDominantColor(coverUrl: String, sourceId: Long? = null): DomainColor?
    suspend fun extractDominantColorFromBitmap(byteArray: ByteArray): DomainColor?
}
