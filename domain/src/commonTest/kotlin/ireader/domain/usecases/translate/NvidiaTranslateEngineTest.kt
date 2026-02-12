package ireader.domain.usecases.translate

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import ireader.core.http.BrowserEngine
import ireader.core.http.CookieSynchronizer
import ireader.core.http.HttpClientsInterface
import ireader.core.http.NetworkConfig
import ireader.core.http.SSLConfiguration
import ireader.core.http.CloudflareBypassHandler
import ireader.core.http.NoOpCloudflareBypassHandler
import ireader.core.prefs.Preference
import ireader.domain.data.engines.ContentType
import ireader.domain.data.engines.ToneType
import ireader.domain.data.engines.TranslationContext
import ireader.i18n.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.*

/**
 * TDD Tests for NvidiaTranslateEngine
 * 
 * Following Red-Green-Refactor methodology:
 * 1. Write failing test first
 * 2. Write minimal code to pass
 * 3. Refactor
 */
class NvidiaTranslateEngineTest {

    private lateinit var engine: NvidiaTranslateEngine
    private lateinit var mockHttpClients: HttpClientsInterface
    private lateinit var mockNvidiaApiKey: Preference<String>
    private lateinit var mockNvidiaModel: Preference<String>

    private val testApiKey = "nvapi-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

    // ==================== Engine Properties Tests ====================

    @Test
    fun `engine should have correct id`() {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        assertEquals(10L, engine.id, "NVIDIA engine ID should be 10")
    }

    @Test
    fun `engine should have correct name`() {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        assertEquals("NVIDIA NIM", engine.engineName, "Engine name should be 'NVIDIA NIM'")
    }

    @Test
    fun `engine should support AI features`() {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        assertTrue(engine.supportsAI, "NVIDIA engine should support AI")
        assertTrue(engine.supportsContextAwareTranslation, "Should support context-aware translation")
        assertTrue(engine.supportsStylePreservation, "Should support style preservation")
    }

    @Test
    fun `engine should require API key`() {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        assertTrue(engine.requiresApiKey, "NVIDIA engine should require API key")
    }

    @Test
    fun `engine should not be offline`() {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        assertFalse(engine.isOffline, "NVIDIA engine should not be offline")
    }

    @Test
    fun `engine should have appropriate rate limit delay`() {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        assertTrue(engine.rateLimitDelayMs >= 1000L, "Rate limit delay should be at least 1 second")
        assertTrue(engine.rateLimitDelayMs <= 5000L, "Rate limit delay should be at most 5 seconds")
    }

    @Test
    fun `engine should have appropriate max chars per request`() {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        assertTrue(engine.maxCharsPerRequest >= 2000, "Max chars should be at least 2000")
        assertTrue(engine.maxCharsPerRequest <= 8000, "Max chars should be at most 8000")
    }

    // ==================== Language Support Tests ====================

    @Test
    fun `engine should support common languages`() {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        val languages = engine.supportedLanguages
        
        assertTrue(languages.isNotEmpty(), "Should support multiple languages")
        assertTrue(languages.any { it.first == "en" }, "Should support English")
        assertTrue(languages.any { it.first == "zh" }, "Should support Chinese")
        assertTrue(languages.any { it.first == "ja" }, "Should support Japanese")
        assertTrue(languages.any { it.first == "ko" }, "Should support Korean")
        assertTrue(languages.any { it.first == "es" }, "Should support Spanish")
        assertTrue(languages.any { it.first == "fr" }, "Should support French")
        assertTrue(languages.any { it.first == "de" }, "Should support German")
        assertTrue(languages.any { it.first == "auto" }, "Should support auto-detect")
    }

    @Test
    fun `engine should support RTL languages`() {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        val languages = engine.supportedLanguages
        
        assertTrue(languages.any { it.first == "ar" }, "Should support Arabic")
        assertTrue(languages.any { it.first == "fa" }, "Should support Farsi/Persian")
        assertTrue(languages.any { it.first == "he" }, "Should support Hebrew")
    }

    // ==================== Model Support Tests ====================

    @Test
    fun `engine should have predefined models available`() {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        val models = engine.availableModels
        
        assertTrue(models.isNotEmpty(), "Should have predefined models")
        assertTrue(models.any { it.first.contains("llama", ignoreCase = true) }, "Should have Llama models")
        assertTrue(models.any { it.first.contains("mistral", ignoreCase = true) }, "Should have Mistral models")
    }

    @Test
    fun `engine should return cached models when available`() {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        val models = engine.getCachedModels()
        
        assertTrue(models.isNotEmpty(), "Should return models from static list")
    }

    // ==================== Translation Error Handling Tests ====================

    @Test
    fun `translate should fail with empty text list`() = runTest {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        var errorReceived: UiText? = null

        engine.translate(
            texts = emptyList(),
            source = "en",
            target = "zh",
            onProgress = {},
            onSuccess = { fail("Should not succeed with empty list") },
            onError = { errorReceived = it }
        )

        assertNotNull(errorReceived, "Should receive error for empty text list")
    }

    @Test
    fun `translate should fail without API key`() = runTest {
        mockHttpClients = createMockHttpClients(successResponse())
        mockNvidiaApiKey = createMockPreference("")
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        var errorReceived: UiText? = null

        engine.translate(
            texts = listOf("Hello"),
            source = "en",
            target = "zh",
            onProgress = {},
            onSuccess = { fail("Should not succeed without API key") },
            onError = { errorReceived = it }
        )

        assertNotNull(errorReceived, "Should receive error without API key")
    }

    @Test
    fun `translate should handle 401 unauthorized error`() = runTest {
        mockHttpClients = createMockHttpClients(errorResponse(HttpStatusCode.Unauthorized))
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        var errorReceived: UiText? = null

        engine.translate(
            texts = listOf("Hello"),
            source = "en",
            target = "zh",
            onProgress = {},
            onSuccess = { fail("Should not succeed with 401 error") },
            onError = { errorReceived = it }
        )

        assertNotNull(errorReceived, "Should receive error for 401")
    }

    @Test
    fun `translate should handle 429 rate limit error`() = runTest {
        mockHttpClients = createMockHttpClients(errorResponse(HttpStatusCode.TooManyRequests))
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        var errorReceived: UiText? = null

        engine.translate(
            texts = listOf("Hello"),
            source = "en",
            target = "zh",
            onProgress = {},
            onSuccess = { fail("Should not succeed with 429 error") },
            onError = { errorReceived = it }
        )

        assertNotNull(errorReceived, "Should receive error for rate limit")
    }

    @Test
    fun `translate should handle 402 payment required error`() = runTest {
        mockHttpClients = createMockHttpClients(errorResponse(HttpStatusCode.PaymentRequired))
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        var errorReceived: UiText? = null

        engine.translate(
            texts = listOf("Hello"),
            source = "en",
            target = "zh",
            onProgress = {},
            onSuccess = { fail("Should not succeed with 402 error") },
            onError = { errorReceived = it }
        )

        assertNotNull(errorReceived, "Should receive error for insufficient credits")
    }

    // ==================== Translation Success Tests ====================

    @Test
    fun `translate should succeed with valid response`() = runTest {
        mockHttpClients = createMockHttpClients(successResponse("你好世界"))
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        var result: List<String>? = null
        val progressUpdates = mutableListOf<Int>()

        engine.translate(
            texts = listOf("Hello World"),
            source = "en",
            target = "zh",
            onProgress = { progressUpdates.add(it) },
            onSuccess = { result = it },
            onError = { fail("Should not fail: $it") }
        )

        assertNotNull(result, "Should receive translation result")
        assertEquals(1, result!!.size, "Should have one translated paragraph")
        assertEquals("你好世界", result!![0], "Translation should match")
        assertTrue(progressUpdates.contains(100), "Should report 100% progress")
    }

    @Test
    fun `translate should handle multiple paragraphs`() = runTest {
        val translatedText = "第一段\n---PARAGRAPH_BREAK---\n第二段\n---PARAGRAPH_BREAK---\n第三段"
        mockHttpClients = createMockHttpClients(successResponse(translatedText))
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        var result: List<String>? = null

        engine.translate(
            texts = listOf("Paragraph 1", "Paragraph 2", "Paragraph 3"),
            source = "en",
            target = "zh",
            onProgress = {},
            onSuccess = { result = it },
            onError = { fail("Should not fail: $it") }
        )

        assertNotNull(result)
        assertEquals(3, result!!.size, "Should have three translated paragraphs")
    }

    @Test
    fun `translate should adjust paragraph count when mismatch`() = runTest {
        val translatedText = "只有一段"
        mockHttpClients = createMockHttpClients(successResponse(translatedText))
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        var result: List<String>? = null

        engine.translate(
            texts = listOf("Para 1", "Para 2", "Para 3"),
            source = "en",
            target = "zh",
            onProgress = {},
            onSuccess = { result = it },
            onError = { fail("Should not fail: $it") }
        )

        assertNotNull(result)
        assertEquals(3, result!!.size, "Should adjust to match input paragraph count")
    }

    @Test
    fun `translate should handle empty choices response`() = runTest {
        mockHttpClients = createMockHttpClients(emptyChoicesResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        var errorReceived: UiText? = null

        engine.translate(
            texts = listOf("Hello"),
            source = "en",
            target = "zh",
            onProgress = {},
            onSuccess = { fail("Should not succeed with empty choices") },
            onError = { errorReceived = it }
        )

        assertNotNull(errorReceived, "Should receive error for empty choices")
    }

    // ==================== Progress Reporting Tests ====================

    @Test
    fun `translate should report progress during translation`() = runTest {
        mockHttpClients = createMockHttpClients(successResponse("翻译"))
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        val progressUpdates = mutableListOf<Int>()

        engine.translate(
            texts = listOf("Text 1"),
            source = "en",
            target = "zh",
            onProgress = { progressUpdates.add(it) },
            onSuccess = {},
            onError = { fail("Should not fail: $it") }
        )

        assertTrue(progressUpdates.isNotEmpty(), "Should report progress")
        assertTrue(progressUpdates.last() == 100, "Final progress should be 100%")
        assertTrue(progressUpdates.first() == 0, "Initial progress should be 0%")
    }

    // ==================== Context-Aware Translation Tests ====================

    @Test
    fun `translateWithContext should use content type instructions`() = runTest {
        mockHttpClients = createMockHttpClients(successResponse("翻译结果"))
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        val context = TranslationContext(
            contentType = ContentType.LITERARY,
            toneType = ToneType.FORMAL,
            preserveStyle = true
        )

        var result: List<String>? = null

        engine.translateWithContext(
            texts = listOf("Test text"),
            source = "en",
            target = "zh",
            context = context,
            onProgress = {},
            onSuccess = { result = it },
            onError = { fail("Should not fail: $it") }
        )

        assertNotNull(result, "Should receive translation with context")
    }

    @Test
    fun `translateWithContext should handle technical content`() = runTest {
        mockHttpClients = createMockHttpClients(successResponse("技术翻译"))
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        val context = TranslationContext(
            contentType = ContentType.TECHNICAL,
            toneType = ToneType.PROFESSIONAL
        )

        var result: List<String>? = null

        engine.translateWithContext(
            texts = listOf("Technical documentation"),
            source = "en",
            target = "zh",
            context = context,
            onProgress = {},
            onSuccess = { result = it },
            onError = { fail("Should not fail: $it") }
        )

        assertNotNull(result)
    }

    // ==================== Model Fetching Tests ====================

    @Test
    fun `fetchAvailableModels should fail without API key`() = runTest {
        mockHttpClients = createMockHttpClients(modelsSuccessResponse())
        mockNvidiaApiKey = createMockPreference("")
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        val result = engine.fetchAvailableModels()

        assertTrue(result.isFailure, "Should fail without API key")
    }

    @Test
    fun `fetchAvailableModels should return models on success`() = runTest {
        mockHttpClients = createMockHttpClients(modelsSuccessResponse())
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        val result = engine.fetchAvailableModels()

        assertTrue(result.isSuccess, "Should succeed with valid API key")
        val models = result.getOrThrow()
        assertTrue(models.isNotEmpty(), "Should return models")
    }

    @Test
    fun `fetchAvailableModels should handle HTTP error`() = runTest {
        mockHttpClients = createMockHttpClients(errorResponse(HttpStatusCode.InternalServerError))
        mockNvidiaApiKey = createMockPreference(testApiKey)
        mockNvidiaModel = createMockPreference("meta/llama-3.1-8b-instruct")
        engine = NvidiaTranslateEngine(mockHttpClients, mockNvidiaApiKey, mockNvidiaModel)

        val result = engine.fetchAvailableModels()

        assertTrue(result.isFailure, "Should fail on HTTP error")
    }

    // ==================== Helper Functions ====================

    private fun createMockHttpClients(mockEngine: MockEngine): HttpClientsInterface {
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { 
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }

        return object : HttpClientsInterface {
            override val default = httpClient
            override val browser: BrowserEngine get() = throw NotImplementedError("Not needed for tests")
            override val cloudflareClient = httpClient
            override val config: NetworkConfig get() = NetworkConfig()
            override val sslConfig: SSLConfiguration get() = SSLConfiguration()
            override val cookieSynchronizer: CookieSynchronizer get() = throw NotImplementedError("Not needed for tests")
            override val cloudflareBypassHandler: CloudflareBypassHandler get() = NoOpCloudflareBypassHandler
        }
    }

    private fun createMockPreference(value: String): Preference<String> {
        return object : Preference<String> {
            override fun get(): String = value
            override fun set(value: String) {}
            override fun key(): String = "test_key"
            override fun defaultValue(): String = ""
            override fun isSet(): Boolean = value.isNotEmpty()
            override fun delete() {}
            override fun changes(): Flow<String> = flowOf()
            override suspend fun stateIn(scope: kotlinx.coroutines.CoroutineScope): kotlinx.coroutines.flow.StateFlow<String> {
                return kotlinx.coroutines.flow.MutableStateFlow(value)
            }
        }
    }

    private fun successResponse(translatedText: String = "翻译结果"): MockEngine {
        return MockEngine { request ->
            respond(
                content = """
                    {
                        "id": "chatcmpl-test123",
                        "choices": [
                            {
                                "message": {
                                    "role": "assistant",
                                    "content": "$translatedText"
                                },
                                "finish_reason": "stop"
                            }
                        ],
                        "usage": {
                            "prompt_tokens": 100,
                            "completion_tokens": 50,
                            "total_tokens": 150
                        }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    }

    private fun errorResponse(statusCode: HttpStatusCode): MockEngine {
        return MockEngine { request ->
            respond(
                content = """{"error": {"message": "Error", "type": "error"}}""",
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    }

    private fun emptyChoicesResponse(): MockEngine {
        return MockEngine { request ->
            respond(
                content = """
                    {
                        "id": "chatcmpl-test123",
                        "choices": []
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    }

    private fun modelsSuccessResponse(): MockEngine {
        return MockEngine { request ->
            respond(
                content = """
                    {
                        "data": [
                            {"id": "meta/llama-3.1-8b-instruct"},
                            {"id": "meta/llama-3.1-70b-instruct"},
                            {"id": "mistralai/mistral-large"}
                        ]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    }
}
