package ireader.domain.usecases.translate

import ireader.i18n.UiText
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoogleTranslateMLTest {

    @Test
    fun `engine properties are correctly configured`() {
        val engine = GoogleTranslateML()
        assertEquals(0L, engine.id)
        assertTrue(engine.supportedLanguages.isNotEmpty())
    }

    @Test
    fun `empty text returns error`() = runTest {
        val engine = GoogleTranslateML()
        var errorReceived: UiText? = null
        
        engine.translate(
            texts = emptyList(),
            source = "en",
            target = "es",
            onProgress = {},
            onSuccess = {},
            onError = { error -> errorReceived = error }
        )
        
        assertTrue(errorReceived != null)
    }
}
