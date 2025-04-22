package ireader.domain.data.engines

import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

class OllamaTranslateEngine(
    private val readerPreferences: ReaderPreferences,
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
) : TranslateEngine() {

    override val id: Long = 11L
    override val engineName: String = "Ollama"
    override val supportsAI: Boolean = true
    override val supportsContextAwareTranslation: Boolean = true
    override val supportsStylePreservation: Boolean = true
    override val requiresApiKey: Boolean = false

    @Serializable
    data class OllamaRequest(
        val model: String,
        val prompt: String,
        val stream: Boolean = false
    )

    @Serializable
    data class OllamaResponse(
        val model: String,
        val created_at: String? = null,
        val response: String,
        val done: Boolean
    )

    override suspend fun translate(
        texts: List<String>,
        source: String,
        target: String,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        val contentType = ContentType.entries.toTypedArray()
            .getOrElse(readerPreferences.translatorContentType().get()) {
            ireader.domain.data.engines.ContentType.GENERAL
        }
        val toneType = ToneType.entries.toTypedArray()
            .getOrElse(readerPreferences.translatorToneType().get()) {
            ireader.domain.data.engines.ToneType.NEUTRAL
        }
        val context = TranslationContext(
            contentType = contentType,
            preserveStyle = readerPreferences.translatorPreserveStyle().get(),
            toneType = toneType
        )
        translateWithContext(texts, source, target, context, onProgress, onSuccess, onError)
    }

    override suspend fun translateWithContext(
        texts: List<String>,
        source: String,
        target: String,
        context: TranslationContext,
        onProgress: (Int) -> Unit,
        onSuccess: (List<String>) -> Unit,
        onError: (UiText) -> Unit
    ) {
        try {
            val serverUrl = readerPreferences.ollamaServerUrl().get()
            val model = readerPreferences.ollamaModel().get()
            
            if (serverUrl.isBlank()) {
                onError(UiText.MStringResource(MR.strings.ollama_server_url_not_set))
                return
            }

            val results = withContext(Dispatchers.IO) {
                val translations = mutableListOf<String>()
                
                texts.forEachIndexed { index, text ->
                    if (text.isBlank()) {
                        translations.add("")
                        onProgress((index + 1) * 100 / texts.size)
                        return@forEachIndexed
                    }

                    val contentTypeStr = when (context.contentType) {
                        ContentType.LITERARY -> "literary text"
                        ContentType.TECHNICAL -> "technical document"
                        ContentType.CONVERSATION -> "conversation"
                        ContentType.POETRY -> "poetry"
                        ContentType.ACADEMIC -> "academic paper"
                        else -> "general text"
                    }

                    val toneStr = when (context.toneType) {
                        ToneType.FORMAL -> "formal"
                        ToneType.CASUAL -> "casual"
                        ToneType.PROFESSIONAL -> "professional"
                        ToneType.HUMOROUS -> "humorous"
                        else -> "neutral"
                    }

                    val stylePreservation = if (context.preserveStyle) {
                        "Carefully preserve the original formatting, line breaks, and text styling of the input text."
                    } else {
                        "You may adjust formatting as needed."
                    }

                    val prompt = """
                        You are a translation assistant that specializes in translating from $source to $target. 
                        Translate the following $contentTypeStr with a $toneStr tone:
                        
                        $text
                        
                        Only respond with the translated text. Do not include any explanations or notes.
                        $stylePreservation
                    """.trimIndent()

                    val request = OllamaRequest(
                        model = model,
                        prompt = prompt
                    )

                    val response = client.post(serverUrl) {
                        contentType(io.ktor.http.ContentType.Application.Json)
                        setBody(request)
                    }

                    if (response.status.isSuccess()) {
                        val result = Json.decodeFromString<OllamaResponse>(response.bodyAsText())
                        translations.add(result.response.trim())
                    } else {
                        throw Exception("Failed to translate: ${response.status}")
                    }

                    onProgress((index + 1) * 100 / texts.size)
                }
                
                translations
            }
            
            onSuccess(results)
        } catch (e: Exception) {
            onError(UiText.ExceptionString(e))
        }
    }
} 