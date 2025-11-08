package ireader.presentation.ui.settings.appearance

import ireader.domain.models.theme.CustomTheme
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Utility object for importing and exporting themes
 */
object ThemeImportExport {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Export a theme to JSON string
     */
    fun exportTheme(theme: CustomTheme): String {
        return json.encodeToString(theme)
    }
    
    /**
     * Export multiple themes to JSON string
     */
    fun exportThemes(themes: List<CustomTheme>): String {
        return json.encodeToString(themes)
    }
    
    /**
     * Import a theme from JSON string
     */
    fun importTheme(jsonString: String): Result<CustomTheme> {
        return try {
            val theme = json.decodeFromString<CustomTheme>(jsonString)
            Result.success(theme)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Import multiple themes from JSON string
     */
    fun importThemes(jsonString: String): Result<List<CustomTheme>> {
        return try {
            val themes = json.decodeFromString<List<CustomTheme>>(jsonString)
            Result.success(themes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate if a JSON string is a valid theme
     */
    fun isValidThemeJson(jsonString: String): Boolean {
        return try {
            json.decodeFromString<CustomTheme>(jsonString)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Validate if a JSON string contains valid themes
     */
    fun isValidThemesJson(jsonString: String): Boolean {
        return try {
            json.decodeFromString<List<CustomTheme>>(jsonString)
            true
        } catch (e: Exception) {
            false
        }
    }
}
