package ireader.data.catalog

import io.ktor.client.request.*
import io.ktor.client.statement.*
import ireader.core.http.HttpClients
import ireader.domain.catalogs.service.CatalogRemoteApi
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.services.extensions_insstaller_service.GetDefaultRepo
import ireader.domain.data.repository.CatalogSourceRepository
import kotlinx.coroutines.flow.first
import ireader.domain.utils.CatalogNotFoundException
import ireader.i18n.REPO_URL
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json



class CatalogGithubApi(
    private val httpClient: HttpClients,
    private val getDefaultRepo: GetDefaultRepo,
    private val catalogSourceRepository: CatalogSourceRepository
) : CatalogRemoteApi {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun fetchCatalogs(): List<CatalogRemote> {
        val allCatalogs = mutableListOf<CatalogRemote>()
        val errors = mutableListOf<String>()
        
        // Get all enabled repositories
        val repositories = catalogSourceRepository.subscribe().first()
        val enabledRepositories = repositories.filter { it.isEnable }
        
        if (enabledRepositories.isEmpty()) {
            // No repositories configured - return empty list
            return emptyList()
        }
        
        // Fetch from all enabled repositories
        for (repo in enabledRepositories) {
            try {
                val catalogs = fetchFromRepository(repo)
                allCatalogs.addAll(catalogs)
            } catch (e: Exception) {
                errors.add("Repository ${repo.name} (${repo.key}): ${e.message}")
                // Continue with other repositories instead of failing completely
            }
        }
        
        // If we got some catalogs, return them even if some repositories failed
        if (allCatalogs.isNotEmpty()) {
            return allCatalogs
        }
        
        // If all repositories failed, throw an exception with all error messages
        throw CatalogNotFoundException("Failed to fetch catalogs from all repositories:\n${errors.joinToString("\n")}")
    }
    
    private suspend fun fetchFromRepository(repo: ireader.domain.models.entities.ExtensionSource): List<CatalogRemote> {
        try {
            val response: String = httpClient.default
                .get(repo.key)
                .bodyAsText()

            // Check if response is an error (like 404)
            if (response.startsWith("404") || response.contains("Not Found") || 
                response.contains("<!DOCTYPE html>") || response.contains("<html")) {
                throw CatalogNotFoundException("Repository not found or returned error: ${repo.key}")
            }

            // Validate that response looks like JSON
            val trimmedResponse = response.trim()
            if (!trimmedResponse.startsWith("[") && !trimmedResponse.startsWith("{")) {
                throw CatalogNotFoundException("Invalid JSON response from repository: ${repo.key}")
            }

            return when {
                repo.isLNReaderRepository() || repo.key.contains("lnreader-plugins") || repo.key.contains("plugins.min.json") -> {
                    parseLNReaderFormat(response, repo)
                }
                else -> {
                    parseIReaderFormat(response, repo)
                }
            }
        } catch (e: kotlinx.serialization.SerializationException) {
            // Handle JSON parsing errors specifically
            throw CatalogNotFoundException("Failed to parse JSON from repository ${repo.key}. This might be an ${repo.repositoryType} repository being parsed with the wrong format. Error: ${e.message}")
        } catch (e: Exception) {
            // Handle other network or parsing errors
            throw CatalogNotFoundException("Failed to fetch catalogs from repository ${repo.key}: ${e.message}")
        }
    }

    private fun parseIReaderFormat(response: String, repo: ireader.domain.models.entities.ExtensionSource): List<CatalogRemote> {
        val catalogs = json.decodeFromString<List<CatalogRemoteApiModel>>(response)
        if (catalogs.isEmpty()) {
            throw CatalogNotFoundException("No catalogs found in repository")
        }
        
        val repoUrl = repo.key.substringBefore("index.min.json","").takeIf { it.isNotBlank() } ?: REPO_URL
        return catalogs.map { catalog ->
            val iconUrl = "$repoUrl/icon/${catalog.apk.replace(".apk", ".png")}"
            val appUrl = "$repoUrl/apk/${catalog.apk}"
            val jarUrl = "$repoUrl/jar/${catalog.apk.replace(".apk", ".jar")}"
            CatalogRemote(
                name = catalog.name,
                description = catalog.description,
                sourceId = catalog.id,
                pkgName = catalog.pkg,
                versionName = catalog.version,
                versionCode = catalog.code,
                lang = catalog.lang,
                pkgUrl = appUrl,
                iconUrl = iconUrl,
                nsfw = catalog.nsfw,
                source = CatalogRemote.DEFAULT_ID,
                jarUrl = jarUrl,
                repositoryId = repo.id,
                repositoryType = "IREADER" // Mark as IReader repository
            )
        }
    }

    private fun parseLNReaderFormat(response: String, repo: ireader.domain.models.entities.ExtensionSource): List<CatalogRemote> {
        val lnReaderCatalogs = json.decodeFromString<List<LNReaderPluginModel>>(response)
        if (lnReaderCatalogs.isEmpty()) {
            throw CatalogNotFoundException("No LNReader catalogs found in repository")
        }
        
        return lnReaderCatalogs.map { plugin ->
            // Generate a unique numeric ID for LNReader sources
            // Use a high offset (1_000_000_000_000L) to avoid conflicts with IReader sources
            // Combine with hashCode for consistency across sessions
            val baseHash = plugin.id.hashCode().toLong()
            val numericId = 1_000_000_000_000L + (if (baseHash < 0) -baseHash else baseHash)
            
            // Convert language name to language code
            val languageCode = convertLanguageNameToCode(plugin.lang)
            
            // Fix icon URL to ensure it's a direct image URL
            val iconUrl = fixLNReaderIconUrl(plugin.iconUrl, plugin.id, languageCode, repo)
            
            CatalogRemote(
                name = plugin.name,
                description = plugin.description ?: "LNReader Plugin",
                sourceId = numericId,
                pkgName = plugin.id,
                versionName = plugin.version,
                versionCode = plugin.version.replace(".", "").toIntOrNull() ?: 1,
                lang = languageCode,
                pkgUrl = plugin.url,
                iconUrl = iconUrl,
                nsfw = false, // LNReader plugins don't typically have NSFW flag
                source = CatalogRemote.DEFAULT_ID,
                jarUrl = plugin.url, // For LNReader, the URL is the plugin file itself
                repositoryId = repo.id,
                repositoryType = "LNREADER" // Mark as LNReader repository
            )
        }
    }
    
    /**
     * Converts LNReader language names to ISO 639-1 language codes.
     * LNReader uses full language names (e.g., "English", "العربية", "日本語") 
     * while IReader uses language codes (e.g., "en", "ar", "ja").
     */
    private fun convertLanguageNameToCode(languageName: String?): String {
        if (languageName.isNullOrBlank()) return "en"
        
        // Trim and normalize the input
        val normalized = languageName.trim()
        
        // If it's already a 2-letter code, return it lowercase
        if (normalized.length == 2 && normalized.all { it.isLetter() || it.isDigit() }) {
            return normalized.lowercase()
        }
        
        // Map of language names to ISO 639-1 codes (case-insensitive)
        return when (normalized.lowercase()) {
            "english" -> "en"
            "العربية", "arabic" -> "ar"
            "中文", "chinese", "中文 (简体)", "中文 (繁體)" -> "zh"
            "español", "spanish" -> "es"
            "français", "french" -> "fr"
            "bahasa indonesia", "indonesian" -> "id"
            "日本語", "japanese" -> "ja"
            "한국어", "korean" -> "ko"
            "português", "portuguese" -> "pt"
            "русский", "russian" -> "ru"
            "ไทย", "thai" -> "th"
            "türkçe", "turkish" -> "tr"
            "tiếng việt", "vietnamese" -> "vi"
            "deutsch", "german" -> "de"
            "italiano", "italian" -> "it"
            "polski", "polish" -> "pl"
            "українська", "ukrainian" -> "uk"
            "filipino", "tagalog" -> "tl"
            "magyar", "hungarian" -> "hu"
            "čeština", "czech" -> "cs"
            "română", "romanian" -> "ro"
            "nederlands", "dutch" -> "nl"
            "svenska", "swedish" -> "sv"
            "norsk", "norwegian" -> "no"
            "dansk", "danish" -> "da"
            "suomi", "finnish" -> "fi"
            "ελληνικά", "greek" -> "el"
            "עברית", "hebrew" -> "he"
            "हिन्दी", "hindi" -> "hi"
            "বাংলা", "bengali" -> "bn"
            "မြန်မာဘာသာ", "burmese" -> "my"
            "català", "catalan" -> "ca"
            "galego", "galician" -> "gl"
            "euskara", "basque" -> "eu"
            "lietuvių", "lithuanian" -> "lt"
            "latviešu", "latvian" -> "lv"
            "eesti", "estonian" -> "et"
            "slovenčina", "slovak" -> "sk"
            "slovenščina", "slovene" -> "sl"
            "hrvatski", "croatian" -> "hr"
            "srpski", "serbian" -> "sr"
            "български", "bulgarian" -> "bg"
            "македонски", "macedonian" -> "mk"
            else -> {
                // If we can't map it, log a warning and default to English
                ireader.core.log.Log.warn("Unknown language name: '$normalized', defaulting to 'en'")
                "en"
            }
        }
    }
    
    /**
     * Fixes LNReader icon URLs to ensure they point to raw image files.
     * Converts GitHub tree URLs to raw URLs and constructs fallback URLs if needed.
     */
    private fun fixLNReaderIconUrl(
        iconUrl: String?,
        pluginId: String,
        lang: String?,
        repo: ireader.domain.models.entities.ExtensionSource
    ): String {
        // If iconUrl is provided and valid, fix it if needed
        if (!iconUrl.isNullOrBlank()) {
            // Convert GitHub tree URL to raw URL
            if (iconUrl.contains("github.com") && iconUrl.contains("/tree/")) {
                return iconUrl
                    .replace("github.com", "raw.githubusercontent.com")
                    .replace("/tree/", "/")
            }
            // If it's already a raw URL or external URL, use it as-is
            if (iconUrl.startsWith("http://") || iconUrl.startsWith("https://")) {
                return iconUrl
            }
        }
        
        // Fallback: construct icon URL from repository structure
        // LNReader plugins typically follow: src/{lang}/{pluginId}/icon.png
        val repoBaseUrl = repo.source.removeSuffix(".git")
        val language = lang ?: "en"
        return "$repoBaseUrl/raw/main/src/$language/$pluginId/icon.png"
    }


    @Serializable
    private data class CatalogRemoteApiModel(
        @SerialName("name") val name: String,
        @SerialName("pkg") val pkg: String,
        @SerialName("version") val version: String,
        @SerialName("code") val code: Int,
        @SerialName("lang") val lang: String,
        @SerialName("apk") val apk: String,
        @SerialName("id") val id: Long,
        @SerialName("description") val description: String,
        @SerialName("nsfw") val nsfw: Boolean,
    )

    @Serializable
    private data class LNReaderPluginModel(
        @SerialName("id") val id: String,
        @SerialName("name") val name: String,
        @SerialName("version") val version: String,
        @SerialName("url") val url: String,
        @SerialName("description") val description: String? = null,
        @SerialName("lang") val lang: String? = null,
        @SerialName("iconUrl") val iconUrl: String? = null
    )
}
