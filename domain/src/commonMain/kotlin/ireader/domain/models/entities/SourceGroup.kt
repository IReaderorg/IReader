package ireader.domain.models.entities

/**
 * Represents the group/type of a source.
 * Sources are differentiated by their prefix convention:
 * - LNREADER: lnreader-X sources (e.g., lnreader-mangadex)
 * - IREADER: ireader-source extensions (e.g., ireader-mangadex)
 */
enum class SourceGroup {
    LNREADER,
    IREADER;

    companion object {
        /**
         * Resolves the source group from a source name.
         * Returns null if the source name doesn't match any known prefix.
         */
        fun fromSourceName(sourceName: String): SourceGroup? {
            return when {
                sourceName.startsWith("lnreader-", ignoreCase = true) -> LNREADER
                sourceName.startsWith("ireader-", ignoreCase = true) -> IREADER
                else -> null
            }
        }

        /**
         * Returns the prefix for this source group.
         */
        val SourceGroup.prefix: String
            get() = when (this) {
                LNREADER -> "lnreader"
                IREADER -> "ireader"
            }
    }
}
