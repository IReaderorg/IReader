package ireader.domain.preferences.prefs

import ireader.core.prefs.Preference
import ireader.core.prefs.PreferenceStore

/**
 * Preferences for WiFi Sync feature.
 * 
 * Manages user preferences related to device synchronization,
 * including manual server/client role selection.
 */
class SyncPreferences(private val preferenceStore: PreferenceStore) {
    
    /**
     * Manual server/client role selection.
     * 
     * Returns a string-based preference that represents:
     * - "server": This device acts as server
     * - "client": This device acts as client
     * 
     * Default: "server" (desktop typically acts as server)
     */
    fun actAsServer(): Preference<String> {
        return preferenceStore.getString("sync_act_as_server", "server")
    }
    
    /**
     * Helper to check if device should act as server.
     * 
     * @return true for server, false for client
     */
    fun isServer(): Boolean {
        return actAsServer().get() == "server"
    }
}
