package ireader.desktop.update

/**
 * Represents AppImage update information embedded in the AppImage file
 * 
 * This class handles parsing and generation of update information strings
 * according to the AppImage specification.
 * 
 * @property transport The update transport mechanism (zsync or gh-releases-zsync)
 * @property url Direct URL for zsync transport
 * @property owner GitHub owner for gh-releases-zsync transport
 * @property repo GitHub repository for gh-releases-zsync transport
 * @property releaseTag GitHub release tag (e.g., "latest", "v1.0.0")
 * @property filename Filename pattern for gh-releases-zsync transport
 * 
 * @see <a href="https://github.com/AppImage/AppImageSpec/blob/master/draft.md#update-information">AppImage Update Information Spec</a>
 */
data class AppImageUpdateInfo(
    val transport: UpdateTransport,
    val url: String? = null,
    val owner: String? = null,
    val repo: String? = null,
    val releaseTag: String? = null,
    val filename: String? = null
) {
    /**
     * Validates that the update info has all required fields for its transport type
     */
    fun isValid(): Boolean {
        return when (transport) {
            UpdateTransport.ZSYNC -> {
                !url.isNullOrBlank()
            }
            UpdateTransport.GH_RELEASES_ZSYNC -> {
                !owner.isNullOrBlank() &&
                !repo.isNullOrBlank() &&
                !releaseTag.isNullOrBlank() &&
                !filename.isNullOrBlank()
            }
        }
    }
    
    /**
     * Converts this update info to the AppImage update information string format
     * 
     * @return Formatted update information string
     */
    fun toUpdateInfoString(): String {
        return when (transport) {
            UpdateTransport.ZSYNC -> {
                "${transport.toTransportString()}|$url"
            }
            UpdateTransport.GH_RELEASES_ZSYNC -> {
                "${transport.toTransportString()}|$owner|$repo|$releaseTag|$filename"
            }
        }
    }
    
    companion object {
        /**
         * Parses an AppImage update information string
         * 
         * @param updateInfoString The update information string to parse
         * @return Parsed AppImageUpdateInfo or null if invalid
         */
        fun parse(updateInfoString: String): AppImageUpdateInfo? {
            if (updateInfoString.isBlank()) {
                return null
            }
            
            val parts = updateInfoString.split("|")
            if (parts.isEmpty()) {
                return null
            }
            
            val transport = UpdateTransport.fromString(parts[0]) ?: return null
            
            return when (transport) {
                UpdateTransport.ZSYNC -> {
                    if (parts.size < 2) {
                        null
                    } else {
                        AppImageUpdateInfo(
                            transport = transport,
                            url = parts[1]
                        )
                    }
                }
                UpdateTransport.GH_RELEASES_ZSYNC -> {
                    if (parts.size < 5) {
                        null
                    } else {
                        AppImageUpdateInfo(
                            transport = transport,
                            owner = parts[1],
                            repo = parts[2],
                            releaseTag = parts[3],
                            filename = parts[4]
                        )
                    }
                }
            }
        }
        
        /**
         * Creates update info for GitHub releases with zsync
         * 
         * @param owner GitHub repository owner
         * @param repo GitHub repository name
         * @param releaseTag Release tag (e.g., "latest", "v1.0.0")
         * @param filename Filename (e.g., "IReader-x86_64.AppImage.zsync")
         * @return AppImageUpdateInfo instance
         */
        fun forGitHubReleases(
            owner: String,
            repo: String,
            releaseTag: String = "latest",
            filename: String
        ): AppImageUpdateInfo {
            return AppImageUpdateInfo(
                transport = UpdateTransport.GH_RELEASES_ZSYNC,
                owner = owner,
                repo = repo,
                releaseTag = releaseTag,
                filename = filename
            )
        }
        
        /**
         * Creates update info for direct zsync URL
         * 
         * @param url Direct URL to the zsync file
         * @return AppImageUpdateInfo instance
         */
        fun forZsyncUrl(url: String): AppImageUpdateInfo {
            return AppImageUpdateInfo(
                transport = UpdateTransport.ZSYNC,
                url = url
            )
        }
    }
}
