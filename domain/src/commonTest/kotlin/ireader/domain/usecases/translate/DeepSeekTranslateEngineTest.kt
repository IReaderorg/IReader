//package ireader.domain.usecases.translate
//
//import io.ktor.client.*
//import io.ktor.client.engine.mock.*
//import io.ktor.client.plugins.contentnegotiation.*
//import io.ktor.http.*
//import io.ktor.serialization.kotlinx.json.*
//import io.mockk.*
//import ireader.core.http.BrowserEngine
//import ireader.core.http.CookieSynchronizer
//import ireader.core.http.HttpClients
//import ireader.core.http.NetworkConfig
//import ireader.core.http.SSLConfiguration
//import ireader.core.prefs.Preference
//import ireader.core.prefs.PreferenceStore
//import ireader.domain.data.engines.ContentType
//import ireader.domain.data.engines.ToneType
//import ireader.domain.data.engines.TranslationContext
//import ireader.domain.preferences.prefs.ReaderPreferences
//import ireader.i18n.UiText
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.test.runTest
//import kotlinx.serialization.json.Json
//import kotlin.test.*
//
///**
// * Tests for DeepSeekTranslateEngine with chunk translation support
// */
//class DeepSeekTranslateEngineTest {
//
//    private lateinit var engine: DeepSeekTranslateEngine
//    private lateinit var mockHttpClients: HttpClients
//    private lateinit var mockReaderPreferences: ReaderPreferences
//
//    private val testApiKey = "sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
//
//    @BeforeTest
//    fun setup() {
//        MockKAnnotations.init(this)
//        mockReaderPreferences = createMockReaderPreferences(testApiKey)
//    }
//
//    @AfterTest
//    fun tearDown() {
//        unmockkAll()
//    }
//
//    @Test
//    fun `engine should have correct properties`() {
//        mockHttpClients = createMockHttpClients(successResponse())
//        engine = DeepSeekTranslateEngine(mockHttpClients, mockReaderPreferences)
//
//        assertEquals(3L, engine.id)
//        assertEquals("DeepSeek AI", engine.engineName)
//        assertTrue(engine.supportsAI)
//        assertTrue(engine.supportsContextAwareTranslation)
//        assertTrue(engine.supportsStylePreservation)
//        assertTrue(engine.requiresApiKey)
//    }
//
//    @Test
//    fun `engine should support multiple languages`() {
//        mockHttpClients = createMockHttpClients(successResponse())
//        engine = DeepSeekTranslateEngine(mockHttpClients, mockReaderPreferences)
//
//        assertTrue(engine.supportedLanguages.isNotEmpty())
//        assertTrue(engine.supportedLanguages.any { it.first == "en" })
//        assertTrue(engine.supportedLanguages.any { it.first == "zh" })
//        assertTrue(engine.supportedLanguages.any { it.first == "ja" })
//        assertTrue(engine.supportedLanguages.any { it.first == "auto" })
//    }
//
//    @Test
//    fun `translate should fail with empty text list`() = runTest {
//        mockHttpClients = createMockHttpClients(successResponse())
//        engine = DeepSeekTranslateEngine(mockHttpClients, mockReaderPreferences)
//
//        var errorReceived: UiText? = null
//
//        engine.translate(
//            texts = emptyList(),
//            source = "en",
//            target = "zh",
//            onProgress = {},
//            onSuccess = { fail("Should not succeed with empty list") },
//            onError = { errorReceived = it }
//        )
//
//        assertNotNull(errorReceived)
//    }
//
//    @Test
//    fun `translate should fail without API key`() = runTest {
//        val prefsWithoutKey = createMockReaderPreferences("")
//        mockHttpClients = createMockHttpClients(successResponse())
//        engine = DeepSeekTranslateEngine(mockHttpClients, prefsWithoutKey)
//
//        var errorReceived: UiText? = null
//
//        engine.translate(
//            texts = listOf("Hello"),
//            source = "en",
//            target = "zh",
//            onProgress = {},
//            onSuccess = { fail("Should not succeed without API key") },
//            onError = { errorReceived = it }
//        )
//
//        assertNotNull(errorReceived)
//    }
//
//    @Test
//    fun `translate should succeed with valid response`() = runTest {
//        mockHttpClients = createMockHttpClients(successResponse("你好世界"))
//        engine = DeepSeekTranslateEngine(mockHttpClients, mockReaderPreferences)
//
//        var result: List<String>? = null
//        val progressUpdates = mutableListOf<Int>()
//
//        engine.translate(
//            texts = listOf("Hello World"),
//            source = "en",
//            target = "zh",
//            onProgress = { progressUpdates.add(it) },
//            onSuccess = { result = it },
//            onError = { fail("Should not fail: $it") }
//        )
//
//        assertNotNull(result)
//        assertEquals(1, result!!.size)
//        assertEquals("你好世界", result!![0])
//        assertTrue(progressUpdates.contains(100))
//    }
//
//    @Test
//    fun `translate should handle multiple paragraphs with chunk support`() = runTest {
//        val translatedText = "第一段\n---PARAGRAPH_BREAK---\n第二段\n---PARAGRAPH_BREAK---\n第三段"
//        mockHttpClients = createMockHttpClients(successResponse(translatedText))
//        engine = DeepSeekTranslateEngine(mockHttpClients, mockReaderPreferences)
//
//        var result: List<String>? = null
//
//        engine.translate(
//            texts = listOf("Paragraph 1", "Paragraph 2", "Paragraph 3"),
//            source = "en",
//            target = "zh",
//            onProgress = {},
//            onSuccess = { result = it },
//            onError = { fail("Should not fail: $it") }
//        )
//
//        assertNotNull(result)
//        assertEquals(3, result!!.size)
//    }
//
//    @Test
//    fun `translateWithContext should use content type instructions`() = runTest {
//        mockHttpClients = createMockHttpClients(successResponse("翻译结果"))
//        engine = DeepSeekTranslateEngine(mockHttpClients, mockReaderPreferences)
//
//        val context = TranslationContext(
//            contentType = ContentType.LITERARY,
//            toneType = ToneType.FORMAL,
//            preserveStyle = true
//        )
//
//        var result: List<String>? = null
//
//        engine.translateWithContext(
//            texts = listOf("Test text"),
//            source = "en",
//            target = "zh",
//            context = context,
//            onProgress = {},
//            onSuccess = { result = it },
//            onError = { fail("Should not fail: $it") }
//        )
//
//        assertNotNull(result)
//    }
//
//    @Test
//    fun `translate should handle empty choices response`() = runTest {
//        mockHttpClients = createMockHttpClients(emptyChoicesResponse())
//        engine = DeepSeekTranslateEngine(mockHttpClients, mockReaderPreferences)
//
//        var errorReceived: UiText? = null
//
//        engine.translate(
//            texts = listOf("Hello"),
//            source = "en",
//            target = "zh",
//            onProgress = {},
//            onSuccess = { fail("Should not succeed with empty choices") },
//            onError = { errorReceived = it }
//        )
//
//        assertNotNull(errorReceived)
//    }
//
//    @Test
//    fun `translate should adjust paragraph count when mismatch`() = runTest {
//        val translatedText = "只有一段"
//        mockHttpClients = createMockHttpClients(successResponse(translatedText))
//        engine = DeepSeekTranslateEngine(mockHttpClients, mockReaderPreferences)
//
//        var result: List<String>? = null
//
//        engine.translate(
//            texts = listOf("Para 1", "Para 2", "Para 3"),
//            source = "en",
//            target = "zh",
//            onProgress = {},
//            onSuccess = { result = it },
//            onError = { fail("Should not fail: $it") }
//        )
//
//        assertNotNull(result)
//        assertEquals(3, result!!.size)
//    }
//
//    @Test
//    fun `max chunk size constant should be defined`() {
//        assertEquals(20, DeepSeekTranslateEngine.MAX_CHUNK_SIZE)
//    }
//
//    @Test
//    fun `translate should report progress during chunk processing`() = runTest {
//        mockHttpClients = createMockHttpClients(successResponse("翻译"))
//        engine = DeepSeekTranslateEngine(mockHttpClients, mockReaderPreferences)
//
//        val progressUpdates = mutableListOf<Int>()
//
//        engine.translate(
//            texts = listOf("Text 1"),
//            source = "en",
//            target = "zh",
//            onProgress = { progressUpdates.add(it) },
//            onSuccess = {},
//            onError = { fail("Should not fail: $it") }
//        )
//
//        assertTrue(progressUpdates.isNotEmpty())
//        assertTrue(progressUpdates.last() == 100)
//    }
//
//    // ========== Helper Functions ==========
//
//    private fun createMockHttpClients(mockEngine: MockEngine): HttpClients {
//        val httpClient = HttpClient(mockEngine) {
//            install(ContentNegotiation) {
//                json(Json { ignoreUnknownKeys = true })
//            }
//        }
//
//        return mockk<HttpClients> {
//            every { default } returns httpClient
//        }
//    }
//
//    private fun createMockReaderPreferences(apiKey: String): ReaderPreferences {
//        val mockPrefs = mockk<ReaderPreferences>(relaxed = true)
//
//        val apiKeyPref = mockk<Preference<String>> {
//            every { get() } returns apiKey
//        }
//
//        every { mockPrefs.deepSeekApiKey() } returns apiKeyPref
//
//        return mockPrefs
//    }
//
//    private fun successResponse(translatedText: String = "翻译结果"): MockEngine {
//        return MockEngine { request ->
//            respond(
//                content = """
//                    {
//                        "id": "chatcmpl-test123",
//                        "choices": [
//                            {
//                                "message": {
//                                    "role": "assistant",
//                                    "content": "$translatedText"
//                                },
//                                "finish_reason": "stop"
//                            }
//                        ]
//                    }
//                """.trimIndent(),
//                status = HttpStatusCode.OK,
//                headers = headersOf(HttpHeaders.ContentType, "application/json")
//            )
//        }
//    }
//
//    private fun errorResponse(statusCode: HttpStatusCode): MockEngine {
//        return MockEngine { request ->
//            respond(
//                content = """{"error": {"message": "Error", "type": "error"}}""",
//                status = statusCode,
//                headers = headersOf(HttpHeaders.ContentType, "application/json")
//            )
//        }
//    }
//
//    private fun emptyChoicesResponse(): MockEngine {
//        return MockEngine { request ->
//            respond(
//                content = """
//                    {
//                        "id": "chatcmpl-test123",
//                        "choices": []
//                    }
//                """.trimIndent(),
//                status = HttpStatusCode.OK,
//                headers = headersOf(HttpHeaders.ContentType, "application/json")
//            )
//        }
//    }
//}
