package ireader.domain.usecases.fonts

import io.ktor.client.call.*
import io.ktor.client.request.*
import ireader.core.http.HttpClients
import ireader.core.prefs.PreferenceStore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.fleeksoft.ksoup.Ksoup
import ireader.domain.utils.extensions.currentTimeToLong



class FontUseCase(
    private val clients: HttpClients,
    private val preferenceStore: PreferenceStore,
) {
    // In-memory cache for font list to avoid repeated API calls
    private var cachedFonts: List<String>? = null
    private var lastFetchTime: Long = 0
    private val cacheValidityDuration = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
    
    private val fontListPref = preferenceStore.getString("cached_font_list", "")
    private val fontListTimePref = preferenceStore.getLong("cached_font_list_time", 0L)

    suspend fun getRemoteFonts(): List<String> {
        val currentTime = currentTimeToLong()
        
        // Check in-memory cache first
        if (cachedFonts != null && (currentTime - lastFetchTime) < cacheValidityDuration) {
            return cachedFonts!!
        }
        
        // Check persistent cache
        val persistedTime = fontListTimePref.get()
        if ((currentTime - persistedTime) < cacheValidityDuration) {
            val persistedFonts = fontListPref.get()
            if (persistedFonts.isNotEmpty()) {
                try {
                    val fonts = Json.decodeFromString<List<String>>(persistedFonts)
                    if (fonts.isNotEmpty()) {
                        cachedFonts = fonts
                        lastFetchTime = persistedTime
                        return fonts
                    }
                } catch (e: Exception) {
                    ireader.core.log.Log.error("Failed to decode cached fonts", e)
                }
            }
        }

        // Fetch from API
        return try {
            val response: String = clients.default.get("https://fonts.gstatic.com/s/a/directory.xml", block = {}).body()
            val fonts = Ksoup.parse(response).select("family").map { it.attr("name") }
            
            // Update in-memory cache
            cachedFonts = fonts
            lastFetchTime = currentTime
            
            // Update persistent cache
            try {
                fontListPref.set(Json.encodeToString(fonts))
                fontListTimePref.set(currentTime)
            } catch (e: Exception) {
                ireader.core.log.Log.error("Failed to persist font cache", e)
            }
            
            fonts
        } catch (e: Exception) {
            ireader.core.log.Log.error("Failed to fetch fonts from API", e)
            
            // If fetch fails and we have cached fonts (even if expired), return them
            cachedFonts?.let { return it }
            
            // Try to return persisted fonts even if expired
            val persistedFonts = fontListPref.get()
            if (persistedFonts.isNotEmpty()) {
                try {
                    val fonts = Json.decodeFromString<List<String>>(persistedFonts)
                    if (fonts.isNotEmpty()) {
                        cachedFonts = fonts
                        return fonts
                    }
                } catch (decodeError: Exception) {
                    ireader.core.log.Log.error("Failed to decode persisted fonts as fallback", decodeError)
                }
            }
            
            // Otherwise, throw the exception
            throw e
        }
    }
    
    /**
     * Clear the font cache to force a refresh on next fetch
     */
    fun clearCache() {
        cachedFonts = null
        lastFetchTime = 0
        fontListPref.delete()
        fontListTimePref.delete()
    }
}
