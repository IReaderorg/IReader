package ireader.desktop.update

/**
 * Supported AppImage update transport mechanisms
 * 
 * @see <a href="https://github.com/AppImage/AppImageSpec/blob/master/draft.md#update-information">AppImage Update Information Spec</a>
 */
enum class UpdateTransport {
    /**
     * Direct zsync URL
     * Format: zsync|<url>
     * Example: zsync|https://example.com/app.AppImage.zsync
     */
    ZSYNC,
    
    /**
     * GitHub Releases with zsync
     * Format: gh-releases-zsync|<owner>|<repo>|<tag>|<filename>
     * Example: gh-releases-zsync|IReader-org|IReader|latest|IReader-*-x86_64.AppImage.zsync
     */
    GH_RELEASES_ZSYNC;
    
    companion object {
        fun fromString(value: String): UpdateTransport? {
            return when (value.lowercase()) {
                "zsync" -> ZSYNC
                "gh-releases-zsync" -> GH_RELEASES_ZSYNC
                else -> null
            }
        }
    }
    
    fun toTransportString(): String {
        return when (this) {
            ZSYNC -> "zsync"
            GH_RELEASES_ZSYNC -> "gh-releases-zsync"
        }
    }
}
