// package ireader.domain.usecases.translate

// import io.ktor.client.*
// import io.ktor.client.engine.cio.*
// import io.ktor.client.plugins.contentnegotiation.*
// import io.ktor.serialization.kotlinx.json.*
// import io.mockk.*
// import ireader.core.http.HttpClients
// import ireader.core.prefs.Preference
// import ireader.domain.preferences.prefs.ReaderPreferences
// import ireader.i18n.UiText
// import kotlinx.coroutines.runBlocking
// import kotlinx.serialization.json.Json
// import kotlin.test.*

// /**
//  * Integration test for DeepSeekTranslateEngine with real API calls
//  */
// class DeepSeekTranslateEngineIntegrationTest {

//     private val testApiKey = "sk-xxxxxxxxxxxxxxxxxxxxxxx"

//     @Test
//     fun `test real DeepSeek API call`() = runBlocking {
//         // Create real HTTP client
//         val realHttpClient = HttpClient(CIO) {
//             install(ContentNegotiation) {
//                 json(Json { 
//                     ignoreUnknownKeys = true 
//                     isLenient = true
//                 })
//             }
//         }
        
//         val mockHttpClients = mockk<HttpClients> {
//             every { default } returns realHttpClient
//         }
        
//         val mockPrefs = mockk<ReaderPreferences>(relaxed = true)
//         val apiKeyPref = mockk<Preference<String>> {
//             every { get() } returns testApiKey
//         }
//         every { mockPrefs.deepSeekApiKey() } returns apiKeyPref
        
//         val engine = DeepSeekTranslateEngine(mockHttpClients, mockPrefs)
        
//         var result: List<String>? = null
//         var error: UiText? = null
//         var lastProgress = 0
        
//         println("=== DeepSeek API Integration Test ===")
//         println("API Key: ${testApiKey.take(15)}...")
        
//         engine.translate(
//             texts = listOf("Hello, how are you?"),
//             source = "en",
//             target = "zh",
//             onProgress = { progress ->
//                 lastProgress = progress
//                 println("Progress: $progress%")
//             },
//             onSuccess = { translations ->
//                 result = translations
//                 println("SUCCESS! Translations: $translations")
//             },
//             onError = { err ->
//                 error = err
//                 println("ERROR: $err")
//                 when (err) {
//                     is UiText.DynamicString -> println("Error message: ${err.text}")
//                     is UiText.ExceptionString -> println("Exception: ${err.e.message}")
//                     else -> println("Other error type: $err")
//                 }
//             }
//         )
        
//         realHttpClient.close()
        
//         println("\n=== Test Results ===")
//         println("Result: $result")
//         println("Error: $error")
//         println("Last Progress: $lastProgress")
        
//         if (error != null) {
//             println("\nAPI call failed. Possible reasons:")
//             println("1. Invalid API key")
//             println("2. Insufficient account balance (402)")
//             println("3. Network issues")
//             println("4. API rate limiting (429)")
//         }
//     }
// }
