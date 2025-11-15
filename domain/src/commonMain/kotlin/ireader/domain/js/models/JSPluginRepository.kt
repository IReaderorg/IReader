package ireader.domain.js.models

/**
 * Represents a repository containing JavaScript plugins.
 * 
 * @property name The display name of the repository
 * @property url The URL to the repository's plugin list (JSON format)
 * @property enabled Whether this repository is currently enabled
 */
data class JSPluginRepository(
    val name: String,
    val url: String,
    val enabled: Boolean = true
) {
    companion object {
        /**
         * Returns the default LNReader official plugin repository.
         */
        fun default(): JSPluginRepository {
            return JSPluginRepository(
                name = "LNReader Official",
                url = "https://raw.githubusercontent.com/LNReader/lnreader-plugins/main/plugins.json",
                enabled = true
            )
        }
    }
}
