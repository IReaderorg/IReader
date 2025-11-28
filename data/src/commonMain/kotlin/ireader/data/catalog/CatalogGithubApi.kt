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
        
        // Trim and normalize the input, removing invisible Unicode characters
        // LRM (U+200E), RLM (U+200F), and other control characters that may be present
        val normalized = languageName
            .trim()
            .replace("\u200E", "") // Left-to-Right Mark
            .replace("\u200F", "") // Right-to-Left Mark
            .replace("\u200B", "") // Zero Width Space
            .replace("\u200C", "") // Zero Width Non-Joiner
            .replace("\u200D", "") // Zero Width Joiner
            .replace("\uFEFF", "") // Byte Order Mark
            .trim()
        
        if (normalized.isBlank()) return "en"
        
        // If it's already a 2-letter code, return it lowercase
        if (normalized.length == 2 && normalized.all { it.isLetter() || it.isDigit() }) {
            return normalized.lowercase()
        }
        
        // If it's already a 3-letter code (like "ara" for Arabic), convert it
        if (normalized.length == 3 && normalized.all { it.isLetter() }) {
            return convertIso639_2ToIso639_1(normalized.lowercase()) ?: normalized.lowercase()
        }
        
        // Map of language names to ISO 639-1 codes
        // Note: Non-Latin scripts don't have lowercase, so we check both original and lowercase
        val lowercased = normalized.lowercase()
        
        return when {
            // Arabic variants (different spellings with Arabic Yeh ي and Persian Yeh ی)
            normalized == "العربية" || normalized == "العربیة" || normalized == "العربیه" || 
            normalized == "عربي" || normalized == "عربی" || lowercased == "arabic" ||
            normalized.contains("عرب") -> "ar"
            
            // Chinese variants
            normalized == "中文" || normalized == "中文 (简体)" || normalized == "中文 (繁體)" ||
            normalized == "简体中文" || normalized == "繁體中文" || lowercased == "chinese" ||
            normalized == "中文, 汉语, 漢語" || normalized.contains("中文") -> "zh"
            
            // Other languages
            lowercased == "english" -> "en"
            lowercased == "español" || lowercased == "spanish" -> "es"
            lowercased == "français" || lowercased == "french" -> "fr"
            lowercased == "bahasa indonesia" || lowercased == "indonesian" -> "id"
            normalized == "日本語" || lowercased == "japanese" || normalized.contains("日本") -> "ja"
            // Korean variants (including "조선말, 한국어")
            normalized == "한국어" || normalized == "조선말" || normalized == "조선말, 한국어" ||
            normalized.contains("한국") || normalized.contains("조선") || lowercased == "korean" -> "ko"
            lowercased == "português" || lowercased == "portuguese" -> "pt"
            normalized == "русский" || lowercased == "russian" || normalized.contains("Русск") -> "ru"
            normalized == "ไทย" || lowercased == "thai" -> "th"
            lowercased == "türkçe" || lowercased == "turkish" -> "tr"
            lowercased == "tiếng việt" || lowercased == "vietnamese" -> "vi"
            lowercased == "deutsch" || lowercased == "german" -> "de"
            lowercased == "italiano" || lowercased == "italian" -> "it"
            lowercased == "polski" || lowercased == "polish" -> "pl"
            normalized == "українська" || lowercased == "ukrainian" || normalized.contains("Україн") -> "uk"
            lowercased == "filipino" || lowercased == "tagalog" -> "tl"
            lowercased == "magyar" || lowercased == "hungarian" -> "hu"
            lowercased == "čeština" || lowercased == "czech" -> "cs"
            lowercased == "română" || lowercased == "romanian" -> "ro"
            lowercased == "nederlands" || lowercased == "dutch" -> "nl"
            lowercased == "svenska" || lowercased == "swedish" -> "sv"
            lowercased == "norsk" || lowercased == "norwegian" -> "no"
            lowercased == "dansk" || lowercased == "danish" -> "da"
            lowercased == "suomi" || lowercased == "finnish" -> "fi"
            normalized == "ελληνικά" || lowercased == "greek" -> "el"
            normalized == "עברית" || lowercased == "hebrew" -> "he"
            normalized == "हिन्दी" || lowercased == "hindi" -> "hi"
            normalized == "বাংলা" || lowercased == "bengali" -> "bn"
            normalized == "မြန်မာဘာသာ" || lowercased == "burmese" -> "my"
            lowercased == "català" || lowercased == "catalan" -> "ca"
            lowercased == "galego" || lowercased == "galician" -> "gl"
            lowercased == "euskara" || lowercased == "basque" -> "eu"
            lowercased == "lietuvių" || lowercased == "lithuanian" -> "lt"
            lowercased == "latviešu" || lowercased == "latvian" -> "lv"
            lowercased == "eesti" || lowercased == "estonian" -> "et"
            lowercased == "slovenčina" || lowercased == "slovak" -> "sk"
            lowercased == "slovenščina" || lowercased == "slovene" -> "sl"
            lowercased == "hrvatski" || lowercased == "croatian" -> "hr"
            lowercased == "srpski" || lowercased == "serbian" -> "sr"
            normalized == "български" || lowercased == "bulgarian" -> "bg"
            normalized == "македонски" || lowercased == "macedonian" -> "mk"
            // Persian/Farsi variants
            normalized == "فارسی" || normalized == "فارسي" || lowercased == "persian" || lowercased == "farsi" -> "fa"
            // Urdu
            normalized == "اردو" || lowercased == "urdu" -> "ur"
            // Multi-language sources - treat as "all" or default to English
            lowercased == "multi" || lowercased == "multilingual" -> "multi"
            else -> {
                // If we can't map it, log a warning and return the original (might be a valid code)
                ireader.core.log.Log.warn("Unknown language name: '$normalized', returning as-is")
                // Return the normalized value if it looks like a language code, otherwise default to "en"
                if (normalized.length <= 5 && normalized.all { it.isLetter() || it == '-' }) {
                    normalized.lowercase()
                } else {
                    "en"
                }
            }
        }
    }
    
    /**
     * Converts ISO 639-2 (3-letter) language codes to ISO 639-1 (2-letter) codes.
     */
    private fun convertIso639_2ToIso639_1(iso639_2: String): String? {
        return when (iso639_2) {
            "ara" -> "ar"
            "chi", "zho" -> "zh"
            "eng" -> "en"
            "spa" -> "es"
            "fra", "fre" -> "fr"
            "ind" -> "id"
            "jpn" -> "ja"
            "kor" -> "ko"
            "por" -> "pt"
            "rus" -> "ru"
            "tha" -> "th"
            "tur" -> "tr"
            "vie" -> "vi"
            "deu", "ger" -> "de"
            "ita" -> "it"
            "pol" -> "pl"
            "ukr" -> "uk"
            "fil", "tgl" -> "tl"
            "hun" -> "hu"
            "ces", "cze" -> "cs"
            "ron", "rum" -> "ro"
            "nld", "dut" -> "nl"
            "swe" -> "sv"
            "nor" -> "no"
            "dan" -> "da"
            "fin" -> "fi"
            "ell", "gre" -> "el"
            "heb" -> "he"
            "hin" -> "hi"
            "ben" -> "bn"
            "mya", "bur" -> "my"
            "cat" -> "ca"
            "glg" -> "gl"
            "eus", "baq" -> "eu"
            "lit" -> "lt"
            "lav" -> "lv"
            "est" -> "et"
            "slk", "slo" -> "sk"
            "slv" -> "sl"
            "hrv" -> "hr"
            "srp" -> "sr"
            "bul" -> "bg"
            "mkd", "mac" -> "mk"
            "fas", "per" -> "fa"
            "urd" -> "ur"
            else -> null
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
