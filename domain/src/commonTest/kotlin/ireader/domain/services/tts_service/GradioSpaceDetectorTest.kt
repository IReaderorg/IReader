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
            "https://breezeblue-breeze-tts-2-demo.hf.space",
            GradioSpaceDetector.normalizeSpaceUrl("https://huggingface.co/spaces/BreezeBlue/breeze-tts-2-demo")
        )

        assertEquals(
            "https://k2-fsa-omnivoice.hf.space",
            GradioSpaceDetector.normalizeSpaceUrl("https://huggingface.co/spaces/k2-fsa/OmniVoice")
        )
    }

    @Test
    fun testDeriveFriendlyName() {
        assertEquals("Hexgrad Kokoro TTS", GradioSpaceDetector.deriveFriendlyName("hexgrad/Kokoro-TTS", null))
        assertEquals("Custom Space Title", GradioSpaceDetector.deriveFriendlyName("hexgrad/Kokoro-TTS", "Custom Space Title"))
        assertEquals("BreezeBlue Breeze Tts 2 Demo", GradioSpaceDetector.deriveFriendlyName("https://huggingface.co/spaces/BreezeBlue/breeze-tts-2-demo", null))
    }

    @Test
    fun testCommunityTemplates() {
        val templates = GradioCommunityTemplates.TEMPLATES
        assertTrue(templates.isNotEmpty())

        val breezeblue = templates.find { it.id == "template_breezeblue" }
        assertNotNull(breezeblue)
        assertEquals("/voice_design", breezeblue.apiName)
        assertTrue(breezeblue.parameters.any { it.isTextInput })

        val omnivoice = templates.find { it.id == "template_omnivoice" }
        assertNotNull(omnivoice)
        assertEquals("/_design_fn", omnivoice.apiName)
        assertTrue(omnivoice.parameters.any { it.isTextInput })

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
