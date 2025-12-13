package ireader.data.characterart

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Generates image prompts from chapter text using Gemini AI.
 * 
 * This class:
 * 1. Cleans chapter text (removes dialogs, unnecessary content)
 * 2. Summarizes the chapter focusing on visual elements
 * 3. Generates an image prompt for character art or scene
 */
class ChapterArtPromptGenerator(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    companion object {
        private const val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        private const val DEFAULT_MODEL = "gemini-2.0-flash"
        
        // Max characters to send to Gemini (to avoid token limits)
        private const val MAX_INPUT_CHARS = 15000
        
        // Prompt template for generating image prompts
        private val SYSTEM_PROMPT = """
You are an expert at analyzing novel chapters and creating detailed image generation prompts.

Your task:
1. Read the chapter text provided
2. Identify the most visually interesting element - either:
   - A character's appearance (face, clothing, distinctive features)
   - A dramatic scene or moment
   - A setting or location
3. Generate a detailed image prompt suitable for AI image generation

Rules:
- Focus on VISUAL details only
- Be specific about colors, lighting, mood, style
- Include art style suggestions (digital art, anime, realistic, etc.)
- Keep the prompt under 200 words
- Do NOT include any dialogue or text in the prompt
- Make it suitable for character portrait or scene illustration

Output format:
Return ONLY the image prompt, nothing else. No explanations, no prefixes.
        """.trimIndent()
    }
    
    /**
     * Clean chapter text by removing unnecessary content
     */
    fun cleanChapterText(rawText: String): String {
        var text = rawText
        
        // Remove common chapter markers
        text = text.replace(Regex("^Chapter\\s*\\d+[:\\s]*", RegexOption.IGNORE_CASE), "")
        text = text.replace(Regex("^Part\\s*\\d+[:\\s]*", RegexOption.IGNORE_CASE), "")
        
        // Remove excessive whitespace
        text = text.replace(Regex("\\s{3,}"), "\n\n")
        
        // Remove URLs
        text = text.replace(Regex("https?://\\S+"), "")
        
        // Remove author notes patterns
        text = text.replace(Regex("\\[A/N:.*?\\]", RegexOption.DOT_MATCHES_ALL), "")
        text = text.replace(Regex("\\(Author's Note:.*?\\)", RegexOption.DOT_MATCHES_ALL), "")
        
        // Remove translator notes
        text = text.replace(Regex("\\[TL:.*?\\]", RegexOption.DOT_MATCHES_ALL), "")
        text = text.replace(Regex("\\[TN:.*?\\]", RegexOption.DOT_MATCHES_ALL), "")
        
        // Trim and limit length
        text = text.trim()
        if (text.length > MAX_INPUT_CHARS) {
            // Take first portion (usually has character introductions)
            // and last portion (usually has climax/important scenes)
            val firstPart = text.take(MAX_INPUT_CHARS / 2)
            val lastPart = text.takeLast(MAX_INPUT_CHARS / 2)
            text = "$firstPart\n\n[...]\n\n$lastPart"
        }
        
        return text
    }
    
    /**
     * Generate an image prompt from chapter text using Gemini
     * @param model Optional model to use (defaults to gemini-2.0-flash if blank)
     */
    suspend fun generateImagePrompt(
        apiKey: String,
        chapterText: String,
        bookTitle: String,
        chapterTitle: String,
        preferredFocus: PromptFocus = PromptFocus.AUTO,
        model: String = ""
    ): Result<GeneratedPromptResult> {
        if (apiKey.isBlank()) {
            return Result.failure(Exception("Gemini API key is required"))
        }
        
        val cleanedText = cleanChapterText(chapterText)
        
        if (cleanedText.length < 100) {
            return Result.failure(Exception("Chapter text is too short to analyze"))
        }
        
        val focusInstruction = when (preferredFocus) {
            PromptFocus.CHARACTER -> "Focus on describing a CHARACTER from this chapter."
            PromptFocus.SCENE -> "Focus on describing a SCENE or MOMENT from this chapter."
            PromptFocus.SETTING -> "Focus on describing the SETTING or LOCATION from this chapter."
            PromptFocus.AUTO -> "Choose the most visually interesting element (character, scene, or setting)."
        }
        
        val userPrompt = """
Book: "$bookTitle"
Chapter: "$chapterTitle"

$focusInstruction

Chapter text:
---
$cleanedText
---

Generate an image prompt based on this chapter.
        """.trimIndent()
        
        return try {
            val modelToUse = model.ifBlank { DEFAULT_MODEL }
            val response = httpClient.post("$GEMINI_API_URL/$modelToUse:generateContent?key=$apiKey") {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(
                    GeminiRequest.serializer(),
                    GeminiRequest(
                        contents = listOf(
                            GeminiContent(
                                parts = listOf(
                                    GeminiPart(text = SYSTEM_PROMPT),
                                    GeminiPart(text = userPrompt)
                                )
                            )
                        ),
                        generationConfig = GeminiGenerationConfig(
                            temperature = 0.7f,
                            maxOutputTokens = 500
                        )
                    )
                ))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                val geminiResponse = json.decodeFromString<GeminiResponse>(responseBody)
                
                val generatedPrompt = geminiResponse.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?.trim()
                
                if (generatedPrompt.isNullOrBlank()) {
                    Result.failure(Exception("Failed to generate prompt - empty response"))
                } else {
                    Result.success(
                        GeneratedPromptResult(
                            imagePrompt = generatedPrompt,
                            bookTitle = bookTitle,
                            chapterTitle = chapterTitle,
                            focus = preferredFocus
                        )
                    )
                }
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Gemini API error: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Focus type for prompt generation
 */
enum class PromptFocus {
    AUTO,       // Let AI decide
    CHARACTER,  // Focus on character appearance
    SCENE,      // Focus on a dramatic scene
    SETTING     // Focus on location/environment
}

/**
 * Result of prompt generation
 */
data class GeneratedPromptResult(
    val imagePrompt: String,
    val bookTitle: String,
    val chapterTitle: String,
    val focus: PromptFocus
)

// Gemini API DTOs
@Serializable
private data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

@Serializable
private data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String = "user"
)

@Serializable
private data class GeminiPart(
    val text: String
)

@Serializable
private data class GeminiGenerationConfig(
    val temperature: Float = 0.7f,
    val maxOutputTokens: Int = 500
)

@Serializable
private data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
private data class GeminiCandidate(
    val content: GeminiResponseContent? = null
)

@Serializable
private data class GeminiResponseContent(
    val parts: List<GeminiPart>? = null
)
