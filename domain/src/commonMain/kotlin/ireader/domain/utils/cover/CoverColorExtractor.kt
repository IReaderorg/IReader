package ireader.domain.utils.cover

import ireader.domain.models.common.DomainColor

interface CoverColorExtractor {
    suspend fun extractDominantColor(coverUrl: String, sourceId: Long? = null): DomainColor?
    suspend fun extractDominantColorFromBitmap(byteArray: ByteArray): DomainColor?

    companion object {
        /**
         * Resolves relative/protocol-relative cover URLs against a source base URL.
         * Mirrors HttpSource.getAbsoluteUrl; kept here so every caller agrees.
         */
        fun resolveCoverUrl(coverUrl: String, baseUrl: String?): String = when {
            coverUrl.startsWith("http://") || coverUrl.startsWith("https://") -> coverUrl
            coverUrl.startsWith("//") -> "https:$coverUrl"
            baseUrl.isNullOrBlank() -> coverUrl
            coverUrl.startsWith("/") -> "${baseUrl.trimEnd('/')}$coverUrl"
            else -> "${baseUrl.trimEnd('/')}/$coverUrl"
        }
    }
}
