//package ireader.domain.data.engines
//
//import io.ktor.client.HttpClient
//import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
//import io.ktor.client.request.post
//import io.ktor.client.request.setBody
//import io.ktor.client.statement.bodyAsText
//import io.ktor.http.contentType
//import io.ktor.http.isSuccess
//import io.ktor.serialization.kotlinx.json.json
//import ireader.domain.preferences.prefs.ReaderPreferences
//import ireader.domain.utils.extensions.ioDispatcher
//import ireader.i18n.UiText
//import ireader.i18n.resources.Res
//import ireader.i18n.resources.ollama_server_url_not_set
//import kotlinx.coroutines.withContext
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.Json
//
//class OllamaTranslateEngine(
//    private val readerPreferences: ReaderPreferences,
//    private val client: HttpClient = HttpClient {
//        install(ContentNegotiation) {
//            json(Json {
//                ignoreUnknownKeys = true
//                isLenient = true
//            })
//        }
//    }
//) : TranslateEngine() {
//
//    override val id: Long = 11L
//    override val engineName: String = "Ollama"
//    override val supportsAI: Boolean = true
//    override val supportsContextAwareTranslation: Boolean = true
//    override val supportsStylePreservation: Boolean = true
//    override val requiresApiKey: Boolean = false
//
//    @Serializable
//    data class OllamaMessage(
//        val role: String,
//        val content: String
//    )
//
//    @Serializable
//    data class OllamaChatRequest(
//        val model: String,
//        val messages: List<OllamaMessage>,
//        val stream: Boolean = false
//    )
//
//    @Serializable
//    data class OllamaChatResponse(
//        val model: String,
//        val created_at: String? = null,
//        val message: OllamaMessage,
//        val done: Boolean
//    )
//
//    override suspend fun translate(
//        texts: List<String>,
//        source: String,
//        target: String,
//        onProgress: (Int) -> Unit,
//        onSuccess: (List<String>) -> Unit,
//        onError: (UiText) -> Unit
//    ) {
//        val contentType = ContentType.entries.toTypedArray()
//            .getOrElse(readerPreferences.translatorContentType().get()) {
//            ireader.domain.data.engines.ContentType.GENERAL
//        }
//        val toneType = ToneType.entries.toTypedArray()
//            .getOrElse(readerPreferences.translatorToneType().get()) {
//            ireader.domain.data.engines.ToneType.NEUTRAL
//        }
//        val context = TranslationContext(
//            contentType = contentType,
//            preserveStyle = readerPreferences.translatorPreserveStyle().get(),
//            toneType = toneType
//        )
//        translateWithContext(texts, source, target, context, onProgress, onSuccess, onError)
//    }
//
//    override suspend fun translateWithContext(
//        texts: List<String>,
//        source: String,
//        target: String,
//        context: TranslationContext,
//        onProgress: (Int) -> Unit,
//        onSuccess: (List<String>) -> Unit,
//        onError: (UiText) -> Unit
//    ) {
//        try {
//            val serverUrl = readerPreferences.ollamaServerUrl().get()
//            val model = readerPreferences.ollamaModel().get()
//
//            if (serverUrl.isBlank()) {
//                onError(UiText.MStringResource(Res.string.ollama_server_url_not_set))
//                return
//            }
//
//            val results = withContext(ioDispatcher) {
//                val translations = mutableListOf<String>()
//
//                texts.forEachIndexed { index, text ->
//                    if (text.isBlank()) {
//                        translations.add("")
//                        onProgress((index + 1) * 100 / texts.size)
//                        return@forEachIndexed
//                    }
//
//                    val contentTypeStr = when (context.contentType) {
//                        ContentType.LITERARY -> "literary text"
//                        ContentType.TECHNICAL -> "technical document"
//                        ContentType.CONVERSATION -> "conversation"
//                        ContentType.POETRY -> "poetry"
//                        ContentType.ACADEMIC -> "academic paper"
//                        else -> "general text"
//                    }
//
//                    val toneStr = when (context.toneType) {
//                        ToneType.FORMAL -> "formal"
//                        ToneType.CASUAL -> "casual"
//                        ToneType.PROFESSIONAL -> "professional"
//                        ToneType.HUMOROUS -> "humorous"
//                        else -> "neutral"
//                    }
//
//                    val stylePreservation = if (context.preserveStyle) {
//                        "Carefully preserve the original formatting, line breaks, and text styling of the input text."
//                    } else {
//                        "You may adjust formatting as needed."
//                    }
//
//                    val systemPrompt = """
//                        You are a translation assistant that specializes in translating from $source to $target.
//                        Translate the text with a $toneStr tone for $contentTypeStr.
//                        Only respond with the translated text. Do not include any explanations or notes.
//                        $stylePreservation
//                    """.trimIndent()
//
//                    val request = OllamaChatRequest(
//                        model = model,
//                        messages = listOf(
//                            OllamaMessage(role = "system", content = systemPrompt),
//                            OllamaMessage(role = "user", content = text)
//                        )
//                    )
//
//                    val apiUrl = serverUrl.trimEnd('/') + "/api/chat"
//                    val response = client.post(apiUrl) {
//                        contentType(io.ktor.http.ContentType.Application.Json)
//                        setBody(request)
//                    }
//
//                    if (response.status.isSuccess()) {
//                        val result = Json.decodeFromString<OllamaChatResponse>(response.bodyAsText())
//                        translations.add(result.message.content.trim())
//                    } else {
//                        throw Exception("Failed to translate: ${response.status}")
//                    }
//
//                    onProgress((index + 1) * 100 / texts.size)
//                }
//
//                translations
//            }
//
//            onSuccess(results)
//        } catch (e: Exception) {
//            onError(UiText.ExceptionString(e))
//        }
//    }
//}