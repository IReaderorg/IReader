package ireader.domain.data.repository

/**
 * Fetches the public Discord Guild Widget (no bot/token needed; the server must have
 * "Enable Server Widget" turned on). Used to show a live "N online" count in Discover.
 */
interface DiscordWidgetRepository {
    /** Online presence count, or null on failure / widget disabled. */
    suspend fun getOnlineCount(): Int?
}
