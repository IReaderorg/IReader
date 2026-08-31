package ireader.domain.services.tts_service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GradioSpaceDetectorTest {

    @Test
    fun testNormalizeSpaceUrl() {
        assertEquals(
            "https://hexgrad-kokoro-tts.hf.space",
            GradioSpaceDetector.normalizeSpaceUrl("hexgrad/Kokoro-TTS")
        )

        assertEquals(
            "https://hexgrad-kokoro-tts.hf.space",
            GradioSpaceDetector.normalizeSpaceUrl("https://huggingface.co/spaces/hexgrad/Kokoro-TTS")
        )

        assertEquals(
            "https://coqui-xtts.hf.space",
            GradioSpaceDetector.normalizeSpaceUrl("https://huggingface.co/spaces/coqui/xtts/")
        )

        assertEquals(
            "http://localhost:7860",
            GradioSpaceDetector.normalizeSpaceUrl("http://localhost:7860/")
        )
    }

    @Test
    fun testDeriveFriendlyName() {
        assertEquals("Hexgrad Kokoro TTS", GradioSpaceDetector.deriveFriendlyName("hexgrad/Kokoro-TTS", null))
        assertEquals("Custom Space Title", GradioSpaceDetector.deriveFriendlyName("hexgrad/Kokoro-TTS", "Custom Space Title"))
    }

    @Test
    fun testCommunityTemplates() {
        val templates = GradioCommunityTemplates.TEMPLATES
        assertTrue(templates.isNotEmpty())

        val kokoro = templates.find { it.id == "template_kokoro" }
        assertNotNull(kokoro)
        assertEquals("/predict", kokoro.apiName)
        assertTrue(kokoro.parameters.any { it.isTextInput })
        assertTrue(kokoro.parameters.any { it.type == GradioParamType.CHOICE })

        val instance = GradioCommunityTemplates.createFromTemplate(kokoro)
        assertTrue(instance.isCustom)
        assertTrue(instance.id.startsWith("custom_"))
    }
}
