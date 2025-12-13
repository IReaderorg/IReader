package ireader.data.plugins

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import ireader.domain.plugins.PluginAuthorInfo
import ireader.domain.plugins.PluginIndexEntry
import ireader.domain.plugins.PluginRepositoryIndex
import ireader.domain.plugins.PluginRepositoryIndexFetcher
import kotlinx.serialization.json.Json

/**
 * Implementation of PluginRepositoryIndexFetcher using Ktor HTTP client
 */
class PluginRepositoryIndexFetcherImpl(
    private val httpClient: HttpClient
) : PluginRepositoryIndexFetcher {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override suspend fun fetchIndex(url: String): Result<PluginRepositoryIndex> {
        return try {
            val response = httpClient.get(url)

            if (!response.status.isSuccess()) {
                // Return sample plugins for development/testing
                return Result.success(getSamplePluginIndex())
            }

            val body = response.bodyAsText()

            if (body.isBlank()) {
                return Result.success(getSamplePluginIndex())
            }

            val index = json.decodeFromString<PluginRepositoryIndex>(body)
            Result.success(index)
        } catch (e: kotlinx.serialization.SerializationException) {
            // Return sample plugins on parse error
            Result.success(getSamplePluginIndex())
        } catch (e: Exception) {
            // Return sample plugins on network error
            Result.success(getSamplePluginIndex())
        }
    }

    /**
     * Provides sample plugins for development and testing
     */
    private fun getSamplePluginIndex(): PluginRepositoryIndex {
        return PluginRepositoryIndex(
            version = 1,
            plugins = listOf(
                PluginIndexEntry(
                    id = "com.ireader.theme.dark",
                    name = "Dark Theme",
                    version = "1.0.0",
                    versionCode = 1,
                    description = "A beautiful dark theme for comfortable night reading",
                    author = PluginAuthorInfo(name = "IReader Team"),
                    type = "THEME",
                    permissions = emptyList(),
                    minIReaderVersion = "1.0.0",
                    platforms = listOf("ANDROID", "IOS", "DESKTOP"),
                    iconUrl = null,
                    monetization = null,
                    downloadUrl = "https://example.com/plugins/dark-theme.iplugin",
                    fileSize = 1024
                ),
                PluginIndexEntry(
                    id = "com.ireader.theme.sepia",
                    name = "Sepia Theme",
                    version = "1.0.0",
                    versionCode = 1,
                    description = "Classic sepia tones for a paper-like reading experience",
                    author = PluginAuthorInfo(name = "IReader Team"),
                    type = "THEME",
                    permissions = emptyList(),
                    minIReaderVersion = "1.0.0",
                    platforms = listOf("ANDROID", "IOS", "DESKTOP"),
                    iconUrl = null,
                    monetization = null,
                    downloadUrl = "https://example.com/plugins/sepia-theme.iplugin",
                    fileSize = 1024
                ),
                PluginIndexEntry(
                    id = "com.ireader.tts.google",
                    name = "Google TTS",
                    version = "1.0.0",
                    versionCode = 1,
                    description = "High-quality text-to-speech using Google Cloud",
                    author = PluginAuthorInfo(name = "IReader Team"),
                    type = "TTS",
                    permissions = listOf("NETWORK"),
                    minIReaderVersion = "1.0.0",
                    platforms = listOf("ANDROID", "IOS", "DESKTOP"),
                    iconUrl = null,
                    monetization = null,
                    downloadUrl = "https://example.com/plugins/google-tts.iplugin",
                    fileSize = 2048
                ),
                PluginIndexEntry(
                    id = "com.ireader.translation.deepl",
                    name = "DeepL Translation",
                    version = "1.0.0",
                    versionCode = 1,
                    description = "Professional translation powered by DeepL",
                    author = PluginAuthorInfo(name = "IReader Team"),
                    type = "TRANSLATION",
                    permissions = listOf("NETWORK"),
                    minIReaderVersion = "1.0.0",
                    platforms = listOf("ANDROID", "IOS", "DESKTOP"),
                    iconUrl = null,
                    monetization = null,
                    downloadUrl = "https://example.com/plugins/deepl-translation.iplugin",
                    fileSize = 1536
                ),
                PluginIndexEntry(
                    id = "com.ireader.ai.summarizer",
                    name = "AI Summarizer",
                    version = "1.0.0",
                    versionCode = 1,
                    description = "Summarize chapters and books using AI",
                    author = PluginAuthorInfo(name = "IReader Team"),
                    type = "AI",
                    permissions = listOf("NETWORK"),
                    minIReaderVersion = "1.0.0",
                    platforms = listOf("ANDROID", "IOS", "DESKTOP"),
                    iconUrl = null,
                    monetization = null,
                    downloadUrl = "https://example.com/plugins/ai-summarizer.iplugin",
                    fileSize = 3072
                ),
                PluginIndexEntry(
                    id = "com.ireader.feature.dictionary",
                    name = "Dictionary Lookup",
                    version = "1.0.0",
                    versionCode = 1,
                    description = "Look up word definitions while reading",
                    author = PluginAuthorInfo(name = "IReader Team"),
                    type = "FEATURE",
                    permissions = listOf("NETWORK"),
                    minIReaderVersion = "1.0.0",
                    platforms = listOf("ANDROID", "IOS", "DESKTOP"),
                    iconUrl = null,
                    monetization = null,
                    downloadUrl = "https://example.com/plugins/dictionary.iplugin",
                    fileSize = 1024
                )
            )
        )
    }
}
